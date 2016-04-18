package com.destiny.event.scheduler.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.destiny.event.scheduler.R;
import com.destiny.event.scheduler.services.BungieService;
import com.destiny.event.scheduler.services.RequestResultReceiver;
import com.google.android.gms.common.api.GoogleApiClient;

public class PrepareActivity extends Activity implements RequestResultReceiver.Receiver {

    ProgressBar progressBar;
    Button beginButton;

    LinearLayout lay1;
    LinearLayout lay2;
    LinearLayout lay3;
    LinearLayout lay4;
    LinearLayout lay5;
    LinearLayout lay6;

    ImageView img1;
    ImageView img2;
    ImageView img3;
    ImageView img4;
    ImageView img5;
    ImageView img6;

    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.prepare_layout);

        progressBar = (ProgressBar) findViewById(R.id.progress);
        beginButton = (Button) findViewById(R.id.btn_ok);

        lay1 = (LinearLayout) findViewById(R.id.lay1);
        lay2 = (LinearLayout) findViewById(R.id.lay2);
        lay3 = (LinearLayout) findViewById(R.id.lay3);
        lay4 = (LinearLayout) findViewById(R.id.lay4);
        lay5 = (LinearLayout) findViewById(R.id.lay5);
        lay6 = (LinearLayout) findViewById(R.id.lay6);

        img1 = (ImageView) findViewById(R.id.img1);
        img2 = (ImageView) findViewById(R.id.img2);
        img3 = (ImageView) findViewById(R.id.img3);
        img4 = (ImageView) findViewById(R.id.img4);
        img5 = (ImageView) findViewById(R.id.img5);
        img6 = (ImageView) findViewById(R.id.img6);


        img1.setColorFilter(ContextCompat.getColor(this, android.R.color.black), PorterDuff.Mode.MULTIPLY);

        String cookies = getIntent().getStringExtra("cookies");
        String xcsrf = getIntent().getStringExtra("x-csrf");
        String platform = getIntent().getStringExtra("platform");

        if (savedInstanceState == null){

            RequestResultReceiver mReceiver = new RequestResultReceiver(new Handler());
            mReceiver.setReceiver(this);
            Intent intent = new Intent(Intent.ACTION_SYNC, null, this, BungieService.class);
            intent.putExtra(BungieService.REQUEST_EXTRA, BungieService.GET_CURRENT_ACCOUNT);
            intent.putExtra(BungieService.COOKIE_EXTRA, cookies);
            intent.putExtra(BungieService.RECEIVER_EXTRA, mReceiver);
            intent.putExtra(BungieService.XCSRF_EXTRA, xcsrf);
            intent.putExtra(BungieService.PLATFORM_EXTRA, platform);
            startService(intent);

        }
    }

    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {
        switch (resultCode) {
            case BungieService.STATUS_RUNNING:
                progressBar.setVisibility(View.VISIBLE);
                break;
            case BungieService.STATUS_ERROR:
                Toast.makeText(this, "Erro ao receber o resultado!", Toast.LENGTH_SHORT).show();
                break;
            case BungieService.STATUS_DOCS:
                img1.setColorFilter(ContextCompat.getColor(this, R.color.accent_material_light), PorterDuff.Mode.MULTIPLY);
                lay2.setVisibility(View.VISIBLE);
                img2.setColorFilter(ContextCompat.getColor(this, android.R.color.black), PorterDuff.Mode.MULTIPLY);
                break;
            case BungieService.STATUS_VERIFY:
                img2.setColorFilter(ContextCompat.getColor(this, R.color.accent_material_light), PorterDuff.Mode.MULTIPLY);
                lay3.setVisibility(View.VISIBLE);
                img3.setColorFilter(ContextCompat.getColor(this, android.R.color.black), PorterDuff.Mode.MULTIPLY);
                break;
            case BungieService.STATUS_PICTURE:
                img3.setColorFilter(ContextCompat.getColor(this, R.color.accent_material_light), PorterDuff.Mode.MULTIPLY);
                lay4.setVisibility(View.VISIBLE);
                img4.setColorFilter(ContextCompat.getColor(this, android.R.color.black), PorterDuff.Mode.MULTIPLY);
                break;
            case BungieService.STATUS_FRIENDS:
                img4.setColorFilter(ContextCompat.getColor(this, R.color.accent_material_light), PorterDuff.Mode.MULTIPLY);
                lay5.setVisibility(View.VISIBLE);
                img5.setColorFilter(ContextCompat.getColor(this, android.R.color.black), PorterDuff.Mode.MULTIPLY);
                break;
            case BungieService.STATUS_PARTY:
                img5.setColorFilter(ContextCompat.getColor(this, R.color.accent_material_light), PorterDuff.Mode.MULTIPLY);
                lay6.setVisibility(View.VISIBLE);
                img6.setColorFilter(ContextCompat.getColor(this, android.R.color.black), PorterDuff.Mode.MULTIPLY);
                break;
            case BungieService.STATUS_FINISHED:
                img6.setColorFilter(ContextCompat.getColor(this, R.color.accent_material_light), PorterDuff.Mode.MULTIPLY);
                beginButton.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                break;
        }

    }

    public void onBegin(View view) {
        Intent intent = new Intent(this, DrawerActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();

    }
}
