package dev.runefox.mc.cmd;

public enum TeleportMode {
    tpa,
    tpc,
    both;

    public boolean tpa() {
        return this == tpa || this == both;
    }

    public boolean tpc() {
        return this == tpc || this == both;
    }

    public boolean allowed(boolean call) {
        return this == both || this == tpc && call || this == tpa && !call;
    }
}
