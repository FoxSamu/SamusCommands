package dev.runefox.mc.cmd.cmd;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.runefox.mc.cmd.ModGameRules;
import dev.runefox.mc.cmd.ModPlayer;
import dev.runefox.mc.cmd.TeleportRequest;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;

public class TpAcceptCommand extends Command {
    private final boolean deny;

    public TpAcceptCommand(String name, boolean deny) {
        super(name);
        this.deny = deny;
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> make(LiteralArgumentBuilder<CommandSourceStack> cmd) {
        var playerArg = Commands.argument("player", EntityArgument.player())
                                .requires(ModCommands.requireTrue(ModGameRules.ALLOW_TPA))
                                .executes(this::process);
        var all = Commands.literal("*")
                          .requires(ModCommands.requireTrue(ModGameRules.ALLOW_TPA))
                          .executes(this::processAll);
        return cmd.then(playerArg)
                  .then(all)
                  .requires(ModCommands.requireTrue(ModGameRules.ALLOW_TPA))
                  .executes(this::processLast);
    }

    private int processLast(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        CommandSourceStack src = ctx.getSource();

        ServerPlayer source = src.getPlayerOrException();

        TeleportRequest result;
        if (deny)
            result = ModPlayer.get(source).denyLastTpRequest();
        else
            result = ModPlayer.get(source).acceptLastTpRequest();

        if (result == null) {
            src.sendFailure(message("no_requests"));
            return 0;
        }

        src.sendSuccess(() -> message("success.specific", fancyName(result.from())).withStyle(ChatFormatting.GOLD), false);
        return 1;
    }

    private int processAll(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        CommandSourceStack src = ctx.getSource();

        ServerPlayer source = src.getPlayerOrException();

        boolean result;
        if (deny)
            result = ModPlayer.get(source).denyTpRequests();
        else
            result = ModPlayer.get(source).acceptTpRequests();

        if (!result) {
            src.sendFailure(message("no_requests"));
            return 0;
        }

        src.sendSuccess(() -> message("success.all").withStyle(ChatFormatting.GOLD), false);
        return 1;
    }

    private int process(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        CommandSourceStack src = ctx.getSource();

        ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
        ServerPlayer source = src.getPlayerOrException();

        boolean result;
        if (deny)
            result = ModPlayer.get(source).denyTpRequest(player);
        else
            result = ModPlayer.get(source).acceptTpRequest(player);

        if (!result) {
            src.sendFailure(message("no_such_request", player.getGameProfile().getName()));
            return 0;
        }

        src.sendSuccess(() -> message("success.specific", fancyName(player)).withStyle(ChatFormatting.GOLD), false);
        return 1;
    }

    private static MutableComponent fancyName(ServerPlayer player) {
        return Component.literal(player.getGameProfile().getName()).withStyle(
            style -> style.withColor(ChatFormatting.YELLOW)
        );
    }
}
