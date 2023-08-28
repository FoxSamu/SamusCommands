package dev.runefox.mc.cmd.net;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;

public class ServerSetHomePacket implements ModPacket<ServerHandler> {
    public ServerSetHomePacket() {
    }

    public ServerSetHomePacket(ByteBuf buf) {
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
    }

    @Override
    public void handle(ServerHandler handler) {
        handler.setHome();
    }
}
