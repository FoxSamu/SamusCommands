package dev.runefox.mc.cmd.cmd;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.runefox.mc.cmd.ModGameRules;
import dev.runefox.mc.cmd.ModPlayer;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;

public class TpaCommand extends Command {
    private final boolean call;
    private final String argName;

    public TpaCommand(String name, boolean call) {
        super(name);
        this.call = call;
        this.argName = call ? "target" : "destination";
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> make(LiteralArgumentBuilder<CommandSourceStack> cmd) {
        var playerArg = Commands.argument(argName, EntityArgument.player())
                                .requires(ModCommands.requireTrue(ModGameRules.ALLOW_TPA)
                                                     .and(ModCommands.requireGameRule(ModGameRules.TPA_MODE, rule -> rule.get().allowed(call))))
                                .executes(this::doRequest);
        return cmd.then(playerArg);
    }

    private int doRequest(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        CommandSourceStack src = ctx.getSource();

        ServerPlayer target = EntityArgument.getPlayer(ctx, argName);
        ServerPlayer source = src.getPlayerOrException();

        if (source == target) {
            src.sendFailure(message("yourself"));
            return 0;
        }

        if (src.getServer().getGameRules().getRule(ModGameRules.TPA_REQUIRES_SAME_TEAM).get()) {
            boolean same = false;
            if (source.getTeam() == null)
                same = target.getTeam() == null;
            else if (source.getTeam().isAlliedTo(target.getTeam()))
                same = true;

            if (!same) {
                src.sendFailure(message("wrong_team", target.getGameProfile().getName()));
                return 0;
            }
        }

        boolean result;
        if (call)
            result = ModPlayer.get(target).callForTpTo(source);
        else
            result = ModPlayer.get(target).requestTpFrom(source);

        if (!result) {
            src.sendFailure(message("blocked", target.getGameProfile().getName()));
            return 0;
        }

        src.sendSuccess(() -> message("sent", fancyName(target)).withStyle(ChatFormatting.GOLD), false);
        return 1;
    }

    private static MutableComponent fancyName(ServerPlayer player) {
        return Component.literal(player.getGameProfile().getName()).withStyle(
            style -> style.withColor(ChatFormatting.YELLOW)
        );
    }
}
