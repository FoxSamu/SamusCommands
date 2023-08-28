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

public class HomeCommand extends Command {
    public HomeCommand(String name) {
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

        if (modPlayer.teleportToHome())
            src.sendSuccess(() -> message("success.main"), false);
        else
            throw CommandExceptions.NO_MAIN_HOME.create();
        return 1;
    }

    private int named(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        CommandSourceStack src = ctx.getSource();
        ServerPlayer player = src.getPlayerOrException();
        ModPlayer modPlayer = ModPlayer.get(player);

        String name = ctx.getArgument("name", String.class);

        if (modPlayer.teleportToHome(name))
            src.sendSuccess(() -> message("success.named", homeName(name)), false);
        else
            throw CommandExceptions.NO_NAMED_HOME.create(name);
        return 1;
    }

    public static Component deadHomeName(String name) {
        return Component.literal(name)
                        .withStyle(style -> style.withColor(ChatFormatting.RED)
                                                 .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/sethome " + name))
                                                 .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, ModCommands.message("home", "click_to_set"))));
    }

    public static Component homeName(String name) {
        return Component.literal(name)
                        .withStyle(style -> style.withColor(ChatFormatting.AQUA)
                                                 .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/home " + name))
                                                 .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, ModCommands.message("home", "click_to_teleport"))));
    }

    public static Component deadMainName(boolean caps) {
        return ModCommands.message("home", "main_home" + (caps ? ".caps" : ""))
                        .withStyle(style -> style.withColor(ChatFormatting.RED)
                                                 .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/sethome"))
                                                 .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, ModCommands.message("home", "click_to_set"))));
    }

    public static Component mainName(boolean caps) {
        return ModCommands.message("home", "main_home" + (caps ? ".caps" : ""))
                        .withStyle(style -> style.withColor(ChatFormatting.GREEN)
                                                 .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/home"))
                                                 .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, ModCommands.message("home", "click_to_teleport"))));
    }
}
