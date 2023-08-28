package dev.runefox.mc.cmd.net;

import dev.runefox.mc.cmd.CommandsMod;
import dev.runefox.mc.cmd.ModGameRules;
import dev.runefox.mc.cmd.ModPlayer;
import dev.runefox.mc.cmd.cmd.CommandExceptions;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class ServerHandlerImpl implements ServerHandler {
    private final ServerPlayer player;
    private final ModPlayer controller;

    public ServerHandlerImpl(ServerPlayer player, ModPlayer controller) {
        this.player = player;
        this.controller = controller;
    }

    private void onServerThread(Runnable task) {
        player.server.execute(task);
    }

    private void clientMessage(Component message) {
        Network.sendClient(player, new ClientMessagePacket(message));
    }

    @Override
    public ServerPlayer player() {
        return player;
    }

    @Override
    public ModPlayer controller() {
        return controller;
    }

    @Override
    public void pong() {
        onServerThread(() -> {
            controller.setSynced();
            CommandsMod.LOGGER.info("{} has rfx-cmd and successfully synced", player.getGameProfile().getName());
        });
    }

    @Override
    public void goHome() {
        onServerThread(() -> {
            if (!player.server.getGameRules().getBoolean(ModGameRules.ALLOW_HOME_TP) && !player.hasPermissions(2)) {
                clientMessage(Component.translatable("message.rfx-cmd.homes_not_allowed"));
                return;
            }

            if (!controller.teleportToHome()) {
                clientMessage(Component.translatable("message.rfx-cmd.home_not_set"));
            } else {
                clientMessage(Component.translatable("message.rfx-cmd.home_tpd"));
            }
        });
    }

    @Override
    public void setHome() {
        onServerThread(() -> {
            if (!player.server.getGameRules().getBoolean(ModGameRules.ALLOW_HOME_TP) && !player.hasPermissions(2)) {
                clientMessage(Component.translatable("message.rfx-cmd.homes_not_allowed"));
                return;
            }

            int homes = controller.homeCount();
            int maxHomes = player.server.getGameRules().getRule(ModGameRules.MAX_HOMES).get();
            if (maxHomes > 0 && homes > maxHomes && !player.hasPermissions(2)) {
                clientMessage(Component.translatable("message.rfx-cmd.too_much_homes"));
                return;
            }

            controller.setHome();
            clientMessage(Component.translatable("message.rfx-cmd.home_set"));
        });
    }
}
