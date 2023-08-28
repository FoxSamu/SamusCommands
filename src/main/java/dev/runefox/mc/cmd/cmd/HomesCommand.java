package dev.runefox.mc.cmd.cmd;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.runefox.mc.cmd.ModGameRules;
import dev.runefox.mc.cmd.ModPlayer;
import dev.runefox.mc.cmd.TeleportPos;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameRules;

import java.util.Map;

public class HomesCommand extends Command {
    public HomesCommand(String name) {
        super(name);
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> make(LiteralArgumentBuilder<CommandSourceStack> cmd) {
        return cmd.requires(ModCommands.requireTrue(ModGameRules.ALLOW_HOME_TP).or(src -> src.hasPermission(2)))
                  .executes(this::main);
    }

    private int main(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        try {
            CommandSourceStack src = ctx.getSource();
            ServerPlayer player = src.getPlayerOrException();
            ModPlayer modPlayer = ModPlayer.get(player);

            boolean reduced = src.getServer().getGameRules().getBoolean(GameRules.RULE_REDUCEDDEBUGINFO) && !src.hasPermission(2);

            Map<String, TeleportPos> homes = modPlayer.homes();
            if (homes.isEmpty()) {
                src.sendSuccess(() -> message("none"), false);
                return 0;
            }

            boolean hasMain = homes.containsKey(null);
            int named = homes.size() - (hasMain ? 1 : 0);

            if (hasMain) {
                TeleportPos main = homes.get(null);
                if (reduced)
                    src.sendSuccess(() -> message("main.reduced", HomeCommand.mainName(false)), false);
                else
                    src.sendSuccess(() -> message("main", HomeCommand.mainName(false), makePosText(main, src.getLevel())), false);
            }

            if (named > 0) {
                if (named == 1)
                    src.sendSuccess(() -> message("named.1"), false);
                else
                    src.sendSuccess(() -> message("named.x", named), false);

                homes.entrySet().stream().filter(e -> e.getKey() != null).sorted(Map.Entry.comparingByKey()).forEachOrdered(entry -> {
                    if (reduced)
                        src.sendSuccess(() -> message("named.reduced", HomeCommand.homeName(entry.getKey())), false);
                    else
                        src.sendSuccess(() -> message("named.list", HomeCommand.homeName(entry.getKey()), makePosText(entry.getValue(), src.getLevel())), false);
                });
            }
            return 1;
        } catch (Throwable exc) {
            exc.printStackTrace();
            throw exc;
        }
    }

    public static Component makePosText(TeleportPos pos, ServerLevel level) {
        MutableComponent component = ComponentUtils.wrapInSquareBrackets(Component.translatable("chat.coordinates", pos.pos().getX(), pos.pos().getY(), pos.pos().getZ()));
        if (!level.dimension().location().equals(pos.dimension()))
            component = component.append(ModCommands.message("home", "dimension", pos.dimension()));
        return component.withStyle(style -> style.withColor(ChatFormatting.GREEN));
    }
}
