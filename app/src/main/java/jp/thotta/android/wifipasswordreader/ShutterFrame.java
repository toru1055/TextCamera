package jp.thotta.android.wifipasswordreader;

import android.graphics.Rect;

/**
 * Created by thotta on 15/05/09.
 */
public class ShutterFrame {
    static final int GRID_SIZE = 32;
    private int gridX;
    private int gridY;

    public ShutterFrame(int frameWidth, int frameHeight) {
        gridX = frameWidth / GRID_SIZE;
        gridY = frameHeight / GRID_SIZE;
    }

    public Rect getRect() {
        return (new Rect(getX(), getY(), getX() + getW(), getY() + getH()));
    }

    public int getX() {
        return ((GRID_SIZE * gridX) - getW()) / 2;
    }

    public int getY() {
        return ((GRID_SIZE * gridY) - getH()) / 2;
    }

    public int getW() {
        return 30 * gridX;
    }

    public int getH() {
        return 16 * gridY;
    }

    public String toString() {
        return "x="+getX()+", y="+getY()+", w="+getW()+", h="+getH();
    }
}
