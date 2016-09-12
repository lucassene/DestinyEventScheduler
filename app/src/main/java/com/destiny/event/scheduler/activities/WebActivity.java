package com.destiny.event.scheduler.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.destiny.event.scheduler.R;
import com.destiny.event.scheduler.views.DestinyWebView;

import java.util.Locale;

public class WebActivity extends Activity implements DestinyWebView.DestinyListener {

    String url;
    DestinyWebView destinyWebView;
    TextView urlText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.webview_layout);

        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        Intent intent = getIntent();
        url = intent.getStringExtra("url");

        urlText = (TextView) findViewById(R.id.webview_text);

        String locale = Locale.getDefault().getLanguage();

        destinyWebView = (DestinyWebView) findViewById(R.id.webview);
        destinyWebView.clearUserCookies();
        destinyWebView.clearCache(true);
        destinyWebView.setHorizontalScrollBarEnabled(true);
        destinyWebView.setListener(this);
        destinyWebView.loadLoginUrl(url);

        destinyWebView = new DestinyWebView(getApplicationContext());

    }

    @Override
    public void onUserLoggedIn(String cookies, String crossRefToken) {
        //Toast.makeText(this, "User logged in: " + crossRefToken, Toast.LENGTH_SHORT).show();
        Intent intent = new Intent();
        intent.putExtra("cookies",cookies);
        intent.putExtra("x-csrf", crossRefToken);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    @Override
    public void onLoginFailed() {
        Toast.makeText(this, R.string.problem_bungie_net, Toast.LENGTH_SHORT).show();
        Intent intent = new Intent();
        setResult(Activity.RESULT_CANCELED, intent);
        finish();
    }

    @Override
    public void onPageChanged(String url) {
        urlText.setText(url);
    }

    public void onWebViewCancel(View view) {
        //Toast.makeText(this, "Login failed! User input.", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent();
        setResult(Activity.RESULT_CANCELED, intent);
        finish();
    }
}
