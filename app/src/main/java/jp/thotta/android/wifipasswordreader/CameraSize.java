package jp.thotta.android.wifipasswordreader;

/**
 * Created by thotta on 15/05/13.
 */
public class CameraSize {
    public int index;
    public int width;
    public int height;
    public boolean isSelected;
    public CameraSize(int id, int w, int h, boolean isSelected) {
        index = id;
        width = w;
        height = h;
        this.isSelected = isSelected;
    }
}
