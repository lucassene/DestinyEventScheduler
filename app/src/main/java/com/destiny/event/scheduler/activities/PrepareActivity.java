package com.destiny.event.scheduler.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.destiny.event.scheduler.R;
import com.destiny.event.scheduler.services.DestinyService;
import com.destiny.event.scheduler.services.RequestResultReceiver;

public class PrepareActivity extends Activity implements RequestResultReceiver.Receiver{

    TextView guardianText;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.prepare_layout);

        guardianText = (TextView) findViewById(R.id.label_guardian);
        progressBar = (ProgressBar) findViewById(R.id.progress);

        guardianText.setVisibility(View.GONE);

        String cookies = getIntent().getStringExtra("cookies");
        String xcsrf = getIntent().getStringExtra("x-csrf");

        RequestResultReceiver mReceiver = new RequestResultReceiver(new Handler());
        mReceiver.setReceiver(this);
        Intent intent = new Intent(Intent.ACTION_SYNC, null, this, DestinyService.class);
        intent.putExtra(DestinyService.REQUEST_EXTRA, DestinyService.GET_BUNGIE_ACCOUNT);
        intent.putExtra(DestinyService.COOKIE_EXTRA, cookies);
        intent.putExtra(DestinyService.RECEIVER_EXTRA, mReceiver);
        intent.putExtra(DestinyService.XCSRF_EXTRA, xcsrf);
        startService(intent);

    }

    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {
        switch (resultCode){
            case DestinyService.STATUS_RUNNING:
                progressBar.setVisibility(View.VISIBLE);
                break;
            case DestinyService.STATUS_ERROR:
                Toast.makeText(this, "Erro ao receber o resultado!", Toast.LENGTH_SHORT).show();
                break;
            case DestinyService.STATUS_FINISHED:
                progressBar.setVisibility(View.GONE);
                guardianText.setVisibility(View.VISIBLE);
                Toast.makeText(this, "Username: " + resultData.getString(DestinyService.NAME), Toast.LENGTH_SHORT).show();
                break;
        }

    }
}
