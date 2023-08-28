package dev.runefox.mc.cmd.net;

import dev.runefox.mc.cmd.ModPlayer;
import net.minecraft.server.level.ServerPlayer;

public interface ServerHandler {
    ServerPlayer player();
    ModPlayer controller();

    void pong();
    void goHome();
    void setHome();
}
