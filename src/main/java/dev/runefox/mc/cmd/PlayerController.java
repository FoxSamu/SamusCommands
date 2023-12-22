package dev.runefox.mc.cmd;

import com.mojang.authlib.GameProfile;
import dev.runefox.mc.cmd.cmd.ModCommands;
import dev.runefox.mc.cmd.net.ServerHandler;
import dev.runefox.mc.cmd.net.ServerHandlerImpl;
import dev.runefox.mc.cmd.poi.PoiManager;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

import java.util.*;
import java.util.stream.Stream;

public class PlayerController implements ModPlayer {
    private ServerPlayer player;
    private boolean synced;
    private ServerHandler netHandler;

    private final Map<String, TeleportPos> homes = new HashMap<>();

    private final Map<ServerPlayer, TeleportRequest> tpRequests = new HashMap<>();
    private final Set<UUID> tpBlackList = new HashSet<>();
    private final Set<UUID> tpWhiteList = new HashSet<>();
    private boolean tpWhitelistEnabled = false;

    private final List<String> notes = new ArrayList<>();

    public PlayerController(ServerPlayer player) {
        this.player = player;
        this.netHandler = new ServerHandlerImpl(player, this);
    }

    public ServerPlayer player() {
        return player;
    }

    public void player(ServerPlayer player) {
        this.player = player;
        this.netHandler = new ServerHandlerImpl(player, this);
    }

    private TeleportPos calcHomePos() {
        BlockPos bpos = player.blockPosition();
        ServerLevel level = player.serverLevel();
        return new TeleportPos(bpos, level.dimension().location(), player.getYRot(), player.getXRot());
    }

    @Override
    public boolean isSynced() {
        return synced;
    }

    @Override
    public void setSynced() {
        synced = true;
    }

    @Override
    public ServerHandler net() {
        return netHandler;
    }

    @Override
    public TeleportPos here() {
        return calcHomePos();
    }

    @Override
    public boolean teleport(TeleportPos pos) {
        MinecraftServer server = player.getServer();
        ServerLevel level = server.getLevel(ResourceKey.create(Registries.DIMENSION, pos.dimension()));
        if (level == null)
            return false;

        BlockPos bpos = pos.pos();

        player.teleportTo(level, bpos.getX() + 0.5, bpos.getY(), bpos.getZ() + 0.5, pos.yrot(), pos.xrot());
        return true;
    }

    @Override
    public void setHome(String name, TeleportPos pos) {
        homes.put(name, pos);
    }

    @Override
    public void setHome(String name) {
        setHome(name, calcHomePos());
    }

    @Override
    public void setHome(TeleportPos pos) {
        setHome(null, pos);
    }

    @Override
    public void setHome() {
        setHome(null, calcHomePos());
    }

    @Override
    public TeleportPos getHome(String name) {
        return homes.get(name);
    }

    @Override
    public TeleportPos getHome() {
        return getHome(null);
    }

    @Override
    public boolean teleportToHome(String name) {
        TeleportPos pos = getHome(name);
        if (pos == null)
            return false;

        return teleport(pos);
    }

    @Override
    public boolean teleportToHome() {
        return teleportToHome(null);
    }

    @Override
    public boolean hasHome(String name) {
        TeleportPos pos = getHome(name);
        return pos != null;
    }

    @Override
    public boolean hasHome() {
        return hasHome(null);
    }

    @Override
    public Map<String, TeleportPos> homes() {
        return new HashMap<>(homes);
    }

    @Override
    public void deleteHome(String name) {
        homes.remove(name);
    }

    @Override
    public void deleteHome() {
        deleteHome(null);
    }

    @Override
    public int homeCount() {
        return homes.size();
    }

    @Override
    public void clearHomes(boolean keepMain) {
        TeleportPos main = homes.get(null);
        homes.clear();

        if (keepMain && main != null) {
            homes.put(null, main);
        }
    }

    @Override
    public void teleportToSpawn() {
        ServerLevel level = player.getServer().overworld();

        BlockPos pos = level.getSharedSpawnPos();
        float angle = level.getSharedSpawnAngle();

        player.teleportTo(level, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, angle, player.getXRot());
    }

    private boolean tpRequestReceived(TeleportRequest request) {
        ServerPlayer source = request.from();
        UUID sourceId = source.getGameProfile().getId();
        if (sourceId != null) {
            if (tpWhitelistEnabled && !tpWhiteList.contains(sourceId))
                return false;

            if (tpBlackList.contains(sourceId))
                return false;
        }


        int timeout = player.server.getGameRules().getInt(ModGameRules.TPA_REQUEST_TIMEOUT);

        player.sendSystemMessage(
            ModCommands.message("tpsys", request.call() ? "call_received" : "request_received", fancyName(request.from()))
                       .withStyle(style -> style.withColor(ChatFormatting.GOLD))
                       .append("\n")
                       .append(accept(request.from())).append(" ").append(deny(request.from()))
                       .append("\n")
                       .append(ModCommands.message("tpsys", "timeout_message", fancyTime(timeout)).withStyle(style -> style.withColor(ChatFormatting.GOLD)))
        );
        tpRequests.put(request.from(), request);
        return true;
    }

    private static MutableComponent accept(ServerPlayer player) {
        return ModCommands.message("tpsys", "accept").withStyle(
            style -> style.withColor(ChatFormatting.GREEN)
                          .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, ModCommands.message("tpsys", "click_to_accept")))
                          .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpaccept " + player.getGameProfile().getName()))
        );
    }

    private static MutableComponent deny(ServerPlayer player) {
        return ModCommands.message("tpsys", "deny").withStyle(
            style -> style.withColor(ChatFormatting.RED)
                          .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, ModCommands.message("tpsys", "click_to_deny")))
                          .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpdeny " + player.getGameProfile().getName()))
        );
    }

    private static MutableComponent fancyName(ServerPlayer player) {
        return Component.literal(player.getGameProfile().getName()).withStyle(
            style -> style.withColor(ChatFormatting.YELLOW)
        );
    }

    private static MutableComponent fancyTime(int seconds) {
        return formatTime(seconds).withStyle(
            style -> style.withColor(ChatFormatting.YELLOW)
                          .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, ModCommands.message("timeout", "seconds", seconds)))
        );
    }

    private static MutableComponent formatTime(int seconds) {
        if (seconds < 60) {
            return ModCommands.message("timeout", "seconds", seconds);
        } else {
            if (seconds % 60 == 0) {
                return ModCommands.message("timeout", "minutes", seconds / 60);
            } else {
                return ModCommands.message("timeout", "minutes_seconds", seconds / 60, seconds % 60);
            }
        }
    }

    @Override
    public boolean requestTpFrom(ServerPlayer source) {
        TeleportRequest request = new TeleportRequest(source, System.currentTimeMillis(), false);
        return tpRequestReceived(request);
    }

    @Override
    public boolean callForTpTo(ServerPlayer source) {
        TeleportRequest request = new TeleportRequest(source, System.currentTimeMillis(), true);
        return tpRequestReceived(request);
    }

    private void accept(TeleportRequest request) {
        request.from().sendSystemMessage(
            ModCommands.message(request.call() ? "tpc" : "tpa", "accepted", fancyName(player))
                       .withStyle(ChatFormatting.GOLD)
        );

        if (request.call()) {
            teleport(player, request.from());
        } else {
            teleport(request.from(), player);
        }
    }

    private void deny(TeleportRequest request) {
        request.from().sendSystemMessage(
            ModCommands.message(request.call() ? "tpc" : "tpa", "denied", fancyName(player))
                       .withStyle(ChatFormatting.GOLD)
        );
    }

    /**
     * Source moves to target.
     */
    private static void teleport(ServerPlayer source, ServerPlayer target) {
        // Source moves to target

        ServerLevel lev = target.serverLevel();
        double x = target.getX();
        double y = target.getY();
        double z = target.getZ();
        float yr = target.getYRot();
        float xr = target.getXRot();

        source.teleportTo(lev, x, y, z, yr, xr);
    }

    private TeleportRequest lastRequest() {
        return tpRequests().max(TeleportRequest.COMPARATOR).orElse(null);
    }

    @Override
    public boolean acceptTpRequest(ServerPlayer source) {
        TeleportRequest request = tpRequests.remove(source);
        if (request == null) return false;
        accept(request);
        return true;
    }

    @Override
    public TeleportRequest acceptLastTpRequest() {
        TeleportRequest request = lastRequest();
        if (request != null)
            accept(request);
        return request;
    }

    @Override
    public boolean acceptTpRequests() {
        if (tpRequests.isEmpty())
            return false;

        tpRequests().sorted(TeleportRequest.COMPARATOR).forEachOrdered(this::accept);
        return true;
    }

    @Override
    public boolean denyTpRequest(ServerPlayer source) {
        TeleportRequest request = tpRequests.remove(source);
        if (request == null) return false;
        deny(request);
        return true;
    }

    @Override
    public TeleportRequest denyLastTpRequest() {
        TeleportRequest request = lastRequest();
        if (request != null)
            deny(request);
        return request;
    }

    @Override
    public boolean denyTpRequests() {
        if (tpRequests.isEmpty())
            return false;

        tpRequests().sorted(TeleportRequest.COMPARATOR).forEachOrdered(this::deny);
        return true;
    }

    @Override
    public void addToTpBlacklist(GameProfile profile) {
        tpBlackList.add(profile.getId());
    }

    @Override
    public void removeFromTpBlacklist(GameProfile profile) {
        tpBlackList.remove(profile.getId());
    }

    @Override
    public boolean isTpBlacklisted(GameProfile profile) {
        return tpBlackList.contains(profile.getId());
    }

    @Override
    public void addToTpWhitelist(GameProfile profile) {
        tpWhiteList.add(profile.getId());
    }

    @Override
    public void removeFromTpWhitelist(GameProfile profile) {
        tpWhiteList.remove(profile.getId());
    }

    @Override
    public boolean isTpWhitelisted(GameProfile profile) {
        return tpWhiteList.contains(profile.getId());
    }

    @Override
    public boolean canRequest(GameProfile profile) {
        if (tpWhitelistEnabled && !isTpWhitelisted(profile))
            return false;
        return !isTpBlacklisted(profile);
    }

    @Override
    public void useTpWhitelist(boolean on) {
        tpWhitelistEnabled = on;
    }

    @Override
    public boolean useTpWhitelist() {
        return tpWhitelistEnabled;
    }

    @Override
    public Stream<TeleportRequest> tpRequests() {
        return tpRequests.values().stream();
    }

    @Override
    public int note(String text) {
        notes.add(text);
        return notes.size();
    }

    @Override
    public int lastNoteLine() {
        return notes.size();
    }

    @Override
    public String getNote(int line) {
        if (line <= 0 || line > notes.size())
            return null;

        return notes.get(line - 1);
    }

    @Override
    public String editNote(int line, String text) {
        if (line <= 0 || line > notes.size())
            return null;

        return notes.set(line - 1, text);
    }

    @Override
    public String removeNote(int line) {
        if (line <= 0 || line > notes.size())
            return null;

        return notes.remove(line - 1);
    }

    @Override
    public List<String> removeNotes(int from, int to) {
        ArrayList<String> removed = new ArrayList<>();

        // Prevent very slow runtime if player enters huge numbers
        from = Math.max(from, 1);
        to = Math.min(to, notes.size());

        for (int i = from; i <= to; i++) {
            removed.add(notes.remove(from - 1));
        }

        return removed;
    }

    @Override
    public List<String> getNotes(int from, int to) {
        List<String> out = new ArrayList<>();

        // Prevent very slow runtime if player enters huge numbers
        from = Math.max(from, 1);
        to = Math.min(to, notes.size());

        for (int i = from; i <= to; i++) {
            out.add(notes.get(i - 1));
        }

        return out;
    }

    @Override
    public String moveNote(int from, int to) {
        if (from <= 0 || from > notes.size())
            return null;

        if (to <= 0) to = 1;
        if (to > notes.size()) to = notes.size();

        String note = notes.remove(from - 1);
        notes.add(to - 1, note);
        return note;
    }

    @Override
    public List<String> clearNotes() {
        ArrayList<String> removed = new ArrayList<>(notes);
        notes.clear();
        return removed;
    }

    @Override
    public int noteCount() {
        return notes.size();
    }

    @Override
    public List<String> notes() {
        return new ArrayList<>(notes);
    }

    @Override
    public TeleportPos lastDeath() {
        Optional<GlobalPos> death = player.getLastDeathLocation();
        if (death.isEmpty()) return null;

        GlobalPos pos = death.get();
        return new TeleportPos(
            pos.pos(),
            pos.dimension().location(),
            player.getYRot(),
            player.getXRot()
        );
    }

    @Override
    public boolean teleportToLastDeathPos() {
        TeleportPos pos = lastDeath();
        if (lastDeath() == null)
            return false;

        return teleport(pos);
    }

    @Override
    public boolean teleportToPoi(String name) {
        PoiManager poiManager = PoiManager.of(player.server);
        TeleportPos pos = poiManager.getPoi(name);
        if (pos == null) return false;
        return teleport(pos);
    }

    @Override
    public boolean teleportToPoi() {
        PoiManager poiManager = PoiManager.of(player.server);
        TeleportPos pos = poiManager.getPoi();
        if (pos == null) return false;
        return teleport(pos);
    }

    public void tick() {
        int timeoutMs = player.server.getGameRules().getInt(ModGameRules.TPA_REQUEST_TIMEOUT) * 1000;
        tpRequests.values().removeIf(req -> {
            if (System.currentTimeMillis() - req.timestamp() > timeoutMs) {
                CommandsMod.LOGGER.info("Removing timed out request from {} to {}", req.from().getGameProfile().getName(), player.getGameProfile().getName());
                return true;
            }
            return false;
        });
        tpRequests.keySet().removeIf(LivingEntity::isDeadOrDying);
        tpRequests.keySet().removeIf(ServerPlayer::hasDisconnected);
    }

    public void load(CompoundTag tag) {
        homes.clear();
        if (tag.contains("Homes")) {
            ListTag homes = tag.getList("Homes", Tag.TAG_COMPOUND);

            for (int i = 0, l = homes.size(); i < l; i++) {
                CompoundTag homeTag = homes.getCompound(i);

                String name = null;
                if (homeTag.contains("N", Tag.TAG_STRING))
                    name = homeTag.getString("N");
                int x = homeTag.getInt("X");
                int y = homeTag.getInt("Y");
                int z = homeTag.getInt("Z");
                float xr = homeTag.getFloat("RX");
                float yr = homeTag.getFloat("RY");
                String d = homeTag.getString("D");

                this.homes.put(name, new TeleportPos(new BlockPos(x, y, z), new ResourceLocation(d), yr, xr));
            }
        }

        this.notes.clear();
        ListTag notes = tag.getList("Notes", Tag.TAG_STRING);
        for (int i = 0, l = notes.size(); i < l; i++) {
            this.notes.add(notes.getString(i));
        }

        ListTag blacklist = tag.getList("TPBlacklist", Tag.TAG_INT_ARRAY);
        ListTag whitelist = tag.getList("TPWhitelist", Tag.TAG_INT_ARRAY);
        tpWhitelistEnabled = tag.getBoolean("TPWhitelistOn");

        tpBlackList.clear();
        tpWhiteList.clear();

        for (Tag item : blacklist) {
            UUID uuid = loadUuid(item);
            if (uuid != null)
                tpBlackList.add(uuid);
        }

        for (Tag item : whitelist) {
            UUID uuid = loadUuid(item);
            if (uuid != null)
                tpWhiteList.add(uuid);
        }
    }

    private UUID loadUuid(Tag tag) {
        if (!(tag instanceof IntArrayTag arr))
            return null;
        if (arr.size() != 4)
            return null;
        return UUIDUtil.uuidFromIntArray(arr.getAsIntArray());
    }

    private Tag saveUuid(UUID uuid) {
        return new IntArrayTag(UUIDUtil.uuidToIntArray(uuid));
    }

    public void save(CompoundTag tag) {
        if (!homes.isEmpty()) {
            ListTag homes = new ListTag();

            this.homes.forEach((name, pos) -> {
                CompoundTag homeTag = new CompoundTag();
                if (name != null)
                    homeTag.putString("N", name);
                homeTag.putInt("X", pos.pos().getX());
                homeTag.putInt("Y", pos.pos().getY());
                homeTag.putInt("Z", pos.pos().getZ());
                homeTag.putFloat("RX", pos.xrot());
                homeTag.putFloat("RY", pos.yrot());
                homeTag.putString("D", pos.dimension().toString());

                homes.add(homeTag);
            });

            tag.put("Homes", homes);
        }

        ListTag notes = new ListTag();
        for (String note : this.notes) {
            notes.add(StringTag.valueOf(note));
        }
        tag.put("Notes", notes);

        ListTag blacklist = new ListTag();
        ListTag whitelist = new ListTag();

        for (UUID uuid : tpBlackList)
            blacklist.add(saveUuid(uuid));
        for (UUID uuid : tpWhiteList)
            whitelist.add(saveUuid(uuid));

        tag.put("TPBlacklist", blacklist);
        tag.put("TPWhitelist", whitelist);
        tag.putBoolean("TPWhitelistOn", tpWhitelistEnabled);
    }

    public static PlayerController get(ServerPlayer player) {
        return ((ServerPlayerAccess) player).rfx_cmd_modPlayerData();
    }
}
