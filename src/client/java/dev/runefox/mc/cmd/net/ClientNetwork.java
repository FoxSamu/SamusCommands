package dev.runefox.mc.cmd.net;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;

public class ClientNetwork {
    public static void init() {
        ClientPlayNetworking.registerGlobalReceiver(Network.CHANNEL_ID, ClientNetwork::receive);
        ClientHandlerImpl impl;
        Network.clientHandler = impl = new ClientHandlerImpl();

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> impl.ready());
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> impl.unready());
    }

    private static void receive(Minecraft client, ClientPacketListener handler, FriendlyByteBuf buf, PacketSender responseSender) {
        Network.clientReceives(buf);
    }
}
