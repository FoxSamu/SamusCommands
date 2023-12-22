package dev.runefox.mc.cmd.mixin;

import dev.runefox.mc.cmd.MinecraftServerAccess;
import dev.runefox.mc.cmd.poi.PoiManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SuppressWarnings("DataFlowIssue")
@Mixin(MinecraftServer.class)
public class ServerMixin implements MinecraftServerAccess {
    private final PoiManager poiManager = new PoiManager((MinecraftServer) (Object) this);

    @Override
    public PoiManager rfx_cmd_poiManager() {
        return poiManager;
    }

    @Shadow
    private LevelStorageSource.LevelStorageAccess storageSource;

    @Override
    public LevelStorageSource.LevelStorageAccess rfx_cmd_storageSource() {
        return storageSource;
    }

    @Inject(method = "saveEverything", at = @At("HEAD"))
    private void onSaveEverything(boolean b1, boolean b2, boolean b3, CallbackInfoReturnable<Boolean> ret) {
        poiManager.save(storageSource);
    }
}
