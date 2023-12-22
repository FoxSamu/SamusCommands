package dev.runefox.mc.cmd;

import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class ItemClickHandler implements UseItemCallback {

    public static void init() {
        UseItemCallback.EVENT.register(new ItemClickHandler());
    }

    @Override
    public InteractionResultHolder<ItemStack> interact(Player player, Level world, InteractionHand hand) {
        ItemStack item = player.getItemInHand(hand);
        if (world.isClientSide)
            return InteractionResultHolder.pass(item);

        if (item.is(Items.RECOVERY_COMPASS)) {
            if (world.getGameRules().getRule(ModGameRules.DEATH_TP).get().allowCompass || player.hasPermissions(2)) {
                ServerPlayer sp = (ServerPlayer) player;
                ModPlayer mp = ModPlayer.get(sp);

                if (mp.teleportToLastDeathPos()) {
                    return InteractionResultHolder.success(item);
                } else {
                    return InteractionResultHolder.fail(item);
                }
            }
        }
        return InteractionResultHolder.pass(item);
    }
}
