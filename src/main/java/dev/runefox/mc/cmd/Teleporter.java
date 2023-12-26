package dev.runefox.mc.cmd;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.entity.EntityTypeTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

// This class is purely dedicated to teleporting a player and its leashed mobs and pets to another location.
public class Teleporter {
    public final ServerPlayer player;
    public final ServerLevel src;
    public final ServerLevel dst;
    public final double x, y, z;
    public final float yrot, xrot;
    public final boolean teleportPets;
    public final boolean teleportLeashedMobs;

    private final List<TamableAnimal> pets = new ArrayList<>();
    private final List<Mob> leashed = new ArrayList<>();

    public Teleporter(ServerPlayer player, ServerLevel destination, double x, double y, double z, float yrot, float xrot) {
        this.player = player;
        this.dst = destination;
        this.src = player.serverLevel();
        this.x = x;
        this.y = y;
        this.z = z;
        this.yrot = yrot;
        this.xrot = xrot;

        GameRules rules = player.server.getGameRules();
        this.teleportPets = rules.getRule(ModGameRules.TELEPORT_PETS).get().allowTeleport(src, dst);
        this.teleportLeashedMobs = rules.getRule(ModGameRules.TELEPORT_LEASHED_MOBS).get().allowTeleport(src, dst);
    }

    private void findPets() {
        src.getEntities(
            EntityTypeTest.forClass(TamableAnimal.class),
            animal -> animal.isOwnedBy(player) && !animal.isOrderedToSit() && !animal.isLeashed(),
            pets
        );
    }

    private void findLeashedMobs() {
        src.getEntities(
            EntityTypeTest.forClass(Mob.class),
            animal -> animal.getLeashHolder() == player,
            leashed
        );
    }

    private boolean canTeleportInto(Mob mob, BlockPos floor) {
        double wdt = mob.getBbWidth();
        double hgt = mob.getBbHeight();

        double x = floor.getX() + 0.5;
        double y = floor.getY() + 1;
        double z = floor.getZ() + 0.5;

        // Check mob hitbox for any collision shapes, suffocating blocks or fluids
        BlockPos.betweenClosedStream(
                    Mth.floor(x - wdt / 2),
                    Mth.floor(y),
                    Mth.floor(z - wdt / 2),
                    Mth.ceil(x + wdt / 2),
                    Mth.ceil(y + hgt),
                    Mth.ceil(z + wdt / 2)
                )
                .anyMatch(p -> {
                    BlockState state = dst.getBlockState(p);
                    return !state.isSuffocating(dst, p) && state.getCollisionShape(dst, p).isEmpty() && state.getFluidState().isEmpty();
                });

        // Check if block below is solid for entity to stand on
        return dst.getBlockState(floor).entityCanStandOn(dst, floor, mob);
    }

    private BlockPos findReasonableBlockToTeleportToInColumn(Mob mob, int x, int y, int z, int radius, BlockPos.MutableBlockPos mpos) {
        // Search column up and down for a reasonable position
        for (int off = 0; off < radius; off++) {
            int y1 = y + off;
            int y2 = y - off;

            mpos.set(x, y1, z).move(Direction.DOWN); // Move one down to target floor block
            if (canTeleportInto(mob, mpos))
                return mpos.immutable().above();

            mpos.set(x, y2, z).move(Direction.DOWN);
            if (canTeleportInto(mob, mpos))
                return mpos.immutable().above();
        }

        return null;
    }

    private BlockPos findReasonableBlockToTeleportTo(Mob mob, BlockPos pos, int radius) {
        BlockPos.MutableBlockPos mpos = new BlockPos.MutableBlockPos();
        RandomSource rng = mob.getRandom();

        int y = pos.getY();

        // 30 attempts at finding a random column where pet can teleport to
        for (int attempt = 0; attempt < 30; attempt++) {
            int x = rng.nextInt(radius) - rng.nextInt(radius) + pos.getX();
            int z = rng.nextInt(radius) - rng.nextInt(radius) + pos.getZ();

            BlockPos p = findReasonableBlockToTeleportToInColumn(mob, x, y, z, radius, mpos);

            if (p != null)
                return p;
        }

        return null;
    }

    private Mob doTeleport(Mob self, double x, double y, double z, float yr, float xr) {
        float bxr = Mth.clamp(xr, -90.0f, 90.0f);
        if (dst == self.level()) {
            self.teleportTo(dst, x, y, z, Set.of(), yr, xr);
        } else {
            self.unRide();
            Mob entity = (Mob) self.getType().create(dst);
            if (entity == null)
                return null;

            entity.restoreFrom(self);
            entity.moveTo(x, y, z, yr, bxr);
            entity.setYHeadRot(yr);
            self.setRemoved(Entity.RemovalReason.CHANGED_DIMENSION);
            dst.addDuringTeleport(entity);
            return entity;
        }
        return self;
    }

    private Mob teleportMob(Mob mob, int radius) {
        BlockPos reasonablePos = findReasonableBlockToTeleportTo(mob, new BlockPos(
            Mth.floor(x), Mth.floor(y), Mth.floor(z)
        ), radius);

        if (reasonablePos != null) {
            return doTeleport(mob, reasonablePos.getX() + 0.5, reasonablePos.getY(), reasonablePos.getZ() + 0.5, mob.getYRot(), mob.getXRot());
        } else {
            return doTeleport(mob, x, y, z, mob.getYRot(), mob.getXRot());
        }
    }

    public void teleport() {
        // Find pets, only teleport pets if it's through the same dimension
        pets.clear();
        if (teleportPets && src == dst) {
            findPets();
        }

        // Find leashed mobs
        leashed.clear();
        findLeashedMobs();
        if (!teleportLeashedMobs) {
            // Unleash mobs if leashed mobs cannot teleport and drop their leash
            for (Mob mob : leashed) {
                mob.dropLeash(true, true);
            }
            leashed.clear();
        }

        // Unleash mobs before player teleports
        for (Mob mob : leashed) {
            mob.dropLeash(false, false);
        }

        // Teleport player
        player.teleportTo(dst, x, y, z, yrot, xrot);

        // Teleport pets
        for (TamableAnimal pet : pets) {
            teleportMob(pet, 16);
        }

        // Teleport leashed mobs
        for (Mob mob : leashed) {
            mob = teleportMob(mob, 8);
            mob.setLeashedTo(player, true);
        }
    }
}
