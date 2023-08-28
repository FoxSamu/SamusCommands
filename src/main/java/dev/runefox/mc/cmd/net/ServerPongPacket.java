package dev.runefox.mc.cmd.net;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;

public class ServerPongPacket implements ModPacket<ServerHandler> {
    public ServerPongPacket() {
    }

    public ServerPongPacket(ByteBuf buf) {
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
    }

    @Override
    public void handle(ServerHandler handler) {
        handler.pong();
    }
}
