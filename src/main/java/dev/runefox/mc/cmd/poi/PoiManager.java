package dev.runefox.mc.cmd.poi;

import dev.runefox.mc.cmd.CommandsMod;
import dev.runefox.mc.cmd.MinecraftServerAccess;
import dev.runefox.mc.cmd.TeleportPos;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class PoiManager {
    private final MinecraftServer server;

    private TeleportPos main;
    private final Map<String, TeleportPos> named = new HashMap<>();

    public PoiManager(MinecraftServer server) {
        this.server = server;
    }

    private static final boolean DEBUG_SAVE_SNBT = false;

    public void save(LevelStorageSource.LevelStorageAccess storageSource) {
        Path path = storageSource.getLevelPath(LevelResource.ROOT).resolve("rfx-poi.dat");
        try (DataOutputStream dos = new DataOutputStream(Files.newOutputStream(path))) {
            CompoundTag tag = new CompoundTag();
            toTag(tag);
            NbtIo.write(tag, dos);
            CommandsMod.LOGGER.info("Saved poi data");
        } catch (IOException exc) {
            CommandsMod.LOGGER.error("Failed to write POI data", exc);
        }

        if (DEBUG_SAVE_SNBT) {
            Path spath = storageSource.getLevelPath(LevelResource.ROOT).resolve("rfx-poi.snbt");
            try (Writer wr = Files.newBufferedWriter(spath)) {
                CompoundTag tag = new CompoundTag();
                toTag(tag);
                SnbtPrinterTagVisitor visitor = new SnbtPrinterTagVisitor();
                wr.append(visitor.visit(tag));

                CommandsMod.LOGGER.info("Saved poi snbt");
            } catch (IOException exc) {
                CommandsMod.LOGGER.error("Failed to write POI data", exc);
            }
        }
    }

    public void load(LevelStorageSource.LevelStorageAccess storageSource) {
        Path path = storageSource.getLevelPath(LevelResource.ROOT).resolve("rfx-poi.dat");
        try (DataInputStream dos = new DataInputStream(Files.newInputStream(path))) {
            CompoundTag tag = NbtIo.read(dos);
            fromTag(tag);
            CommandsMod.LOGGER.info("Loaded poi data");
        } catch (FileNotFoundException exc) {
            CommandsMod.LOGGER.info("POI data not found, creating...");
        } catch (IOException exc) {
            CommandsMod.LOGGER.error("Failed to write POI data", exc);
        }
    }

    public void fromTag(CompoundTag tag) {
        if (tag.contains("Pois")) {
            ListTag pois = tag.getList("Pois", Tag.TAG_COMPOUND);
            main = null;
            named.clear();

            for (int i = 0, l = pois.size(); i < l; i++) {
                CompoundTag poiTag = pois.getCompound(i);

                String name = null;
                if (poiTag.contains("N", Tag.TAG_STRING))
                    name = poiTag.getString("N");
                int x = poiTag.getInt("X");
                int y = poiTag.getInt("Y");
                int z = poiTag.getInt("Z");
                float xr = poiTag.getFloat("RX");
                float yr = poiTag.getFloat("RY");
                String d = poiTag.getString("D");

                TeleportPos pos = new TeleportPos(new BlockPos(x, y, z), new ResourceLocation(d), yr, xr);

                if (name != null)
                    named.put(name, pos);
                else
                    main = pos;
            }
        }
    }

    public void toTag(CompoundTag tag) {
        ListTag pois = new ListTag();

        named.forEach((name, pos) -> {
            CompoundTag poiTag = new CompoundTag();
            poiTag.putString("N", name);
            poiTag.putInt("X", pos.pos().getX());
            poiTag.putInt("Y", pos.pos().getY());
            poiTag.putInt("Z", pos.pos().getZ());
            poiTag.putFloat("RX", pos.xrot());
            poiTag.putFloat("RY", pos.yrot());
            poiTag.putString("D", pos.dimension().toString());

            pois.add(poiTag);
        });


        if (this.main != null) {
            CompoundTag poiTag = new CompoundTag();
            poiTag.putInt("X", main.pos().getX());
            poiTag.putInt("Y", main.pos().getY());
            poiTag.putInt("Z", main.pos().getZ());
            poiTag.putFloat("RX", main.xrot());
            poiTag.putFloat("RY", main.yrot());
            poiTag.putString("D", main.dimension().toString());
            pois.add(poiTag);
        }


        tag.put("Pois", pois);
    }

    public void setPoi(String name, TeleportPos pos) {
        named.put(name, pos);
    }

    public void setPoi(TeleportPos pos) {
        main = pos;
    }

    public TeleportPos getPoi(String name) {
        return named.get(name);
    }

    public TeleportPos getPoi() {
        return main;
    }

    public void deletePoi(String name) {
        named.remove(name);
    }

    public void deletePoi() {
        main = null;
    }

    public boolean hasPoi(String name) {
        return named.containsKey(name);
    }

    public boolean hasPoi() {
        return main != null;
    }

    public Map<String, TeleportPos> pois() {
        return new HashMap<>(named);
    }

    public int poiCount() {
        return named.size() + (main != null ? 1 : 0);
    }

    public void clearPois(boolean keepMain) {
        if (!keepMain)
            main = null;
        named.clear();
    }

    public static PoiManager of(MinecraftServer server) {
        return ((MinecraftServerAccess) server).rfx_cmd_poiManager();
    }

    public static void load(MinecraftServer server) {
        of(server).load(((MinecraftServerAccess) server).rfx_cmd_storageSource());
    }

    public static void save(MinecraftServer server) {
        of(server).save(((MinecraftServerAccess) server).rfx_cmd_storageSource());
    }

    public static void init() {
        ServerLifecycleEvents.SERVER_STARTED.register(PoiManager::load);
        ServerLifecycleEvents.SERVER_STOPPING.register(PoiManager::save);
    }
}
