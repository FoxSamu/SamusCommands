package dev.runefox.mc.cmd.mixin;

import com.mojang.authlib.GameProfile;
import dev.runefox.mc.cmd.PlayerController;
import dev.runefox.mc.cmd.ServerPlayerAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class PlayerMixin extends Player implements ServerPlayerAccess {
    private PlayerController data;

    public PlayerMixin(Level level, BlockPos blockPos, float f, GameProfile gameProfile) {
        super(level, blockPos, f, gameProfile);
    }

    @Inject(method = "addAdditionalSaveData", at = @At("RETURN"))
    private void addModData(CompoundTag tag, CallbackInfo cbi) {
        if (data != null) {
            CompoundTag rfxCmdData = new CompoundTag();
            data.save(rfxCmdData);
            tag.put("rfx-cmd", rfxCmdData);
        }
    }

    @Inject(method = "readAdditionalSaveData", at = @At("RETURN"))
    private void readModData(CompoundTag tag, CallbackInfo cbi) {
        if (tag.contains("rfx-cmd", Tag.TAG_COMPOUND)) {
            CompoundTag rfxCmdData = tag.getCompound("rfx-cmd");
            data = new PlayerController(ServerPlayer.class.cast(this));
            data.load(rfxCmdData);
        }
    }

    @Inject(method = "restoreFrom", at = @At("RETURN"))
    private void restoreModData(ServerPlayer player, boolean dunnoWhatThisIs, CallbackInfo cbi) {
        data = PlayerController.get(player);
        data.player(ServerPlayer.class.cast(this));
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void tickModData(CallbackInfo cbi) {
        if (data != null)
            data.tick();
    }

    @Override
    public PlayerController rfx_cmd_modPlayerData() {
        if (data == null)
            data = new PlayerController(ServerPlayer.class.cast(this));
        return data;
    }
}
