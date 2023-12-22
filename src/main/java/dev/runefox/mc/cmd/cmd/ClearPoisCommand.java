package dev.runefox.mc.cmd.cmd;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import dev.runefox.mc.cmd.TeleportPos;
import dev.runefox.mc.cmd.poi.PoiManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

import java.util.Map;

public class ClearPoisCommand extends Command {
    public ClearPoisCommand(String name) {
        super(name);
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> make(LiteralArgumentBuilder<CommandSourceStack> cmd) {
        return cmd.requires(SetPoiCommand.PERMS)
                  .executes(ctx -> clear(ctx, false))
                  .then(Commands.argument("include_main", BoolArgumentType.bool())
                                .requires(SetPoiCommand.PERMS)
                                .executes(ctx -> clear(ctx, BoolArgumentType.getBool(ctx, "include_main"))));
    }

    private int clear(CommandContext<CommandSourceStack> ctx, boolean all) {
        try {
            CommandSourceStack src = ctx.getSource();
            PoiManager pois = PoiManager.of(src.getServer());

            Map<String, TeleportPos> ps = pois.pois();

            boolean hasMain = pois.hasPoi();
            int toRemove = pois.poiCount() - (hasMain && !all ? 1 : 0);
            if (ps.isEmpty()) {
                src.sendSuccess(() -> message("none"), true);
                return 0;
            }

            pois.clearPois(!all);
            src.sendSuccess(() -> toRemove == 1 ? message("1") : message("x", toRemove), false);
            return 1;
        } catch (Throwable exc) {
            exc.printStackTrace();
            throw exc;
        }
    }
}
