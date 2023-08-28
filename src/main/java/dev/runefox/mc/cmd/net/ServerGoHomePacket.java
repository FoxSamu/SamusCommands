package dev.runefox.mc.cmd.net;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;

public class ServerGoHomePacket implements ModPacket<ServerHandler> {
    public ServerGoHomePacket() {
    }

    public ServerGoHomePacket(ByteBuf buf) {
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
    }

    @Override
    public void handle(ServerHandler handler) {
        handler.goHome();
    }
}
