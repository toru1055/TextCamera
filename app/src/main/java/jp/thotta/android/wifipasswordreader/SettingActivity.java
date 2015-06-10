package jp.thotta.android.wifipasswordreader;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import java.util.List;


public class SettingActivity extends ActionBarActivity {
    private static final String TAG = "SettingActivity";
    private CameraSizeManager csm;
    private List<CameraSize> sizeList;
    private boolean isOutOfMemory = false;

    private RadioGroup.OnCheckedChangeListener onChangeCheckRadio = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            int checkedRadioId = (checkedId - 1);// % group.getChildCount();
            Log.v(TAG, "checkedId: " + checkedId + ", checkedRadioId: " + checkedRadioId + ", GroupSize: " + group.getChildCount());
            RadioButton rb = (RadioButton)findViewById(checkedId);
            CameraSize size = sizeList.get(checkedRadioId);
            csm.setCameraSize(size);
        }
    };

    private View.OnClickListener onClickFinishButton = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(isOutOfMemory) {
                Utility.goToTopActivity(SettingActivity.this);
            } else {
                finish();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        isOutOfMemory = getIntent().getBooleanExtra("IS_OUT_OF_MEMORY", false);
        // 戻るボタン表示
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setIcon(R.mipmap.ic_launcher);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        RadioGroup group = (RadioGroup)findViewById(R.id.radioGroup);
        Log.v(TAG, "RadioGroup Size: " + group.getChildCount());
        group.setOnCheckedChangeListener(onChangeCheckRadio);
        csm = new CameraSizeManager(this);
        sizeList = csm.getCameraSizeList();
        int selectedButtonId = 0;
        for(CameraSize size : sizeList) {
            Log.v(TAG, String.format("CameraSize=(%d, %d, %d, %b)", size.index, size.width, size.height, size.isSelected));
            RadioButton radio = new RadioButton(this);
            radio.setText(String.format("CameraSize: %d x %d", size.width, size.height));
            radio.setId(size.index + 1);
            if(size.isSelected) {
                selectedButtonId = size.index + 1;
            }
            group.addView(radio);
        }
        group.check(selectedButtonId);
        findViewById(R.id.buttonFinish).setOnClickListener(onClickFinishButton);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_setting, menu);
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
            return true;
        }

        if(id == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
