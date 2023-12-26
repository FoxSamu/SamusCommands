package dev.runefox.mc.cmd.pregen;

import java.util.NoSuchElementException;

public class SingleIterator implements PositionIterator {
    private final int x, z;
    private boolean present = true;

    public SingleIterator(int x, int z) {
        this.x = x;
        this.z = z;
    }

    @Override
    public int currentX() {
        return x;
    }

    @Override
    public int currentZ() {
        return z;
    }

    @Override
    public int total() {
        return 1;
    }

    @Override
    public boolean hasNext() {
        return present;
    }

    @Override
    public void next() {
        if (!present)
            throw new NoSuchElementException();
        present = false;
    }

    @Override
    public void restart() {
        present = true;
    }
}
