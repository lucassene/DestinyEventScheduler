package com.destiny.event.scheduler.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.destiny.event.scheduler.R;

public class LoginActivity extends Activity {

    private static final String PSN_URL = "http://www.bungie.net/en/User/SignIn/Psnid";
    private static final String LIVE_URL = "http://www.bungie.net/en/User/SignIn/Xuid";
    private static final int LOGIN = 1;

    Button psnButton;
    Button liveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.login_layout);

        psnButton = (Button) findViewById(R.id.btn_psn);
        liveButton = (Button) findViewById(R.id.btn_live);

    }

    public void callWebView(View v) {
        if (v.getId() == R.id.btn_psn) {
            Intent intent = new Intent(this, WebActivity.class);
            intent.putExtra("url",PSN_URL);
            startActivityForResult(intent,LOGIN);
        } else if (v.getId() == R.id.btn_live){
            Intent intent = new Intent(this, WebActivity.class);
            intent.putExtra("url",LIVE_URL);
            startActivityForResult(intent,LOGIN);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == LOGIN){
            if (resultCode == Activity.RESULT_OK){
                String result = data.getStringExtra("cookies");
                Toast.makeText(this, "Dados do result: " + result, Toast.LENGTH_SHORT).show();
            }
            if (resultCode == Activity.RESULT_CANCELED){
                Toast.makeText(this, "Result falhou ou vazio.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
