package com.destiny.event.scheduler.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.destiny.event.scheduler.R;

public class LoginActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.login_layout);
    }

    public void callWebView(View v) {
        Intent intent = new Intent(this, WebActivity.class);
        startActivity(intent);
    }

    public void goHome(View v) {
        Intent intent = new Intent(this, DrawerActivity.class);
        startActivity(intent);
    }
}
