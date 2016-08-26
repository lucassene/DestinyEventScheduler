package com.destiny.event.scheduler.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.destiny.event.scheduler.R;
import com.destiny.event.scheduler.data.DBHelper;
import com.destiny.event.scheduler.data.LoggedUserTable;
import com.destiny.event.scheduler.dialogs.MyAlertDialog;
import com.destiny.event.scheduler.interfaces.FromDialogListener;
import com.destiny.event.scheduler.provider.DataProvider;
import com.destiny.event.scheduler.services.BungieService;
import com.destiny.event.scheduler.services.RequestResultReceiver;
import com.destiny.event.scheduler.utils.CookiesUtils;
import com.destiny.event.scheduler.utils.NetworkUtils;

import java.util.Calendar;

public class LoginActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>, RequestResultReceiver.Receiver, FromDialogListener {

    private static final String TAG = "LoginActivity";
    private static final int URL_LOADER_USER = 30;

    private static final String PSN_URL = "http://www.bungie.net/en/User/SignIn/Psnid";
    private static final String LIVE_URL = "http://www.bungie.net/en/User/SignIn/Xuid";
    private static final int PSN = 2;
    private static final int LIVE = 1;
    private static final int LOGIN = 1;

    Button psnButton;
    Button liveButton;
    TextView loginTitle;
    ProgressBar progressBar;

    private String bungieId;
    private String userName;

    private int selectedPlatform;

    RequestResultReceiver mReceiver;

    private int errorCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.login_layout);

        psnButton = (Button) findViewById(R.id.btn_psn);
        liveButton = (Button) findViewById(R.id.btn_live);
        loginTitle = (TextView) findViewById(R.id.login_title);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);

        loginTitle.setVisibility(View.GONE);
        psnButton.setVisibility(View.GONE);
        liveButton.setVisibility(View.GONE);

        getSupportLoaderManager().initLoader(URL_LOADER_USER, null, this);

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isBungieServiceRunning()){
            Intent intent = new Intent(this, PrepareActivity.class);
            startActivity(intent);
            finish();
        }
    }

    public boolean isBungieServiceRunning() {
        SharedPreferences sharedPrefs = getSharedPreferences(DrawerActivity.SHARED_PREFS, Context.MODE_PRIVATE);
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

                SharedPreferences sharedPrefs = getSharedPreferences(DrawerActivity.SHARED_PREFS, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPrefs.edit();
                editor.putString(DrawerActivity.COOKIES_PREF, cookies);
                editor.putString(DrawerActivity.XCSRF_PREF, xcsrf);
                editor.apply();

                if (errorCode != BungieService.ERROR_AUTH){
                    Intent intent = new Intent(this, PrepareActivity.class);
                    //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra("cookies",cookies);
                    intent.putExtra("x-csrf",xcsrf);
                    intent.putExtra("platform",selectedPlatform);
                    startActivity(intent);
                    finish();
                } else {
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

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection;

        switch (id) {
            case URL_LOADER_USER:
                projection = new String[]{LoggedUserTable.COLUMN_ID, LoggedUserTable.COLUMN_MEMBERSHIP, LoggedUserTable.COLUMN_NAME, LoggedUserTable.COLUMN_PLATFORM};
                return new CursorLoader(
                        this,
                        DataProvider.LOGGED_USER_URI,
                        projection,
                        null,
                        null,
                        null);
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        if (data != null && data.moveToFirst()){
            switch (loader.getId()) {
                case URL_LOADER_USER:
                    if (data.getCount() > 0) {
                        bungieId = data.getString(data.getColumnIndexOrThrow(LoggedUserTable.COLUMN_MEMBERSHIP));
                        userName = data.getString(data.getColumnIndexOrThrow(LoggedUserTable.COLUMN_NAME));
                        int platform = data.getInt(data.getColumnIndexOrThrow(LoggedUserTable.COLUMN_PLATFORM));
                        isClanMember(bungieId, platform);
                    }
                    break;
            }
        } else{
            loginTitle.setVisibility(View.VISIBLE);
            psnButton.setVisibility(View.VISIBLE);
            liveButton.setVisibility(View.VISIBLE);
        }

    }

    private void isClanMember(String bungieId, int platform) {

        if (mReceiver == null){
            mReceiver = new RequestResultReceiver(new Handler());
            mReceiver.setReceiver(this);
            Intent intent = new Intent(Intent.ACTION_SYNC, null, this, BungieService.class);
            intent.putExtra(BungieService.REQUEST_EXTRA, BungieService.TYPE_VERIFY_MEMBER);
            intent.putExtra(BungieService.PLATFORM_EXTRA, platform);
            intent.putExtra(BungieService.MEMBERSHIP_EXTRA, bungieId);
            intent.putExtra(BungieService.RECEIVER_EXTRA, mReceiver);

            SharedPreferences sharedPrefs = getSharedPreferences(DrawerActivity.SHARED_PREFS, Context.MODE_PRIVATE);
            String cookies = sharedPrefs.getString(DrawerActivity.COOKIES_PREF, null);
            String xcsrf = sharedPrefs.getString(DrawerActivity.XCSRF_PREF, null);

            if (cookies != null && xcsrf != null){
                intent.putExtra(BungieService.COOKIE_EXTRA, cookies);
                intent.putExtra(BungieService.XCSRF_EXTRA, xcsrf);
                startService(intent);
            } else Log.w(TAG, "Cookies or X-CSRF are null");
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {
        switch (resultCode){
            case BungieService.STATUS_RUNNING:
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
                progressBar.setVisibility(View.GONE);
                if (errorCode == BungieService.ERROR_NO_CLAN || errorCode == BungieService.ERROR_AUTH) {
                    showAlertDialog();
                } else {
                    callDrawerActivity();
                }
                break;
            default:
                callDrawerActivity();
                break;
        }
    }

    private void callDrawerActivity(){
        progressBar.setVisibility(View.GONE);
        Intent intent = new Intent(this, DrawerActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("bungieId",bungieId);
        intent.putExtra("userName",userName);
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
                loginTitle.setVisibility(View.VISIBLE);
                psnButton.setVisibility(View.VISIBLE);
                liveButton.setVisibility(View.VISIBLE);
                break;
            case BungieService.ERROR_AUTH:
                CookiesUtils.clearCookies();
                loginTitle.setVisibility(View.VISIBLE);
                psnButton.setVisibility(View.VISIBLE);
                liveButton.setVisibility(View.VISIBLE);
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

}
