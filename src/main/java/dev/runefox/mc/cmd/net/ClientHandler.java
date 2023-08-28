package dev.runefox.mc.cmd.net;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public interface ClientHandler {
    void sendPacket(ResourceLocation channel, FriendlyByteBuf buf);

    void ping();
    void message(Component component);
}
