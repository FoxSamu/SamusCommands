package dev.runefox.mc.cmd.net;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;

public class ClientMessagePacket implements ModPacket<ClientHandler> {
    private final Component component;

    public ClientMessagePacket(Component component) {
        this.component = component;
    }

    public ClientMessagePacket(FriendlyByteBuf buf) {
        this.component = buf.readComponent();
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeComponent(component);
    }

    @Override
    public void handle(ClientHandler handler) {
        handler.message(component);
    }
}
