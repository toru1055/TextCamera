package jp.thotta.android.wifipasswordreader;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Camera;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by thotta on 15/05/10.
 */
public class CameraSizeManager {
    private static final long DEFAULT_SIZE = 1280 * 720;
    private static final String PREFERENCE_FILE_KEY = "jp.thotta.android.wifipasswordreader.CameraSize";
    private static final String SELECTED_INDEX_KEY = "CameraSizeSelectedIndex";
    private static final String LIST_LENGTH_KEY = "CameraSizeListLength";
    private static final String INITIALIZED_KEY = "IsInitialized";
    private static final String WIDTH_PREFIX = "width:";
    private static final String HEIGHT_PREFIX = "height:";
    private Activity activity;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public CameraSizeManager(Activity activity) {
        this.activity = activity;
        sharedPreferences = activity.getSharedPreferences(
                PREFERENCE_FILE_KEY,
                Context.MODE_PRIVATE
        );
        editor = sharedPreferences.edit();
    }

    public void initialize(List<Camera.Size> sizeList) {
        editor.putInt(LIST_LENGTH_KEY, sizeList.size());
        for(int i = 0; i < sizeList.size(); i++) {
            Camera.Size size = sizeList.get(i);
            String widthKey = WIDTH_PREFIX + i;
            String heightKey = HEIGHT_PREFIX + i;
            editor.putInt(widthKey, size.width);
            editor.putInt(heightKey, size.height);
        }
        int settingIndex = getDefaultKey(sizeList);
        editor.putInt(SELECTED_INDEX_KEY, settingIndex);
        editor.putBoolean(INITIALIZED_KEY, true);
        editor.commit();
    }

    public void clear() {
        editor.clear().commit();
    }

    public boolean isInitialized() {
        return sharedPreferences.getBoolean(INITIALIZED_KEY, false);
    }

    public List<CameraSize> getCameraSizeList() {
        List<CameraSize> sizeList = new ArrayList<>();
        int selectedIndex = sharedPreferences.getInt(SELECTED_INDEX_KEY, -1);
        int listLength = sharedPreferences.getInt(LIST_LENGTH_KEY, 0);
        for(int i = 0; i < listLength; i++) {
            String widthKey = WIDTH_PREFIX + i;
            String heightKey = HEIGHT_PREFIX + i;
            int width = sharedPreferences.getInt(widthKey, 0);
            int height = sharedPreferences.getInt(heightKey, 0);
            CameraSize size = new CameraSize(i, width, height, selectedIndex == i);
            sizeList.add(size);
        }
        return sizeList;
    }

    public void setCameraSize(CameraSize cameraSize) {
        editor.putInt(SELECTED_INDEX_KEY, cameraSize.index);
        editor.commit();
    }

    public Camera.Size selectDefaultSize(List<Camera.Size> sizeList) {
        Camera.Size appropriateSize = sizeList.get(0);
        long minimumDifference = Long.MAX_VALUE;
        for(Camera.Size size : sizeList) {
            long s = size.width * size.height;
            long diff = Math.abs(s - DEFAULT_SIZE);
            if(diff < minimumDifference) {
                minimumDifference = diff;
                appropriateSize = size;
            }
        }
        return appropriateSize;
    }

    public Camera.Size selectSettingSize(List<Camera.Size> sizeList) {
        int resolutionIndex = sharedPreferences.getInt(SELECTED_INDEX_KEY, -1);
        if(resolutionIndex >= 0 && resolutionIndex < sizeList.size()) {
            return sizeList.get(resolutionIndex);
        } else {
            return selectDefaultSize(sizeList);
        }
    }

    public Camera.Size getAppropriatePreviewSize(Camera.Size pictureSize, List<Camera.Size> supportedPreviewSizes) {
        Camera.Size appropriateSize = supportedPreviewSizes.get(0);
        long minimumDifference = Long.MAX_VALUE;
        long pictureS = pictureSize.width * pictureSize.height;
        for(Camera.Size size : supportedPreviewSizes) {
            long s = size.width * size.height;
            long diff = Math.abs(s - pictureS);
            if(diff < minimumDifference) {
                minimumDifference = diff;
                appropriateSize = size;
            }
        }
        return appropriateSize;
    }

    public int getDefaultKey(List<Camera.Size> sizeList) {
        int resolutionIndex = 0;
        long minimumDifference = Long.MAX_VALUE;
        for(int i = 0; i < sizeList.size(); i++) {
            Camera.Size size = sizeList.get(i);
            long s = size.width * size.height;
            long diff = Math.abs(s - DEFAULT_SIZE);
            if(diff < minimumDifference) {
                minimumDifference = diff;
                resolutionIndex = i;
            }
        }
        return resolutionIndex;
    }

    public int getSettingKey(List<Camera.Size> sizeList) {
        int resolutionIndex = sharedPreferences.getInt(SELECTED_INDEX_KEY, -1);
        if(resolutionIndex >= 0 && resolutionIndex < sizeList.size()) {
            return resolutionIndex;
        } else {
            return getDefaultKey(sizeList);
        }
    }
}
