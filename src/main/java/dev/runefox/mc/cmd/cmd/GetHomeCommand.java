package dev.runefox.mc.cmd.cmd;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.runefox.mc.cmd.ModGameRules;
import dev.runefox.mc.cmd.ModPlayer;
import dev.runefox.mc.cmd.TeleportPos;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameRules;

public class GetHomeCommand extends Command {
    public GetHomeCommand(String name) {
        super(name);
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> make(LiteralArgumentBuilder<CommandSourceStack> cmd) {
        return cmd.requires(ModCommands.requireTrue(ModGameRules.ALLOW_HOME_TP).or(src -> src.hasPermission(2)))
                  .executes(this::main)
                  .then(
                      Commands.argument("name", StringArgumentType.greedyString())
                              .requires(ModCommands.requireTrue(ModGameRules.ALLOW_HOME_TP).or(src -> src.hasPermission(2)))
                              .executes(this::named)
                  );
    }

    private int main(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        CommandSourceStack src = ctx.getSource();
        ServerPlayer player = src.getPlayerOrException();
        ModPlayer modPlayer = ModPlayer.get(player);

        if (!modPlayer.hasHome()) {
            throw CommandExceptions.GET_NO_MAIN_HOME.create();
        }

        boolean reduced = src.getServer().getGameRules().getBoolean(GameRules.RULE_REDUCEDDEBUGINFO) && !src.hasPermission(2);

        TeleportPos pos = modPlayer.getHome();
        if (reduced)
            src.sendSuccess(() -> message("reduced.main", HomeCommand.mainName(false)), false);
        else
            src.sendSuccess(() -> message("success.main", HomeCommand.mainName(false), HomesCommand.makePosText(pos, src.getLevel())), false);
        return 1;
    }

    private int named(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        CommandSourceStack src = ctx.getSource();
        ServerPlayer player = src.getPlayerOrException();
        ModPlayer modPlayer = ModPlayer.get(player);

        String name = ctx.getArgument("name", String.class);

        if (!modPlayer.hasHome(name)) {
            throw CommandExceptions.GET_NO_NAMED_HOME.create(name);
        }

        boolean reduced = src.getServer().getGameRules().getBoolean(GameRules.RULE_REDUCEDDEBUGINFO) && !src.hasPermission(2);

        TeleportPos pos = modPlayer.getHome(name);
        if (reduced)
            src.sendSuccess(() -> message("reduced.named", HomeCommand.homeName(name)), false);
        else
            src.sendSuccess(() -> message("success.named", HomeCommand.homeName(name), HomesCommand.makePosText(pos, src.getLevel())), false);
        return 1;
    }
}
