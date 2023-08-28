package dev.runefox.mc.cmd.net;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;

public class ClientPingPacket implements ModPacket<ClientHandler> {
    public ClientPingPacket() {
    }

    public ClientPingPacket(ByteBuf buf) {
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
    }

    @Override
    public void handle(ClientHandler handler) {
        handler.ping();
    }
}
