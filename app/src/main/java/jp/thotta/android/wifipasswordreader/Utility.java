package jp.thotta.android.wifipasswordreader;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;

import com.google.android.gms.ads.AdRequest;

/**
 * Created by thotta on 15/05/30.
 */
public class Utility {
    public static void setActionBarWithMode(ActionBarActivity context, int activityMode) {
        // 戻るボタン表示
        context.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        context.getSupportActionBar().setDisplayShowHomeEnabled(true);
        switch (activityMode) {
            case Config.MODE_WIFI:
                context.getSupportActionBar().setIcon(R.mipmap.ic_action_wifi);
                break;
            case Config.MODE_URL:
                context.getSupportActionBar().setIcon(R.mipmap.ic_action_globe);
                break;
            case Config.MODE_MAIL:
                context.getSupportActionBar().setIcon(R.mipmap.ic_action_mail);
                break;
            case Config.MODE_TEL:
                context.getSupportActionBar().setIcon(R.mipmap.ic_action_phone_start);
                break;
            default:
                context.getSupportActionBar().setIcon(R.mipmap.ic_action_wifi);
        }
    }

    public static void goToTopActivity(Activity activity) {
        Intent intent = new Intent(activity, TopActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        activity.startActivity(intent);
    }

    public static AdRequest makeAdRequest(Context context) {
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(context.getString(R.string.test_device_id_for_ads))
                .addTestDevice(context.getString(R.string.test_device_id_for_ads2))
                .addKeyword("文字認識")
                .build();
        return adRequest;
    }
}
