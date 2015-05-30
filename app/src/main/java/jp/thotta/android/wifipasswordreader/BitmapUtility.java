package jp.thotta.android.wifipasswordreader;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;

/**
 * Created by thotta on 15/05/08.
 */
public class BitmapUtility {

    private static final String TAG = "BitmapUtility";

    public static boolean save(String imagePath, Bitmap bitmap) {
        FileOutputStream fos;
        try {
            fos = new FileOutputStream(imagePath);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static Bitmap read(String imagePath) {
        Bitmap bitmap = null;
        File imageFile = new File(imagePath);
        try {
            InputStream is = new FileInputStream(imageFile);
            bitmap = BitmapFactory.decodeStream(is);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    public static void removeJpgFiles(String dirPath) {
        File dir = new File(dirPath);
        String[] files = dir.list();
        for(String fname : files) {
            Log.v(TAG, "FileName: " + fname);
            File file = new File(dirPath + "/" +fname);
            if (file.exists()) {
                if (file.delete()) {
                    Log.v(TAG, "削除に成功しました: " + fname);
                } else {
                    Log.v(TAG, "削除に失敗しました: " + fname);
                }
            } else {
                Log.v(TAG, "ファイルが見つかりませんでした: " + fname);
            }
        }
    }
}
