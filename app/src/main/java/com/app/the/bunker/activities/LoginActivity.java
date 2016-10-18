package com.app.the.bunker.activities;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.app.the.bunker.BuildConfig;
import com.app.the.bunker.Constants;
import com.app.the.bunker.R;
import com.app.the.bunker.data.DBHelper;
import com.app.the.bunker.dialogs.MyAlertDialog;
import com.app.the.bunker.interfaces.FromDialogListener;
import com.app.the.bunker.models.MultiChoiceItemModel;
import com.app.the.bunker.models.NoticeModel;
import com.app.the.bunker.services.BungieService;
import com.app.the.bunker.services.RequestResultReceiver;
import com.app.the.bunker.services.ServerService;
import com.app.the.bunker.utils.CookiesUtils;
import com.app.the.bunker.utils.NetworkUtils;

import java.util.Calendar;
import java.util.List;

public class LoginActivity extends AppCompatActivity implements RequestResultReceiver.Receiver, FromDialogListener {

    private static final String TAG = "LoginActivity";

    private static final String PSN_URL = "http://www.bungie.net/en/User/SignIn/Psnid";
    private static final String LIVE_URL = "http://www.bungie.net/en/User/SignIn/Xuid";
    private static final int PSN = 2;
    private static final int LIVE = 1;
    private static final int LOGIN = 1;

    Button psnButton;
    Button liveButton;
    TextView loginTitle;
    ProgressBar progressBar;
    LinearLayout buttonsLayout;

    private String bungieId;
    private String userName;
    private int platformId;

    private int selectedPlatform;

    RequestResultReceiver mReceiver;

    private int errorCode;
    private boolean showButtons = false;
    private boolean showProgressBar = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.login_layout);

        psnButton = (Button) findViewById(R.id.btn_psn);
        liveButton = (Button) findViewById(R.id.btn_live);
        loginTitle = (TextView) findViewById(R.id.login_title);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        buttonsLayout = (LinearLayout) findViewById(R.id.buttons_layout);

        checkAccount();

        if (savedInstanceState != null){
            showButtons = savedInstanceState.getBoolean("showButtons");
            showProgressBar = savedInstanceState.getBoolean("showProgress");
        }

    }

    private void prepareDatabase(){
        CookiesUtils.clearCookies();
        DBHelper database = new DBHelper(getApplicationContext());
        SQLiteDatabase db = database.getWritableDatabase();
        database.onUpgrade(db, 0, 0);
        db.close();
        database.close();
    }

    private void checkAccount(){
        AccountManager manager = AccountManager.get(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.GET_ACCOUNTS) != PackageManager.PERMISSION_GRANTED) {}
        Account[] accs = manager.getAccountsByType(Constants.ACC_TYPE);
        Log.w(TAG, "Number of accounts found: " + accs.length);
        SharedPreferences prefs = getSharedPreferences(Constants.SHARED_PREFS, Context.MODE_PRIVATE);
        userName = prefs.getString(Constants.USERNAME_PREF, "");
        String clanId = prefs.getString(Constants.CLAN_PREF, "");
        boolean accFound = false;
        for (Account acc : accs) {
            if (acc.name.equals(userName)) {
                Log.w(TAG, "user " + acc.name + " found.");
                bungieId = manager.getUserData(acc,Constants.ACC_MEMEBRSHIP);
                platformId = Integer.parseInt(manager.getUserData(acc, Constants.ACC_PLATFORM));
                if (!isServerServiceRunning()) {
                    getNotice(bungieId, platformId, clanId);
                } else {
                    isClanMember(bungieId, platformId);
                }
                accFound = true;
                break;
            }
        }
        if (!accFound){
            prepareDatabase();
            showButtons = true;
            buttonsLayout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("showButtons", showButtons);
        outState.putBoolean("showProgress", showProgressBar);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (getSharedPreferences(Constants.SHARED_PREFS, Context.MODE_PRIVATE).getBoolean(PrepareActivity.PREPARE_PREF,false) && isBungieServiceRunning()){
            Intent intent = new Intent(this, PrepareActivity.class);
            intent.putExtra(PrepareActivity.TYPE, PrepareActivity.TYPE_LOGIN);
            startActivity(intent);
            finish();
        }

        if (showButtons){
            if (buttonsLayout != null) {
                buttonsLayout.setVisibility(View.VISIBLE);
            }
        } else if (buttonsLayout != null){
            buttonsLayout.setVisibility(View.GONE);
        }

        if (showProgressBar){
            progressBar.setVisibility(View.VISIBLE);
        } else progressBar.setVisibility(View.GONE);

    }

    public boolean isBungieServiceRunning() {
        SharedPreferences sharedPrefs = getSharedPreferences(Constants.SHARED_PREFS, Context.MODE_PRIVATE);
        return sharedPrefs.getBoolean(BungieService.RUNNING_SERVICE, false);
    }

    public void callWebView(View v) {
        if (NetworkUtils.checkConnection(getApplicationContext())){
            if (v.getId() == R.id.btn_psn) {
                selectedPlatform = PSN;
                Intent intent = new Intent(this, WebActivity.class);
                intent.putExtra("url",PSN_URL);
                startActivityForResult(intent,LOGIN);
            } else if (v.getId() == R.id.btn_live){
                selectedPlatform = LIVE;
                Intent intent = new Intent(this, WebActivity.class);
                intent.putExtra("url",LIVE_URL);
                startActivityForResult(intent,LOGIN);
            }
        } else {
            Toast.makeText(this, R.string.connection_needed_login, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == LOGIN){
            if (resultCode == Activity.RESULT_OK){
                String cookies = data.getStringExtra("cookies");
                String xcsrf = data.getStringExtra("x-csrf");

                SharedPreferences sharedPrefs = getSharedPreferences(Constants.SHARED_PREFS, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPrefs.edit();
                editor.putString(Constants.COOKIES_PREF, cookies);
                editor.putString(Constants.XCSRF_PREF, xcsrf);
                editor.apply();

                if (errorCode != BungieService.ERROR_AUTH){
                    Intent intent = new Intent(this, PrepareActivity.class);
                    intent.putExtra(PrepareActivity.TYPE, PrepareActivity.TYPE_LOGIN);
                    //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra("cookies",cookies);
                    intent.putExtra("x-csrf",xcsrf);
                    intent.putExtra("platform",selectedPlatform);
                    startActivity(intent);
                    finish();
                } else {
                    showProgressBar = false;
                    progressBar.setVisibility(View.GONE);
                    Intent intent = new Intent(this, DrawerActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra("bungieId",bungieId);
                    intent.putExtra("userName",userName);
                    startActivity(intent);
                    finish();
                }
                //Toast.makeText(this, "Dados do result: " + result, Toast.LENGTH_SHORT).show();
            }
        }
    }


    public boolean isServerServiceRunning() {
        SharedPreferences sharedPrefs = getSharedPreferences(Constants.SHARED_PREFS, Context.MODE_PRIVATE);
        return sharedPrefs.getBoolean(ServerService.RUNNING_SERVICE, false);
    }

    private void getNotice(String bungieId, int platform, String clanId) {
        if (mReceiver == null) {
            mReceiver = new RequestResultReceiver(new Handler());
            mReceiver.setReceiver(this);
        }
        Intent intent = new Intent(Intent.ACTION_SYNC, null, this, ServerService.class);
        intent.putExtra(ServerService.REQUEST_TAG, ServerService.TYPE_NOTICE);
        intent.putExtra(ServerService.PLATFORM_TAG, platform);
        intent.putExtra(ServerService.MEMBER_TAG, bungieId);
        intent.putExtra(ServerService.RECEIVER_TAG, mReceiver);
        intent.putExtra(ServerService.CLAN_TAG, clanId);
        startService(intent);
    }

    private void isClanMember(String bungieId, int platform) {
        if (!isBungieServiceRunning()){
            if (mReceiver == null) {
                mReceiver = new RequestResultReceiver(new Handler());
                mReceiver.setReceiver(this);
            }
            Intent intent = new Intent(Intent.ACTION_SYNC, null, this, BungieService.class);
            intent.putExtra(BungieService.REQUEST_EXTRA, BungieService.TYPE_VERIFY_MEMBER);
            intent.putExtra(BungieService.PLATFORM_EXTRA, platform);
            intent.putExtra(BungieService.MEMBERSHIP_EXTRA, bungieId);
            intent.putExtra(BungieService.RECEIVER_EXTRA, mReceiver);

            SharedPreferences sharedPrefs = getSharedPreferences(Constants.SHARED_PREFS, Context.MODE_PRIVATE);
            String cookies = sharedPrefs.getString(Constants.COOKIES_PREF, null);
            String xcsrf = sharedPrefs.getString(Constants.XCSRF_PREF, null);

            if (cookies != null && xcsrf != null){
                intent.putExtra(BungieService.COOKIE_EXTRA, cookies);
                intent.putExtra(BungieService.XCSRF_EXTRA, xcsrf);
                startService(intent);
            } else Log.w(TAG, "Cookies or X-CSRF are null");
        }
    }

    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {
        switch (resultCode){
            case BungieService.STATUS_RUNNING:
            case ServerService.STATUS_RUNNING:
                showProgressBar = true;
                progressBar.setVisibility(View.VISIBLE);
                break;
            case BungieService.STATUS_ERROR:
                switch (resultData.getInt(BungieService.ERROR_TAG)){
                    case BungieService.ERROR_NO_CLAN:
                        errorCode = BungieService.ERROR_NO_CLAN;
                        break;
                    case BungieService.ERROR_AUTH:
                        errorCode = BungieService.ERROR_AUTH;
                        break;
                }
                Log.w(TAG, "Erro ao receber dados do getBungieAccount: " + resultData.getInt(BungieService.ERROR_TAG));
                showProgressBar = false;
                progressBar.setVisibility(View.GONE);
                if (errorCode == BungieService.ERROR_NO_CLAN || errorCode == BungieService.ERROR_AUTH) {
                    showAlertDialog();
                } else {
                    callDrawerActivity();
                }
                break;
            case ServerService.STATUS_ERROR:
                Log.w(TAG, "Error connecting with server (" + resultData.getInt(ServerService.ERROR_TAG)+").");
                isClanMember(bungieId, platformId);
                break;
            case ServerService.STATUS_FINISHED:
                if (resultData.getInt(ServerService.REQUEST_TAG) == ServerService.TYPE_NOTICE){
                    NoticeModel notice = (NoticeModel) resultData.getSerializable(ServerService.NOTICE_TAG);
                    if (notice != null){
                        if (notice.getVersionCode() > BuildConfig.VERSION_CODE){
                            callUpdateActivity(notice);
                        } else isClanMember(bungieId, platformId);
                    } else {
                        Log.w(TAG, "Notice is null.");
                        isClanMember(bungieId, platformId);
                    }
                } else isClanMember(bungieId, platformId);
                break;
            default:
                callDrawerActivity();
                break;
        }
    }

    private void callUpdateActivity(NoticeModel notice) {
        showProgressBar = false;
        progressBar.setVisibility(View.GONE);
        Intent intent = new Intent(this, NoticeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("notice",notice);
        intent.putExtra("bungieId",bungieId);
        intent.putExtra("userName",userName);
        startActivity(intent);
        finish();
    }

    private void callDrawerActivity(){
        showProgressBar = false;
        progressBar.setVisibility(View.GONE);
        Intent intent = new Intent(this, DrawerActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    private void showAlertDialog() {
        DialogFragment dialog = new MyAlertDialog();
        Bundle bundle = new Bundle();
        bundle.putInt("type",MyAlertDialog.ALERT_DIALOG);
        switch (errorCode){
            case BungieService.ERROR_HTTP_REQUEST:
                bundle.putString("title",getString(R.string.error));
                bundle.putString("msg",getString(R.string.bungie_net_error_msg));
                bundle.putString("posButton",getString(R.string.got_it));
                break;
            case BungieService.ERROR_NO_CLAN:
                bundle.putString("title",getString(R.string.no_clan_title));
                bundle.putString("msg",getString(R.string.no_clan_login_msg));
                bundle.putString("posButton", getString(R.string.got_it));
                break;
            case BungieService.ERROR_NO_CONNECTION:
                bundle.putString("title",getString(R.string.error));
                bundle.putString("msg",getString(R.string.no_connection_msg));
                bundle.putString("posButton",getString(R.string.got_it));
                break;
            case BungieService.ERROR_AUTH:
                bundle.putString("title",getString(R.string.error));
                bundle.putString("msg", getString(R.string.credentials_expired));
                bundle.putString("posButton",getString(R.string.got_it));
                break;
        }
        dialog.setArguments(bundle);
        dialog.show(getSupportFragmentManager(),"alert");
    }

    @Override
    public void onPositiveClick(String input, int type) {
        switch (errorCode){
            case BungieService.ERROR_NO_CLAN:
                CookiesUtils.clearCookies();
                DBHelper database = new DBHelper(getApplicationContext());
                SQLiteDatabase db = database.getWritableDatabase();
                database.onUpgrade(db, 0, 0);
                showButtons = true;
                buttonsLayout.setVisibility(View.VISIBLE);
                break;
            case BungieService.ERROR_AUTH:
                prepareDatabase();
                showButtons = true;
                buttonsLayout.setVisibility(View.VISIBLE);
                break;
        }
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
