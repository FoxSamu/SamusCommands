package dev.runefox.mc.cmd.pregen;

public interface PositionIterator {
    int currentX();
    int currentZ();
    int total();

    boolean hasNext();
    void next();

    void restart();
}
