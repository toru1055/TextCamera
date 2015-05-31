package jp.thotta.android.wifipasswordreader;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


public class RecognizeResultActivity extends ActionBarActivity {
    private final String TAG = "RecognizeResultActivity";
    private OcrManager ocrManager;
    private EditText editText;
    private InputMethodManager imm;
    private ImageView imageView;
    private int activityMode;

    private View.OnClickListener onClickLayout = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            imageView.requestFocus();
            imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
        }
    };
    private View.OnClickListener onClickBackButton = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            imageView.requestFocus();
            imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
            finish();
        }
    };
    private View.OnClickListener onClickClearTextButton = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            editText.setText("");
            editText.requestFocus();
            imm.showSoftInput(editText, 0);//InputMethodManager.SHOW_IMPLICIT);
        }
    };
    private View.OnClickListener onClickActivityButton = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            imageView.requestFocus();
            imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
            switch (activityMode) {
                case Config.MODE_WIFI:
                    startWifiSetting();
                    break;
                case Config.MODE_URL:
                    startBrowser();
                    break;
                case Config.MODE_MAIL:
                    startMail();
                    break;
                case Config.MODE_TEL:
                    startPhone();
                    break;
                default:
                    startWifiSetting();
            }
        }
    };
    private View.OnClickListener onClickCopyButton = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            imageView.requestFocus();
            imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
            copyOnClipBoard();
        }
    };

    private void copyOnClipBoard() {
        ClipData.Item item = new ClipData.Item(editText.getText());
        String[] mimeType = new String[1];
        mimeType[0] = ClipDescription.MIMETYPE_TEXT_PLAIN;
        ClipData cd = new ClipData(new ClipDescription("text_data", mimeType), item);
        ClipboardManager cm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        cm.setPrimaryClip(cd);
        Toast.makeText(this, "クリップボードにコピーしました。", Toast.LENGTH_SHORT).show();
    }

    private void startWifiSetting() {
        copyOnClipBoard();
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_WIFI_SETTINGS);
        startActivity(intent);
    }

    private void startBrowser() {
        String url = editText.getText().toString();
        if(!url.startsWith("http")) {
            url = "http://" + url;
        }
        Log.v(TAG, "ReadUrl: " + url);
        try {
            Uri uri = Uri.parse(url);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        } catch(Exception e) {
            e.printStackTrace();
            Log.v(TAG, "ERROR: " + e.getMessage());
            Toast.makeText(this, "URLをオープンできませんでした。", Toast.LENGTH_SHORT);
        }
    }

    private void startMail() {
        String mailAddress = editText.getText().toString();
        try {
            Uri uri = Uri.parse("mailto:" + mailAddress);
            Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            Log.v(TAG, "ERROR: " + e.getMessage());
            Toast.makeText(this, "メールアドレスにエラーがありました。", Toast.LENGTH_SHORT);
        }
    }

    private void startPhone() {
        String phoneNumber = editText.getText().toString();
        try {
            Uri uri = Uri.parse("tel:" + phoneNumber);
            Intent intent = new Intent(Intent.ACTION_DIAL, uri);
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            Log.v(TAG, "ERROR: " + e.getMessage());
            Toast.makeText(this, "電話番号にエラーがありました。", Toast.LENGTH_SHORT);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recognize_result);

        //起動モード取得
        activityMode = getIntent().getIntExtra(getString(R.string.activity_mode), Config.MODE_WIFI);
        Log.v(TAG, "activityMode: " + activityMode);

        // 戻るボタン表示
        Utility.setActionBarWithMode(this, activityMode);

        findViewById(R.id.layoutResultActivity).setOnClickListener(onClickLayout);
        imageView = (ImageView)findViewById(R.id.imageView);
        imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        String trimmedImagePath = getIntent().getStringExtra("trimmedImagePath");
        Log.v(TAG, "trimmedImagePath: " + trimmedImagePath);
        Bitmap trimmedBitmap = BitmapUtility.read(trimmedImagePath);
        if(trimmedBitmap != null) {
            imageView.setImageBitmap(trimmedBitmap);
            ocrManager = new OcrManager(this);
            String recognizedText = ocrManager.getRecognizedText(trimmedBitmap);
            editText = (EditText)findViewById(R.id.editText);
            editText.setTypeface(Typeface.SERIF);
            editText.setText(recognizedText);
            Log.v(TAG, "recognizedText: " + recognizedText);
        }
        findViewById(R.id.buttonCopy).setOnClickListener(onClickCopyButton);
        ImageButton imageButton = (ImageButton)findViewById(R.id.buttonWifiSetting);
        imageButton.setOnClickListener(onClickActivityButton);
        TextView activityName = (TextView)findViewById(R.id.textViewActivityName);
        findViewById(R.id.buttonClearText).setOnClickListener(onClickClearTextButton);
        switch (activityMode) {
            case Config.MODE_WIFI:
                imageButton.setImageResource(R.mipmap.ic_action_wifi);
                activityName.setText(getString(R.string.wifi_button_name));
                break;
            case Config.MODE_URL:
                imageButton.setImageResource(R.mipmap.ic_action_globe);
                activityName.setText(getString(R.string.browser_button_name));
                break;
            case Config.MODE_MAIL:
                imageButton.setImageResource(R.mipmap.ic_action_mail);
                activityName.setText(getString(R.string.mail_button_name));
                break;
            case Config.MODE_TEL:
                imageButton.setImageResource(R.mipmap.ic_action_phone_start);
                activityName.setText(getString(R.string.phone_button_name));
                break;
            default:
                imageButton.setImageResource(R.mipmap.ic_action_wifi);
                activityName.setText(getString(R.string.wifi_button_name));
        }

//        findViewById(R.id.buttonBack).setOnClickListener(onClickBackButton);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_recognize_result, menu);
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
            Utility.goToTopActivity(RecognizeResultActivity.this);
            return true;
        }

        if(id == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


}
