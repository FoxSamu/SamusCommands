package dev.runefox.mc.cmd.cmd;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.runefox.mc.cmd.ModGameRules;
import dev.runefox.mc.cmd.ModPlayer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

public class DeathCommand extends Command {
    public DeathCommand(String name) {
        super(name);
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> make(LiteralArgumentBuilder<CommandSourceStack> cmd) {
        return cmd.requires(
                      ModCommands.requireGameRule(ModGameRules.DEATH_TP, mode -> mode.get().allowCommand)
                                 .or(src -> src.hasPermission(2))
                                 .and(CommandSourceStack::isPlayer)
                  )
                  .executes(this::death);
    }

    private int death(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        CommandSourceStack src = ctx.getSource();
        ServerPlayer player = src.getPlayerOrException();
        if (player.getLastDeathLocation().isEmpty()) {
            src.sendFailure(message("no_death"));
            return 0;
        }

        ModPlayer modPlayer = ModPlayer.get(player);

        if (modPlayer.teleportToLastDeathPos()) {
            src.sendSuccess(() -> message("ok"), false);
            return 1;
        } else {
            src.sendFailure(message("unknown_dimension"));
            return 0;
        }
    }
}
