package jp.thotta.android.wifipasswordreader;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

// TODO: OutOfMemory対応
// 設定に飛ばすことはできたけど、何かとエラーが残っている（シャッターをクリックしても撮影されない）。
// このエラーの時はCreateからやり直したいけど、onResumeに全部の処理を持っていくと通常の起動ができなくなる
// 工夫する必要があるよ。
public class CameraActivity extends ActionBarActivity {
    private final String TAG = "CameraActivity";
    private Camera camera;
    private SurfaceView cameraView;
    private SurfaceHolder cameraHolder;
    private FrameLayout frameLayoutCameraPreview;
    private boolean isTaking = false;
    private int activityMode;
    private String saveDir = Environment.getExternalStorageDirectory().getPath() + "/WifiPasswordReader";

    private SurfaceHolder.Callback cameraHolderCallback = new SurfaceHolder.Callback() {

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            camera = Camera.open();
            try {
                camera.setDisplayOrientation(90);
                camera.setPreviewDisplay(holder);

                // 解像度の設定
                Camera.Parameters params = camera.getParameters();
                List<Camera.Size> supportedPreviewSizes = params.getSupportedPreviewSizes();
                List<Camera.Size> supportedPictureSizes = params.getSupportedPictureSizes();
                CameraSizeManager csm = new CameraSizeManager(CameraActivity.this);
                if(!csm.isInitialized()) {
                    csm.initialize(supportedPictureSizes);
                }
                Camera.Size pictureSize = csm.selectSettingSize(supportedPictureSizes);
                Camera.Size previewSize = csm.getAppropriatePreviewSize(pictureSize, supportedPreviewSizes);
                params.setPictureSize(pictureSize.width, pictureSize.height);
                params.setPreviewSize(previewSize.width, previewSize.height);
                camera.setParameters(params);
                Log.v(TAG, "PictureSize: w=" + pictureSize.width + ", h=" + pictureSize.height);
                Log.v(TAG, "PreviewSize: w=" + previewSize.width + ", h=" + previewSize.height);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            camera.startPreview();
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    };

    private View.OnClickListener onClickSettingButton = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            startActivity(new Intent(CameraActivity.this, SettingActivity.class));
        }
    };

    private View.OnClickListener onClickCameraPreview = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            camera.autoFocus(null);
        }
    };

    private View.OnClickListener onClickShutterButton = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(!isTaking) {
                isTaking = true;
//                camera.autoFocus(autoFocusListener);
                camera.takePicture(shutterListener, null, takePictureListener);
            }
        }
    };

    private Camera.AutoFocusCallback autoFocusListener = new Camera.AutoFocusCallback() {
        @Override
        public void onAutoFocus(boolean success, Camera camera) {
            camera.takePicture(shutterListener, null, takePictureListener);
        }
    };

    private Camera.ShutterCallback shutterListener = new Camera.ShutterCallback() {
        @Override
        public void onShutter() {
            // 空でOK。
        }
    };

    private Camera.PictureCallback takePictureListener = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            if(data == null) {
                return;
            }

            try {
                Calendar cal = Calendar.getInstance();
                SimpleDateFormat sf = new SimpleDateFormat("yyyyMMdd_HHmmss");
                String imgPath = saveDir + "/" + sf.format(cal.getTime()) + ".jpg";
                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                Matrix m = new Matrix();
                m.setRotate(90);
                Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
                ShutterFrame r = new ShutterFrame(rotatedBitmap.getWidth(), rotatedBitmap.getHeight());
                Bitmap trimmedBitmap = Bitmap.createBitmap(rotatedBitmap, r.getX(), r.getY(), r.getW(), r.getH());
                BitmapUtility.save(imgPath, trimmedBitmap);
                isTaking = false;
                Intent intent = new Intent(CameraActivity.this, TrimImageActivity.class);
                intent.putExtra("originalImagePath", imgPath);
                intent.putExtra(getString(R.string.activity_mode), activityMode);
                startActivity(intent);
            } catch(OutOfMemoryError e) {
                Toast.makeText(CameraActivity.this,
                        "エラーが発生しました。設定で解像度を下げてください。",
                        Toast.LENGTH_SHORT).show();
                Log.v(TAG, "error");
                startActivity(new Intent(CameraActivity.this, SettingActivity.class));
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        // 起動モードを取得
        activityMode = getIntent().getIntExtra(getString(R.string.activity_mode), Config.MODE_WIFI);

        // 戻るボタン表示
        Utility.setActionBarWithMode(this, activityMode);

        // カメラの設定
        // TODO: 解像度設定がミスっていると、起動できなくなる件、直す
        // TODO: どこでException吐くか調べて、Toastでエラー表示後、SettingActivityに飛ばす
        File file = new File(saveDir);
        if (!file.exists()) {
            if (!file.mkdir()) {
                Log.e("Debug", "Make Dir Error");
            }
        }
        frameLayoutCameraPreview = (FrameLayout)findViewById(R.id.frameLayoutCamera);
        frameLayoutCameraPreview.setOnClickListener(onClickCameraPreview);

        // TODO: ここからTryCatchかな
        cameraView = new SurfaceView(this);
        cameraHolder = cameraView.getHolder();
        cameraHolder.addCallback(cameraHolderCallback);
        cameraHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        frameLayoutCameraPreview.addView(cameraView);

        // ボタンの設定
        Button buttonShutter = (Button)findViewById(R.id.buttonShutter);
        buttonShutter.setOnClickListener(onClickShutterButton);
        findViewById(R.id.buttonSetting).setOnClickListener(onClickSettingButton);

        // 撮影対象枠の設定
        View shutterFrameView = new View(this) {
            @Override
            protected void onDraw(Canvas canvas) {
                Paint paint = new Paint();
                paint.setColor(Color.GREEN);
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(3);
                ShutterFrame shutterFrame = new ShutterFrame(
                        cameraView.getWidth(), cameraView.getHeight());
                Log.v(TAG, "drawRect: "+shutterFrame.toString());
                Rect r = shutterFrame.getRect();
                canvas.drawRect(r, paint);
            }
        };
        frameLayoutCameraPreview.addView(shutterFrameView);

//        TextView textView = new TextView(this);
//        textView.setText("四角内に入るように撮影してください");
//        textView.setTextColor(Color.WHITE);
//        textView.setTextSize(40);
//        frameLayoutCameraPreview.addView(textView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 過去の画像達をここで削除する
        BitmapUtility.removeJpgFiles(saveDir);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_camera, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(CameraActivity.this, SettingActivity.class));
            return true;
        }

        if(id == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
