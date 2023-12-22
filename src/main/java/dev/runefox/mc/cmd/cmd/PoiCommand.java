package dev.runefox.mc.cmd.cmd;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.runefox.mc.cmd.ModGameRules;
import dev.runefox.mc.cmd.ModPlayer;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.server.level.ServerPlayer;

public class PoiCommand extends Command {
    public PoiCommand(String name) {
        super(name);
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> make(LiteralArgumentBuilder<CommandSourceStack> cmd) {
        return cmd.requires(ModCommands.requireTrue(ModGameRules.ALLOW_POI).or(src -> src.hasPermission(2)))
                  .executes(this::main)
                  .then(
                      Commands.argument("name", StringArgumentType.greedyString())
                              .requires(ModCommands.requireTrue(ModGameRules.ALLOW_POI).or(src -> src.hasPermission(2)))
                              .executes(this::named)
                  );
    }

    private int main(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        CommandSourceStack src = ctx.getSource();
        ServerPlayer player = src.getPlayerOrException();
        ModPlayer modPlayer = ModPlayer.get(player);

        if (modPlayer.teleportToPoi())
            src.sendSuccess(() -> message("success.main"), false);
        else
            throw CommandExceptions.NO_MAIN_POI.create();
        return 1;
    }

    private int named(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        CommandSourceStack src = ctx.getSource();
        ServerPlayer player = src.getPlayerOrException();
        ModPlayer modPlayer = ModPlayer.get(player);

        String name = ctx.getArgument("name", String.class);

        if (modPlayer.teleportToPoi(name))
            src.sendSuccess(() -> message("success.named", poiName(name)), false);
        else
            throw CommandExceptions.NO_NAMED_POI.create(name);
        return 1;
    }

    public static Component deadPoiName(String name) {
        return Component.literal(name)
                        .withStyle(style -> style.withColor(ChatFormatting.RED)
                                                 .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/setpoi " + name))
                                                 .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, ModCommands.message("poi", "click_to_set"))));
    }

    public static Component poiName(String name) {
        return Component.literal(name)
                        .withStyle(style -> style.withColor(ChatFormatting.AQUA)
                                                 .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/poi " + name))
                                                 .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, ModCommands.message("poi", "click_to_teleport"))));
    }

    public static Component deadMainName(boolean caps) {
        return ModCommands.message("poi", "main_poi" + (caps ? ".caps" : ""))
                          .withStyle(style -> style.withColor(ChatFormatting.RED)
                                                   .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/setpoi"))
                                                   .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, ModCommands.message("poi", "click_to_set"))));
    }

    public static Component mainName(boolean caps) {
        return ModCommands.message("poi", "main_poi" + (caps ? ".caps" : ""))
                          .withStyle(style -> style.withColor(ChatFormatting.GREEN)
                                                   .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/poi"))
                                                   .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, ModCommands.message("poi", "click_to_teleport"))));
    }
}
