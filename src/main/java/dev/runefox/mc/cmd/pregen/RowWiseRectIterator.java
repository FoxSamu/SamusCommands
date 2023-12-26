package dev.runefox.mc.cmd.pregen;

import java.util.NoSuchElementException;

public class RowWiseRectIterator implements PositionIterator {
    private final int x1, z1, x2, z2;
    private int cx, cz;

    public RowWiseRectIterator(int x1, int z1, int x2, int z2) {
        this.x1 = Math.min(x1, x2);
        this.z1 = Math.min(z1, z2);
        this.x2 = Math.max(x1, x2) + 1;
        this.z2 = Math.max(z1, z2) + 1;
        restart();
    }

    private int remainingRows() {
        return z2 - (cz + 1);
    }

    private int remainingInRow() {
        return x2 - cx;
    }

    @Override
    public int currentX() {
        return cx;
    }

    @Override
    public int currentZ() {
        return cz;
    }

    @Override
    public int total() {
        return (x2 - x1) * (z2 - z1);
    }

    @Override
    public boolean hasNext() {
        return cx != x2 && cz != z2;
    }

    @Override
    public void next() {
        if (!hasNext())
            throw new NoSuchElementException();

        cx++;
        if (cx >= x2) {
            cx = x1;
            cz++;
        }
    }

    @Override
    public void restart() {
        this.cx = this.x1;
        this.cz = this.z1;
    }
}
