package jp.thotta.android.wifipasswordreader;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;


public class TrimImageActivity extends ActionBarActivity {
    private final String TAG = "TrimImageActivity";
    private String originalImagePath;
    private String trimmedImagePath;
    private TrimmingRectangleView trimView;
    private Bitmap originalBitmap;
    private int activityMode;

    private View.OnClickListener onClickFixRegionButton = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            BitmapUtility.save(trimmedImagePath, trimView.getCurrentBitmap());
            Intent intent = new Intent(TrimImageActivity.this, RecognizeResultActivity.class);
            intent.putExtra("trimmedImagePath", trimmedImagePath);
            intent.putExtra(getString(R.string.activity_mode), activityMode);
            startActivity(intent);
        }
    };
    private View.OnClickListener onClickBackToCameraActivityButton = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            finish();
        }
    };
    private View.OnClickListener onClickBackToOriginalBitmapButton = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            trimView.backToOriginalBitmap();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trim_image);
        //起動モード取得
        activityMode = getIntent().getIntExtra(getString(R.string.activity_mode), Config.MODE_WIFI);

        // 戻るボタン表示
        Utility.setActionBarWithMode(this, activityMode);

        originalImagePath = getIntent().getStringExtra("originalImagePath");
        trimmedImagePath = originalImagePath.replaceFirst(".jpg$", "_trimmed.jpg");
        Log.v(TAG, "originalImagePath: " + originalImagePath);
        Log.v(TAG, "trimmedImagePath: " + trimmedImagePath);
        findViewById(R.id.buttonBackToOriginalBitmap).setOnClickListener(onClickBackToOriginalBitmapButton);
        findViewById(R.id.buttonFixRegion).setOnClickListener(onClickFixRegionButton);
        originalBitmap = BitmapUtility.read(originalImagePath);
        if(originalBitmap != null) {
            FrameLayout parentLayout = (FrameLayout)findViewById(R.id.trimFrame);
            trimView = (TrimmingRectangleView)findViewById(R.id.trimView);
            trimView.initialize(parentLayout, originalBitmap);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_trim_image, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_home) {
            Utility.goToTopActivity(TrimImageActivity.this);
            return true;
        }

        if(id == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
