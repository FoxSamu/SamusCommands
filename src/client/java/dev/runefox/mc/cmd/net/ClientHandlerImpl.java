package dev.runefox.mc.cmd.net;

import dev.runefox.mc.cmd.CommandsMod;
import dev.runefox.mc.cmd.CommandsModClient;
import dev.runefox.mc.cmd.net.ClientHandler;
import dev.runefox.mc.cmd.net.Network;
import dev.runefox.mc.cmd.net.ServerPongPacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class ClientHandlerImpl implements ClientHandler {
    private boolean ready;
    private final List<DeferredPacket> deferredPackets = new ArrayList<>();

    private void onClientThread(Runnable task) {
        Minecraft.getInstance().execute(task);
    }

    private record DeferredPacket(ResourceLocation channel, FriendlyByteBuf buf) {
    }

    public void ready() {
        ready = true;
        for (DeferredPacket pkt : deferredPackets) {
            ClientPlayNetworking.send(pkt.channel, pkt.buf);
        }
        deferredPackets.clear();
    }

    public void unready() {
        ready = false;
    }

    @Override
    public void sendPacket(ResourceLocation channel, FriendlyByteBuf buf) {
        if (!ready) {
            deferredPackets.add(new DeferredPacket(channel, buf));
        } else {
            ClientPlayNetworking.send(channel, buf);
        }
    }

    @Override
    public void ping() {
        onClientThread(() -> {
            CommandsModClient.synced = true;
            CommandsMod.LOGGER.info("Server has rfx-cmd and tries to sync");
            Network.sendServer(new ServerPongPacket());
        });
    }

    @Override
    public void message(Component component) {
        Minecraft.getInstance().gui.setOverlayMessage(component, false);
    }
}
