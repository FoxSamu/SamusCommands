package dev.runefox.mc.cmd.net;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;

public interface ModPacket<T> {
    void encode(FriendlyByteBuf buf);
    void handle(T handler);

    public interface Decoder<T, P extends ModPacket<T>> {
        P decode(FriendlyByteBuf buf);
    }
}
