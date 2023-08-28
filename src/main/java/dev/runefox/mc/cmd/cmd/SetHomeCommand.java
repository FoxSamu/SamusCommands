package dev.runefox.mc.cmd.cmd;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.runefox.mc.cmd.ModGameRules;
import dev.runefox.mc.cmd.ModPlayer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

public class SetHomeCommand extends Command {
    public SetHomeCommand(String name) {
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

        int homes = modPlayer.homeCount();
        if (!modPlayer.hasHome()) {
            homes ++;
        }

        int maxHomes = src.getServer().getGameRules().getRule(ModGameRules.MAX_HOMES).get();
        if (maxHomes > 0 && homes > maxHomes && !src.hasPermission(2))
            throw CommandExceptions.MAX_HOMES_REACHED.create();

        modPlayer.setHome();
        src.sendSuccess(() -> message("success.main", HomeCommand.mainName(false)), false);
        return 1;
    }

    private int named(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        CommandSourceStack src = ctx.getSource();
        ServerPlayer player = src.getPlayerOrException();
        ModPlayer modPlayer = ModPlayer.get(player);

        String name = ctx.getArgument("name", String.class);


        int homes = modPlayer.homeCount();
        if (!modPlayer.hasHome(name)) {
            homes ++;
        }

        int maxHomes = src.getServer().getGameRules().getRule(ModGameRules.MAX_HOMES).get();
        if (maxHomes > 0 && homes > maxHomes && !src.hasPermission(2))
            throw CommandExceptions.MAX_HOMES_REACHED.create();

        modPlayer.setHome(name);
        src.sendSuccess(() -> message("success.named", HomeCommand.homeName(name)), false);
        return 1;
    }
}
