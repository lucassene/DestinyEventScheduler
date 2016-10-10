package com.app.the.bunker.activities;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.app.the.bunker.Constants;
import com.app.the.bunker.R;
import com.app.the.bunker.dialogs.MyAlertDialog;
import com.app.the.bunker.interfaces.FromDialogListener;
import com.app.the.bunker.models.MultiChoiceItemModel;
import com.app.the.bunker.services.AlarmReceiver;
import com.app.the.bunker.services.BungieService;
import com.app.the.bunker.services.RequestResultReceiver;

import java.util.Calendar;
import java.util.List;

import static com.app.the.bunker.activities.DrawerActivity.DEFAULT_INTERVAL;
import static com.app.the.bunker.activities.DrawerActivity.NEW_NOTIFY_PREF;
import static com.app.the.bunker.activities.DrawerActivity.NEW_NOTIFY_TIME_PREF;
import static com.app.the.bunker.activities.DrawerActivity.SHARED_PREFS;

public class PrepareActivity extends AccountAuthenticatorActivity implements RequestResultReceiver.Receiver, FromDialogListener {

    private static final String TAG = "PrepareActivity";

    public static final String LEGEND_PREF = "prepareLegend";
    public static final String PREPARE_PREF = "prepareRunning";
    public static final String MSG_SHOWED_PREF = "msgShowed";

    public static final String TYPE = "type";
    public static final int TYPE_LOGIN = 1;
    public static final int TYPE_RENEW_TOKEN = 2;

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

        switch (getIntent().getIntExtra(TYPE,1)){
            case TYPE_LOGIN:
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
                } else Log.w(TAG, "Receiver is null or BungieService is running.");
                break;
            case TYPE_RENEW_TOKEN:
                //Esconder todo o layout, deixando apenas o logo do app
                //Exibir dialog informando que só é possível ter uma conta e dando opção para logoff
                break;
            default:
                finish();
                break;
        }
    }

    public boolean isBungieServiceRunning() {
        SharedPreferences sharedPrefs = getApplicationContext().getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        return sharedPrefs.getBoolean(BungieService.RUNNING_SERVICE, false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sharedPrefs = getApplicationContext().getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putBoolean(PREPARE_PREF, true);
        editor.putBoolean(DrawerActivity.FOREGROUND_PREF, true);
        editor.apply();
        msg = sharedPrefs.getString(LEGEND_PREF, getString(R.string.vanguard_data));
        text.setText(msg);
    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences sharedPrefs = getApplicationContext().getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putBoolean(DrawerActivity.FOREGROUND_PREF, false);
        editor.putString(LEGEND_PREF, msg);
        editor.apply();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.w(TAG, "PrepareActivity destroyed!");
        SharedPreferences sharedPrefs = getApplicationContext().getSharedPreferences(DrawerActivity.SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putBoolean(PrepareActivity.PREPARE_PREF, false);
        editor.putString(LEGEND_PREF, getString(R.string.vanguard_data));
        editor.apply();
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
            case BungieService.STATUS_LOGIN:
                Log.w(TAG, "Creating " + userName + " account...");
                Intent intent = new Intent();
                intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, Constants.ACC_TYPE);
                intent.putExtra(AccountManager.KEY_ACCOUNT_NAME, userName);
                intent.putExtra(AccountManager.KEY_AUTHTOKEN, resultData.getString("authKey"));
                authorize(intent);
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
            case BungieService.STATUS_EVENTS:
                msg = getString(R.string.updating_database);
                text.setText(msg);
                break;
            case BungieService.STATUS_FINISHED:
                showAlertDialog();
                progressBar.setVisibility(View.GONE);
                text.setVisibility(View.GONE);
                break;
        }
    }

    private void authorize(Intent intent) {
        AccountManager mAccManager = AccountManager.get(this);
        String accType = intent.getStringExtra(AccountManager.KEY_ACCOUNT_TYPE);
        String accName = intent.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
        String accToken = intent.getStringExtra(AccountManager.KEY_AUTHTOKEN);
        Account mAcc = new Account(accName, accType);

        if (!accToken.equalsIgnoreCase("null")){
            mAccManager.addAccountExplicitly(mAcc, null, null);
            mAccManager.setAuthToken(mAcc, accType, accToken);
            setAccountAuthenticatorResult(intent.getExtras());
            Log.w(TAG, "Account created!");
        }
    }

    private void showAlertDialog() {
        DialogFragment dialog = new MyAlertDialog();
        Bundle bundle = new Bundle();
        bundle.putInt("type",MyAlertDialog.SHARE_DIALOG);

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
            case BungieService.ERROR_AUTH:
                bundle.putString("title",getString(R.string.error));
                bundle.putString("msg",getString(R.string.error_bungie_auth));
                bundle.putString("posButton",getString(R.string.got_it));
                break;
            case BungieService.NO_ERROR:
                SharedPreferences prefs = getSharedPreferences(DrawerActivity.SHARED_PREFS, Context.MODE_PRIVATE);
                boolean showed = prefs.getBoolean(MSG_SHOWED_PREF, false);
                if (!showed){
                    Log.w(TAG, "Dialog not showed before");
                    bundle.putString("title", getString(R.string.just_warning));
                    bundle.putString("msg", getString(R.string.first_msg));
                    bundle.putString("posButton", getString(R.string.invite));
                    bundle.putString("negButton", getString(R.string.not_now));
                } else {
                    Log.w(TAG, "Dialog showed is true");
                    bundle.clear();
                    openDrawerActivity(false);
                }
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

    public void openDrawerActivity(boolean share){

        SharedPreferences sharedPrefs = getSharedPreferences(DrawerActivity.SHARED_PREFS, Context.MODE_PRIVATE);

        SharedPreferences.Editor sharedEditor = sharedPrefs.edit();
        sharedEditor.putBoolean(MSG_SHOWED_PREF, true);
        sharedEditor.putBoolean(DrawerActivity.SOUND_PREF, true);
        sharedEditor.putBoolean(DrawerActivity.SCHEDULED_NOTIFY_PREF, true);
        sharedEditor.putBoolean(DrawerActivity.NEW_NOTIFY_PREF, true);
        sharedEditor.putInt(DrawerActivity.SCHEDULED_TIME_PREF, 0);
        sharedEditor.putInt(DrawerActivity.NEW_NOTIFY_TIME_PREF, DEFAULT_INTERVAL);
        sharedEditor.putString(DrawerActivity.MEMBER_PREF, membershipId);
        sharedEditor.putInt(DrawerActivity.PLATFORM_PREF, platformId);
        sharedEditor.putString(DrawerActivity.CLAN_PREF, clanId);
        sharedEditor.apply();

        registerNewGamesAlarm();
        if (sharedPrefs.getBoolean(DrawerActivity.FOREGROUND_PREF, false)){
            Intent intent = new Intent(this, DrawerActivity.class);
            Log.w(TAG, "share? " + share);
            intent.putExtra("share", share);
            startActivity(intent);
            finish();
        }
    }

    public void registerNewGamesAlarm() {
        SharedPreferences sharedPrefs = getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        if (sharedPrefs.getBoolean(NEW_NOTIFY_PREF, false)) {
            int interval = sharedPrefs.getInt(NEW_NOTIFY_TIME_PREF, DEFAULT_INTERVAL);
            Intent intent = new Intent(this, AlarmReceiver.class);
            intent.putExtra(AlarmReceiver.TYPE_HEADER, AlarmReceiver.TYPE_NEW_NOTIFICATIONS);
            intent.putExtra("memberId", membershipId);
            intent.putExtra("platformId", platformId);
            PendingIntent pIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            AlarmManager alarm = (AlarmManager) getSystemService(ALARM_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
                Intent nIntent = new Intent(this, DrawerActivity.class);
                nIntent.putExtra("isFromNews", true);
                PendingIntent npIntent = PendingIntent.getActivity(this, 0, nIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                AlarmManager.AlarmClockInfo aC = new AlarmManager.AlarmClockInfo(System.currentTimeMillis() + interval, npIntent);
                alarm.setAlarmClock(aC, pIntent);
            } else alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + interval, interval, pIntent);
            Log.w(TAG, "New game alarm registered in an interval of " + interval + " millis");
        }
    }

    @Override
    public void onPositiveClick(String input, int type) {
        if (errorCode == BungieService.NO_ERROR){
            switch (type){
                case MyAlertDialog.SHARE_DIALOG:
                    openDrawerActivity(true);
                    break;
                case MyAlertDialog.NOT_SHARE_DIALOG:
                default:
                    openDrawerActivity(false);
                    break;
            }
        } else backToLoginActivity();
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

    @Override
    public void onListChecked(List<MultiChoiceItemModel> list) {

    }
}
