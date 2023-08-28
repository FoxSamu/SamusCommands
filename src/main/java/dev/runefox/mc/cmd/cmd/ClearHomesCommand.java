package dev.runefox.mc.cmd.cmd;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.runefox.mc.cmd.ModGameRules;
import dev.runefox.mc.cmd.ModPlayer;
import dev.runefox.mc.cmd.TeleportPos;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

import java.util.Map;

public class ClearHomesCommand extends Command {
    public ClearHomesCommand(String name) {
        super(name);
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> make(LiteralArgumentBuilder<CommandSourceStack> cmd) {
        return cmd.requires(ModCommands.requireTrue(ModGameRules.ALLOW_HOME_TP).or(src -> src.hasPermission(2)))
                  .executes(ctx -> clear(ctx, false))
                  .then(Commands.argument("include_main", BoolArgumentType.bool())
                                .requires(ModCommands.requireTrue(ModGameRules.ALLOW_HOME_TP).or(src -> src.hasPermission(2)))
                                .executes(ctx -> clear(ctx, BoolArgumentType.getBool(ctx, "include_main"))));
    }

    private int clear(CommandContext<CommandSourceStack> ctx, boolean all) throws CommandSyntaxException {
        try {
            CommandSourceStack src = ctx.getSource();
            ServerPlayer player = src.getPlayerOrException();
            ModPlayer modPlayer = ModPlayer.get(player);

            Map<String, TeleportPos> homes = modPlayer.homes();

            boolean hasMain = modPlayer.hasHome();
            int toRemove = modPlayer.homeCount() - (hasMain && !all ? 1 : 0);
            if (homes.isEmpty()) {
                src.sendSuccess(() -> message("none"), false);
                return 0;
            }

            modPlayer.clearHomes(!all);
            src.sendSuccess(() -> toRemove == 1 ? message("1") : message("x", toRemove), false);
            return 1;
        } catch (Throwable exc) {
            exc.printStackTrace();
            throw exc;
        }
    }
}
