package dev.runefox.mc.cmd.cmd;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.runefox.mc.cmd.ModGameRules;
import dev.runefox.mc.cmd.ModPlayer;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;

public class TpBlacklistCommand extends Command {
    private final boolean whitelist;

    public TpBlacklistCommand(String name, boolean whitelist) {
        super(name);
        this.whitelist = whitelist;
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> make(LiteralArgumentBuilder<CommandSourceStack> cmd) {
        var add = Commands.literal("add").then(
            Commands.argument("profile", GameProfileArgument.gameProfile())
                    .requires(ModCommands.requireTrue(ModGameRules.ALLOW_TPA))
                    .executes(this::add)
        );
        var remove = Commands.literal("remove").then(
            Commands.argument("profile", GameProfileArgument.gameProfile())
                    .requires(ModCommands.requireTrue(ModGameRules.ALLOW_TPA))
                    .executes(this::remove)
        );

        cmd = cmd.requires(ModCommands.requireTrue(ModGameRules.ALLOW_TPA))
                 .then(add)
                 .then(remove);

        if (whitelist) {
            var enable = Commands.literal("enable")
                                 .requires(ModCommands.requireTrue(ModGameRules.ALLOW_TPA))
                                 .executes(ctx -> enable(ctx, 0));
            var disable = Commands.literal("disable")
                                  .requires(ModCommands.requireTrue(ModGameRules.ALLOW_TPA))
                                  .executes(ctx -> enable(ctx, 1));
            var toggle = Commands.literal("toggle")
                                 .requires(ModCommands.requireTrue(ModGameRules.ALLOW_TPA))
                                 .executes(ctx -> enable(ctx, 2));

            cmd = cmd.then(enable)
                     .then(disable)
                     .then(toggle);
        }

        return cmd;
    }

    private int enable(CommandContext<CommandSourceStack> ctx, int mode) throws CommandSyntaxException {
        CommandSourceStack src = ctx.getSource();
        ServerPlayer player = src.getPlayer();
        ModPlayer controller = ModPlayer.get(player);

        if (mode == 0) {
            if (controller.useTpWhitelist()) {
                src.sendSuccess(() -> message("already_enabled").withStyle(ChatFormatting.YELLOW), false);
                return 0;
            } else {
                controller.useTpWhitelist(true);
                src.sendSuccess(() -> message("enabled"), false);
                return 1;
            }
        }
        if (mode == 1) {
            if (!controller.useTpWhitelist()) {
                src.sendSuccess(() -> message("already_disabled").withStyle(ChatFormatting.YELLOW), false);
                return 0;
            } else {
                controller.useTpWhitelist(false);
                src.sendSuccess(() -> message("disabled"), false);
                return 1;
            }
        }
        if (mode == 2) {
            if (!controller.useTpWhitelist()) {
                controller.useTpWhitelist(true);
                src.sendSuccess(() -> message("enabled"), false);
            } else {
                controller.useTpWhitelist(false);
                src.sendSuccess(() -> message("disabled"), false);
            }
            return 1;
        }

        throw new AssertionError();
    }

    private int add(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        CommandSourceStack src = ctx.getSource();
        ServerPlayer player = src.getPlayer();
        ModPlayer controller = ModPlayer.get(player);

        Collection<GameProfile> profiles = GameProfileArgument.getGameProfiles(ctx, "profile");

        int n = 0;
        for (GameProfile profile : profiles) {
            if (whitelist) {
                if (!controller.isTpWhitelisted(profile)) {
                    n++;
                    controller.addToTpWhitelist(profile);

                    src.sendSuccess(() -> message("added", fancyName(profile)), false);
                }
            } else {
                if (!controller.isTpBlacklisted(profile)) {
                    n++;
                    controller.addToTpBlacklist(profile);

                    src.sendSuccess(() -> message("added", fancyName(profile)), false);
                }
            }
        }

        return n;
    }

    private static Component fancyName(GameProfile profile) {
        return Component.literal(profile.getName()).withStyle(ChatFormatting.YELLOW);
    }

    private int remove(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        CommandSourceStack src = ctx.getSource();
        ServerPlayer player = src.getPlayer();
        ModPlayer controller = ModPlayer.get(player);

        Collection<GameProfile> profiles = GameProfileArgument.getGameProfiles(ctx, "profile");

        int n = 0;
        for (GameProfile profile : profiles) {
            if (whitelist) {
                if (controller.isTpWhitelisted(profile)) {
                    n++;
                    controller.removeFromTpWhitelist(profile);

                    src.sendSuccess(() -> message("removed", fancyName(profile)), false);
                }
            } else {
                if (controller.isTpBlacklisted(profile)) {
                    n++;
                    controller.removeFromTpBlacklist(profile);

                    src.sendSuccess(() -> message("removed", fancyName(profile)), false);
                }
            }
        }

        return n;
    }
}
