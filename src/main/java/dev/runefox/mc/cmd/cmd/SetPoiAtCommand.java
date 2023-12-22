package dev.runefox.mc.cmd.cmd;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.runefox.mc.cmd.ModPlayer;
import dev.runefox.mc.cmd.poi.PoiManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;

public class SetPoiAtCommand extends Command {
    public SetPoiAtCommand(String name) {
        super(name);
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> make(LiteralArgumentBuilder<CommandSourceStack> cmd) {
        return cmd.requires(SetPoiCommand.PERMS)
                  .then(
                      Commands.argument("pos", BlockPosArgument.blockPos())
                              .requires(SetPoiCommand.PERMS)
                              .executes(this::main)
                              .then(
                                  Commands.argument("name", StringArgumentType.greedyString())
                                          .requires(SetPoiCommand.PERMS)
                                          .executes(this::named)
                              )
                  );
    }

    private int main(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        CommandSourceStack src = ctx.getSource();
        ServerPlayer player = src.getPlayerOrException();
        ModPlayer modPlayer = ModPlayer.get(player);

        BlockPos pos = BlockPosArgument.getBlockPos(ctx, "pos");

        PoiManager.of(player.server).setPoi(modPlayer.here().at(pos));
        src.sendSuccess(() -> message("success.main", PoiCommand.mainName(false)), true);
        return 1;
    }

    private int named(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        CommandSourceStack src = ctx.getSource();
        ServerPlayer player = src.getPlayerOrException();
        ModPlayer modPlayer = ModPlayer.get(player);

        BlockPos pos = BlockPosArgument.getBlockPos(ctx, "pos");
        String name = ctx.getArgument("name", String.class);

        PoiManager.of(player.server).setPoi(name, modPlayer.here().at(pos));
        src.sendSuccess(() -> message("success.named", PoiCommand.poiName(name)), true);
        return 1;
    }
}
