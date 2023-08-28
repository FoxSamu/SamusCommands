package dev.runefox.mc.cmd.net;

import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.network.FriendlyByteBuf;

import java.util.ArrayList;
import java.util.List;

public class PacketReg<T> {
    private final Object2IntMap<Class<? extends ModPacket<T>>> ids = new Object2IntOpenHashMap<>();
    private final List<ModPacket.Decoder<T, ?>> decoders = new ArrayList<>();

    public <P extends ModPacket<T>> void register(Class<P> cls, ModPacket.Decoder<T, P> decoder) {
        int id = decoders.size();
        decoders.add(decoder);
        ids.put(cls, id);
    }

    public ModPacket<T> decode(FriendlyByteBuf buf) {
        int id = buf.readShort();
        if (id < 0 || id >= decoders.size())
            return null;

        return decoders.get(id).decode(buf);
    }

    public void encode(ModPacket<T> pkt, FriendlyByteBuf buf) {
        int id = ids.getOrDefault(pkt.getClass(), -1);
        if (id < 0)
            throw new IllegalArgumentException("Packet not registered: " + pkt.getClass().getName());
        buf.writeShort(id);
        pkt.encode(buf);
    }
}
