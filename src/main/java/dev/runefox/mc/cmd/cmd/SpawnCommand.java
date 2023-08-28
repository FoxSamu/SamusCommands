package dev.runefox.mc.cmd.cmd;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.runefox.mc.cmd.ModGameRules;
import dev.runefox.mc.cmd.ModPlayer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

public class SpawnCommand extends Command {
    public SpawnCommand(String name) {
        super(name);
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> make(LiteralArgumentBuilder<CommandSourceStack> cmd) {
        return cmd.requires(ModCommands.requireTrue(ModGameRules.ALLOW_TPA).or(src -> src.hasPermission(2)))
                  .executes(this::spawn);
    }

    private int spawn(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        try {
            CommandSourceStack src = ctx.getSource();
            ServerPlayer player = src.getPlayerOrException();
            ModPlayer modPlayer = ModPlayer.get(player);

            modPlayer.teleportToSpawn();
            src.sendSuccess(() -> message("success"), false);
            return 1;
        } catch (Throwable exc) {
            exc.printStackTrace();
            throw exc;
        }
    }
}
