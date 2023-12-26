package dev.runefox.mc.cmd.mixin;

import dev.runefox.mc.cmd.ServerLevelAccess;
import dev.runefox.mc.cmd.pregen.Pregenerator;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ServerLevel.class)
public class LevelMixin implements ServerLevelAccess {
    private Pregenerator rfx_cmd_pregenerator;

    @Override
    public Pregenerator rfx_cmd_pregenerator() {
        return rfx_cmd_pregenerator;
    }

    @Override
    public void rfx_cmd_pregenerator(Pregenerator pregen) {
        this.rfx_cmd_pregenerator = pregen;
    }


}
