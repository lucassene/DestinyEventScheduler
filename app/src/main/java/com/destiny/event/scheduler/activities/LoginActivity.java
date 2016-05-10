package com.destiny.event.scheduler.activities;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.destiny.event.scheduler.R;
import com.destiny.event.scheduler.data.LoggedUserTable;
import com.destiny.event.scheduler.provider.DataProvider;
import com.destiny.event.scheduler.utils.NetworkUtils;

public class LoginActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int URL_LOADER_USER = 30;

    private static final String PSN_URL = "http://www.bungie.net/en/User/SignIn/Psnid";
    private static final String LIVE_URL = "http://www.bungie.net/en/User/SignIn/Xuid";
    private static final String PSN = "2";
    private static final String LIVE = "1";
    private static final int LOGIN = 1;

    Button psnButton;
    Button liveButton;
    TextView loginTitle;

    private String bungieId;
    private String userName;

    private String selectedPlatform;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.login_layout);

        psnButton = (Button) findViewById(R.id.btn_psn);
        liveButton = (Button) findViewById(R.id.btn_live);
        loginTitle = (TextView) findViewById(R.id.login_title);

        loginTitle.setVisibility(View.GONE);
        psnButton.setVisibility(View.GONE);
        liveButton.setVisibility(View.GONE);

        getSupportLoaderManager().initLoader(URL_LOADER_USER, null, this);

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
                Intent intent = new Intent(this, PrepareActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("cookies",cookies);
                intent.putExtra("x-csrf",xcsrf);
                intent.putExtra("platform",selectedPlatform);
                startActivity(intent);
                finish();
                //Toast.makeText(this, "Dados do result: " + result, Toast.LENGTH_SHORT).show();
            }
            if (resultCode == Activity.RESULT_CANCELED){
                //Toast.makeText(this, "Result falhou ou vazio.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection;

        switch (id) {
            case URL_LOADER_USER:
                projection = new String[]{LoggedUserTable.COLUMN_ID, LoggedUserTable.COLUMN_MEMBERSHIP, LoggedUserTable.COLUMN_NAME};
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
                        Intent intent = new Intent(this, DrawerActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.putExtra("bungieId",bungieId);
                        intent.putExtra("userName",userName);
                        startActivity(intent);
                        finish();
                    }
                    break;
            }
        } else{
            loginTitle.setVisibility(View.VISIBLE);
            psnButton.setVisibility(View.VISIBLE);
            liveButton.setVisibility(View.VISIBLE);
        }



    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
