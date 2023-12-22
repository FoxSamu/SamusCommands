package dev.runefox.mc.cmd;

import dev.runefox.mc.cmd.poi.PoiManager;
import net.minecraft.world.level.storage.LevelStorageSource;

public interface MinecraftServerAccess {
    PoiManager rfx_cmd_poiManager();
    LevelStorageSource.LevelStorageAccess rfx_cmd_storageSource();
}
