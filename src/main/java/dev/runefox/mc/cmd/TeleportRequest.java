package dev.runefox.mc.cmd;

import net.minecraft.server.level.ServerPlayer;

import java.util.Comparator;

public record TeleportRequest(ServerPlayer from, long timestamp, boolean call) {
    public static final Comparator<TeleportRequest> COMPARATOR = Comparator.comparing(TeleportRequest::timestamp);
}
