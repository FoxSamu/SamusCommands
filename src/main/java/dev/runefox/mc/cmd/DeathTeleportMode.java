package dev.runefox.mc.cmd;

public enum DeathTeleportMode {
    off(false, false),
    command(true, false),
    compass(false, true),
    both(true, true);

    public final boolean allowCommand;
    public final boolean allowCompass;

    DeathTeleportMode(boolean allowCommand, boolean allowCompass) {
        this.allowCommand = allowCommand;
        this.allowCompass = allowCompass;
    }
}
