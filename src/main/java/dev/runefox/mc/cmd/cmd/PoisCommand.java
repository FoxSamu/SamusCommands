package dev.runefox.mc.cmd.cmd;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import dev.runefox.mc.cmd.TeleportPos;
import dev.runefox.mc.cmd.poi.PoiManager;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.GameRules;

import java.util.Map;

public class PoisCommand extends Command {
    public PoisCommand(String name) {
        super(name);
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> make(LiteralArgumentBuilder<CommandSourceStack> cmd) {
        return cmd.requires(GetPoiCommand.PERMS)
                  .executes(this::main);
    }

    private int main(CommandContext<CommandSourceStack> ctx) {
        try {
            CommandSourceStack src = ctx.getSource();
            PoiManager pois = PoiManager.of(src.getServer());

            boolean reduced = src.getServer().getGameRules().getBoolean(GameRules.RULE_REDUCEDDEBUGINFO) && !src.hasPermission(2);

            Map<String, TeleportPos> ps = pois.pois();
            if (pois.poiCount() == 0) {
                src.sendSuccess(() -> message("none"), false);
                return 0;
            }

            boolean hasMain = pois.hasPoi();
            int named = ps.size();

            if (hasMain) {
                TeleportPos main = pois.getPoi();
                if (reduced)
                    src.sendSuccess(() -> message("main.reduced", PoiCommand.mainName(false)), false);
                else
                    src.sendSuccess(() -> message("main", PoiCommand.mainName(false), makePosText(main, src.getLevel())), false);
            }

            if (named > 0) {
                if (named == 1)
                    src.sendSuccess(() -> message("named.1"), false);
                else
                    src.sendSuccess(() -> message("named.x", named), false);

                ps.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEachOrdered(entry -> {
                    if (reduced)
                        src.sendSuccess(() -> message("named.reduced", PoiCommand.poiName(entry.getKey())), false);
                    else
                        src.sendSuccess(() -> message("named.list", PoiCommand.poiName(entry.getKey()), makePosText(entry.getValue(), src.getLevel())), false);
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
            component = component.append(ModCommands.message("poi", "dimension", pos.dimension()));
        return component.withStyle(style -> style.withColor(ChatFormatting.GREEN));
    }
}
