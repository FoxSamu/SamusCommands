package dev.runefox.mc.cmd.net;

import dev.runefox.mc.cmd.ModPlayer;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

public class Network {
    public static final ResourceLocation CHANNEL_ID = new ResourceLocation("rfx-cmd", "packet");

    public static final PacketReg<ClientHandler> CLIENT_REG = new PacketReg<>();
    public static final PacketReg<ServerHandler> SERVER_REG = new PacketReg<>();

    static ClientHandler clientHandler;


    public static void init() {
        CLIENT_REG.register(ClientPingPacket.class, ClientPingPacket::new);
        CLIENT_REG.register(ClientMessagePacket.class, ClientMessagePacket::new);

        SERVER_REG.register(ServerPongPacket.class, ServerPongPacket::new);
        SERVER_REG.register(ServerGoHomePacket.class, ServerGoHomePacket::new);
        SERVER_REG.register(ServerSetHomePacket.class, ServerSetHomePacket::new);

        ServerPlayNetworking.registerGlobalReceiver(CHANNEL_ID, Network::receive);
    }

    private static void receive(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl handler, FriendlyByteBuf buf, PacketSender responseSender) {
        serverReceives(player, buf);
    }

    public static void sendServer(ModPacket<ServerHandler> pkt) {
        if (clientHandler != null) {
            FriendlyByteBuf buf = PacketByteBufs.create();
            SERVER_REG.encode(pkt, buf);
            clientHandler.sendPacket(CHANNEL_ID, buf);
        }
    }

    public static void sendClient(ServerPlayer player, ModPacket<ClientHandler> pkt) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        CLIENT_REG.encode(pkt, buf);
        ServerPlayNetworking.send(player, CHANNEL_ID, buf);
    }

    static void clientReceives(FriendlyByteBuf buf) {
        ModPacket<ClientHandler> pkt = CLIENT_REG.decode(buf);
        if (clientHandler != null)
            pkt.handle(clientHandler);
    }

    static void serverReceives(ServerPlayer player, FriendlyByteBuf buf) {
        ModPacket<ServerHandler> pkt = SERVER_REG.decode(buf);
        pkt.handle(ModPlayer.get(player).net());
    }
}
