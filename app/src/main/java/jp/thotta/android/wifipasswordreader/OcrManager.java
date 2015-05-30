package jp.thotta.android.wifipasswordreader;

import android.app.Activity;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by thotta on 15/05/08.
 */
public class OcrManager {
    private static final String TAG = "OcrManager";
    public static final String lang = "eng";
    public static final String DATA_PATH = Environment
            .getExternalStorageDirectory().toString() + "/OcrManager/";

    private Activity activity;
    private TessBaseAPI baseAPI;

    public OcrManager(Activity activity) {
        this.activity = activity;
        copyTrainedData();
        baseAPI = new TessBaseAPI();
        baseAPI.init(DATA_PATH, lang);
        baseAPI.setDebug(true);
//        String whiteList = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
//        baseAPI.setVariable(TessBaseAPI.VAR_CHAR_WHITELIST, whiteList);
    }

    public String getRecognizedText(Bitmap bitmap) {
        baseAPI.setImage(bitmap);
        String recognizedText = baseAPI.getUTF8Text();
//        recognizedText = recognizedText.replaceAll("[^0-9a-zA-Z@:¥/¥-¥. ]", "-");
        recognizedText = recognizedText.replaceAll("[^0-9a-zA-Z@:¥/¥-¥.]", "-");
        return recognizedText;
    }

    private boolean isInitialized() {
        File trainedFile = new File(DATA_PATH + "tessdata/" + lang + ".traineddata");
        return trainedFile.exists();
    }

    private boolean makeDirectories() {
        String[] paths = new String[] { DATA_PATH, DATA_PATH + "tessdata/" };

        for (String path : paths) {
            File dir = new File(path);
            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    Log.v(TAG, "ERROR: Creation of directory " + path + " on sdcard failed");
                    return false;
                } else {
                    Log.v(TAG, "Created directory " + path + " on sdcard");
                }
            }
        }
        return true;
    }

    private void copyTrainedData() {
        if (!isInitialized()) {
            makeDirectories();
            try {
                AssetManager assetManager = activity.getAssets();
                InputStream in = assetManager.open("tessdata/" + lang + ".traineddata");
                //GZIPInputStream gin = new GZIPInputStream(in);
                OutputStream out = new FileOutputStream(DATA_PATH
                        + "tessdata/" + lang + ".traineddata");

                // Transfer bytes from in to out
                byte[] buf = new byte[1024];
                int len;
                //while ((lenf = gin.read(buff)) > 0) {
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                in.close();
                //gin.close();
                out.close();

                Log.v(TAG, "Copied " + lang + " traineddata");
            } catch (IOException e) {
                Log.e(TAG, "Was unable to copy " + lang + " traineddata " + e.toString());
            }
        }
    }
}
