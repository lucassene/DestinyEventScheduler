package com.destiny.event.scheduler.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.destiny.event.scheduler.R;
import com.destiny.event.scheduler.dialogs.MyAlertDialog;
import com.destiny.event.scheduler.interfaces.FromDialogListener;
import com.destiny.event.scheduler.services.BungieService;
import com.destiny.event.scheduler.services.RequestResultReceiver;

import java.util.Calendar;

public class PrepareActivity extends AppCompatActivity implements RequestResultReceiver.Receiver, FromDialogListener {

    private static final String TAG = "PrepareActivity";

    public static final String LEGEND_PREF = "prepareLegend";

    ProgressBar progressBar;
    TextView text;

    String membershipId;
    String userName;
    int platformId;
    String clanId;

    int errorCode;
    String msg = "";

    RequestResultReceiver mReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.prepare_layout);

        progressBar = (ProgressBar) findViewById(R.id.progress);
        text = (TextView) findViewById(R.id.msg_text);

        String cookies = getIntent().getStringExtra("cookies");
        String xcsrf = getIntent().getStringExtra("x-csrf");
        int platform = getIntent().getIntExtra("platform",0);

        if (mReceiver == null && !isBungieServiceRunning()){
            mReceiver = new RequestResultReceiver(new Handler());
            mReceiver.setReceiver(this);
            Intent intent = new Intent(Intent.ACTION_SYNC, null, this, BungieService.class);
            intent.putExtra(BungieService.REQUEST_EXTRA, BungieService.TYPE_LOGIN);
            intent.putExtra(BungieService.COOKIE_EXTRA, cookies);
            intent.putExtra(BungieService.RECEIVER_EXTRA, mReceiver);
            intent.putExtra(BungieService.XCSRF_EXTRA, xcsrf);
            intent.putExtra(BungieService.PLATFORM_EXTRA, platform);
            startService(intent);
        }

        if (savedInstanceState != null){
            //Log.w(TAG, "SavedInstance != null");
            msg = savedInstanceState.getString("legend");
            text.setText(msg);
        } else {
            //Log.w(TAG, "SavedInstance is empty!");
            SharedPreferences sharedPrefs = getApplicationContext().getSharedPreferences(DrawerActivity.SHARED_PREFS, Context.MODE_PRIVATE);
            msg = sharedPrefs.getString(LEGEND_PREF, getString(R.string.vanguard_data));
            text.setText(msg);
        }
    }

    public boolean isBungieServiceRunning() {
        SharedPreferences sharedPrefs = getApplicationContext().getSharedPreferences(DrawerActivity.SHARED_PREFS, Context.MODE_PRIVATE);
        boolean var = sharedPrefs.getBoolean(BungieService.RUNNING_SERVICE, false);
        //Log.w(TAG, "BungieService running? " +  var);
        return var;
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sharedPrefs = getApplicationContext().getSharedPreferences(DrawerActivity.SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putBoolean(DrawerActivity.FOREGROUND_PREF, true);
        editor.apply();
    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences sharedPrefs = getApplicationContext().getSharedPreferences(DrawerActivity.SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putBoolean(DrawerActivity.FOREGROUND_PREF, false);
        editor.putString(LEGEND_PREF,msg);
        editor.apply();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("legend",msg);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        msg = savedInstanceState.getString("legend");
        text.setText(msg);
    }

    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {

        switch (resultCode) {
            case BungieService.STATUS_RUNNING:
                progressBar.setVisibility(View.VISIBLE);
                break;
            case BungieService.STATUS_ERROR:
                switch (resultData.getInt(BungieService.ERROR_TAG)){
                    case (BungieService.ERROR_NO_CONNECTION):
                        errorCode = BungieService.ERROR_NO_CONNECTION;
                        break;
                    case (BungieService.ERROR_HTTP_REQUEST):
                        errorCode = BungieService.ERROR_HTTP_REQUEST;
                        break;
                    case (BungieService.ERROR_RESPONSE_CODE):
                        errorCode = BungieService.ERROR_RESPONSE_CODE;
                        break;
                    case (BungieService.ERROR_CURRENT_USER):
                        errorCode = BungieService.ERROR_CURRENT_USER;
                        break;
                    case (BungieService.ERROR_NO_CLAN):
                        errorCode = BungieService.ERROR_NO_CLAN;
                        break;
                    case BungieService.ERROR_INCORRECT_REQUEST:
                        errorCode = BungieService.ERROR_INCORRECT_REQUEST;
                        Log.w(TAG, "Incorrect request to BungieService");
                        break;
                }
                showAlertDialog();
                progressBar.setVisibility(View.GONE);
                text.setVisibility(View.GONE);
                break;
            case BungieService.STATUS_DOCS:
                membershipId = resultData.getString("bungieId");
                userName = resultData.getString("userName");
                platformId = resultData.getInt("platform");
                msg = getString(R.string.prior_entries);
                text.setText(msg);
                break;
            case BungieService.STATUS_VERIFY:
                msg = getString(R.string.allies);
                text.setText(msg);
                break;
            case BungieService.STATUS_FRIENDS:
                clanId = resultData.getString("clanId");
                msg = getString(R.string.network);
                text.setText(msg);
                break;
/*            case BungieService.STATUS_PARTY:
                msg = getString(R.string.creating_db);
                text.setText(msg);
                break;*/
            case BungieService.STATUS_FINISHED:
                if (errorCode != BungieService.NO_ERROR){
                    showAlertDialog();
                } else openDrawerActivity();
                progressBar.setVisibility(View.GONE);
                text.setVisibility(View.GONE);
                break;
        }
    }

    private void showAlertDialog() {
        DialogFragment dialog = new MyAlertDialog();
        Bundle bundle = new Bundle();
        bundle.putInt("type",MyAlertDialog.ALERT_DIALOG);

        switch (errorCode){
            case BungieService.ERROR_NO_ICON:
                bundle.putString("title",getString(R.string.just_warning));
                bundle.putString("msg",getString(R.string.no_icon_msg));
                bundle.putString("posButton",getString(R.string.got_it));
                break;
            case BungieService.ERROR_NO_CONNECTION:
                bundle.putString("title",getString(R.string.error));
                bundle.putString("msg",getString(R.string.no_connection_msg));
                bundle.putString("posButton",getString(R.string.got_it));
                break;
            case BungieService.ERROR_HTTP_REQUEST:
            case BungieService.ERROR_RESPONSE_CODE:
            case BungieService.ERROR_CLAN_MEMBER:
            case BungieService.ERROR_MEMBERS_OF_CLAN:
                bundle.putString("title",getString(R.string.error));
                bundle.putString("msg",getString(R.string.bungie_net_error_msg));
                bundle.putString("posButton",getString(R.string.got_it));
                break;
            case BungieService.ERROR_NO_CLAN:
                bundle.putString("title",getString(R.string.error));
                bundle.putString("msg",getString(R.string.no_clan_msg));
                bundle.putString("posButton", getString(R.string.got_it));
                break;
            case BungieService.ERROR_CURRENT_USER:
                bundle.putString("title",getString(R.string.error));
                bundle.putString("msg",getString(R.string.error_bungie_auth));
                bundle.putString("posButton",getString(R.string.got_it));
                break;
            default:
                bundle.putString("title",getString(R.string.error));
                bundle.putString("msg", getString(R.string.some_problem_msg));
                bundle.putString("posButton", getString(R.string.got_it));
                break;
        }

        dialog.setArguments(bundle);
        dialog.show(getSupportFragmentManager(),"alert");
    }

    public void openDrawerActivity(){

        SharedPreferences sharedPrefs = getSharedPreferences(DrawerActivity.SHARED_PREFS, Context.MODE_PRIVATE);

        SharedPreferences.Editor sharedEditor = sharedPrefs.edit();
        sharedEditor.putBoolean(DrawerActivity.SOUND_PREF, true);
        sharedEditor.putBoolean(DrawerActivity.SCHEDULED_NOTIFY_PREF, true);
        sharedEditor.putBoolean(DrawerActivity.NEW_NOTIFY_PREF, false);
        sharedEditor.putInt(DrawerActivity.SCHEDULED_TIME_PREF, 0);
        sharedEditor.putInt(DrawerActivity.NEW_NOTIFY_TIME_PREF, DrawerActivity.DEFAULT_INTERVAL);
        sharedEditor.putString(DrawerActivity.MEMBER_PREF, membershipId);
        sharedEditor.putInt(DrawerActivity.PLATFORM_PREF, platformId);
        sharedEditor.putString(DrawerActivity.CLAN_PREF, clanId);
        sharedEditor.apply();

       if (sharedPrefs.getBoolean(DrawerActivity.FOREGROUND_PREF, false)){
           Intent intent = new Intent(this, DrawerActivity.class);
           startActivity(intent);
           finish();
       };
    }

    @Override
    public void onPositiveClick(String input, int type) {
        backToLoginActivity();
    }

    private void backToLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onDateSent(Calendar date) {

    }

    @Override
    public void onTimeSent(int hour, int minute) {

    }

    @Override
    public void onLogoff() {

    }

    @Override
    public void onItemSelected(String type, String entry, int value) {

    }

    @Override
    public void onMultiItemSelected(boolean[] items) {

    }
}
