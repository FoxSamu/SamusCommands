package dev.runefox.mc.cmd;

import net.minecraft.server.level.ServerLevel;

public enum AnimalTeleportMode {
    off,
    dimension,
    interdimensional;


    public boolean allowTeleport(ServerLevel from, ServerLevel to) {
        return switch (this) {
            case off -> false;
            case dimension -> from == to;
            case interdimensional -> true;
        };
    }
}
