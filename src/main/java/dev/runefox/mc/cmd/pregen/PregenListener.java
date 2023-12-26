package dev.runefox.mc.cmd.pregen;

public interface PregenListener {
    void update(int ok, int fail, int total);
    void finish(int ok, int fail, int total);
}
