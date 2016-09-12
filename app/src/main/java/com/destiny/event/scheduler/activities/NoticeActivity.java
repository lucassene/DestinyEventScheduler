package com.destiny.event.scheduler.activities;

import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.destiny.event.scheduler.R;
import com.destiny.event.scheduler.models.NoticeModel;

public class NoticeActivity extends AppCompatActivity{

    private static final String TAG = "NoticeActivity";

    TextView messageText;
    WebView webView;
    TextView buttonCancel;
    TextView buttonOK;
    TextView toolbarTitle;

    NoticeModel notice;
    String bungieId;
    String userName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notice_layout);

        messageText = (TextView) findViewById(R.id.notice_message);
        webView = (WebView) findViewById(R.id.notice_webview);
        buttonCancel = (TextView) findViewById(R.id.button_cancel);
        buttonOK = (TextView) findViewById(R.id.button_ok);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("");
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        Typeface font = Typeface.createFromAsset(getAssets(), "d.ttf");
        if (toolbar != null) toolbarTitle = (TextView) toolbar.findViewById(R.id.title);
        toolbarTitle.setTypeface(font);
        toolbarTitle.setText(R.string.update_needed);

        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient());

        Bundle bundle = getIntent().getExtras();
        bungieId = bundle.getString("bungieId");
        userName = bundle.getString("userName");
        notice = (NoticeModel) bundle.getSerializable("notice");
        if (notice != null){
            messageText.setText(notice.getMessage());
            webView.loadUrl(notice.getUrl());

            if (notice.isForceUpdate()){
                buttonCancel.setText(R.string.close_app);
                buttonOK.setText(R.string.update);
            } else {
                buttonCancel.setText(R.string.update_later);
                buttonOK.setText(R.string.update);
            }
        } else {
            Log.w(TAG, "Notice is null.");
            finish();
        }

        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (notice.isForceUpdate()){
                    finish();
                } else {
                    callDrawerActivity();
                }
            }
        });

        buttonOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callPlayStore();
            }
        });
    }

    private void callPlayStore() {
        final String appPackageName = getPackageName();
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
            finish();
        } catch (android.content.ActivityNotFoundException anfe) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
            finish();
        }
    }

    private void callDrawerActivity(){
        Intent intent = new Intent(this, DrawerActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("bungieId",bungieId);
        intent.putExtra("userName",userName);
        startActivity(intent);
        finish();
    }

}
