package dev.runefox.mc.cmd.cmd;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.runefox.mc.cmd.ModGameRules;
import dev.runefox.mc.cmd.ModPlayer;
import dev.runefox.mc.cmd.poi.PoiManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.Predicate;

public class SetPoiCommand extends Command {
    public SetPoiCommand(String name) {
        super(name);
    }

    static final Predicate<CommandSourceStack> PERMS = ModCommands.requireTrue(ModGameRules.ALL_CAN_SET_POI)
                                                                  .and(ModCommands.requireTrue(ModGameRules.ALLOW_POI))
                                                                  .or(src -> src.hasPermission(2));

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> make(LiteralArgumentBuilder<CommandSourceStack> cmd) {
        return cmd.requires(PERMS.and(CommandSourceStack::isPlayer))
                  .executes(this::main)
                  .then(
                      Commands.argument("name", StringArgumentType.greedyString())
                              .requires(PERMS.and(CommandSourceStack::isPlayer))
                              .executes(this::named)
                  );
    }

    private int main(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        CommandSourceStack src = ctx.getSource();
        ServerPlayer player = src.getPlayerOrException();
        ModPlayer modPlayer = ModPlayer.get(player);

        PoiManager.of(player.server).setPoi(modPlayer.here());
        src.sendSuccess(() -> message("success.main", PoiCommand.mainName(false)), true);
        return 1;
    }

    private int named(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        CommandSourceStack src = ctx.getSource();
        ServerPlayer player = src.getPlayerOrException();
        ModPlayer modPlayer = ModPlayer.get(player);

        String name = ctx.getArgument("name", String.class);

        PoiManager.of(player.server).setPoi(name, modPlayer.here());
        src.sendSuccess(() -> message("success.named", PoiCommand.poiName(name)), true);
        return 1;
    }
}
