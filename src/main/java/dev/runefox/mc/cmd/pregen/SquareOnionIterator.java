package dev.runefox.mc.cmd.pregen;

import java.util.NoSuchElementException;

public class SquareOnionIterator implements PositionIterator {
    private final int centerX, centerZ, layers;
    private int cl, cp, cx, cz;

    public SquareOnionIterator(int centerX, int centerZ, int layers) {
        this.centerX = centerX;
        this.centerZ = centerZ;
        this.layers = layers;
        restart();
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
        int len = layers * 2 - 1;
        return len * len;
    }

    @Override
    public boolean hasNext() {
        return cl != layers;
    }

    @Override
    public void next() {
        if (!hasNext())
            throw new NoSuchElementException();

        int layerSize = 8 * cl;
        cp++;
        if (cp >= layerSize) {
            cp = 0;
            cl++;
        }
        recomputePos();
    }

    private void recomputePos() {
        if (cl == 0) {
            cx = centerX;
            cz = centerZ;
        } else {
            int edgeLen = 2 * cl;
            int radius = cl;
            if (cp < edgeLen) {
                cx = centerX - radius;
                cz = centerZ - radius;
                cx += cp;
            } else if (cp < edgeLen * 2) {
                cx = centerX + radius;
                cz = centerZ - radius;
                cz += cp;
            } else if (cp < edgeLen * 3) {
                cx = centerX + radius;
                cz = centerZ + radius;
                cx -= cp;
            } else {
                cx = centerX - radius;
                cz = centerZ + radius;
                cz -= cp;
            }
        }
    }

    @Override
    public void restart() {
        cl = 0;
        cp = 0;
        recomputePos();
    }
}
