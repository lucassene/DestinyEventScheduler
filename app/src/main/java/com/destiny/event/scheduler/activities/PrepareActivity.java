package com.destiny.event.scheduler.activities;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.destiny.event.scheduler.R;
import com.destiny.event.scheduler.dialogs.MyAlertDialog;
import com.destiny.event.scheduler.interfaces.FromDialogListener;
import com.destiny.event.scheduler.services.BungieService;
import com.destiny.event.scheduler.services.RequestResultReceiver;

import java.util.Calendar;

public class PrepareActivity extends AppCompatActivity implements RequestResultReceiver.Receiver, FromDialogListener {

    private static final String TAG = "PrepareActivity";

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

    TextView txt6;

    String membershipId;
    String userName;

    int errorCode;
    boolean backError = false;

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

        txt6 = (TextView) findViewById(R.id.txt6);


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

                switch (resultData.getInt(BungieService.ERROR_TAG)){
                    case (BungieService.ERROR_NO_CONNECTION):
                        //Toast.makeText(this, "Verificar conexão com a Internet", Toast.LENGTH_SHORT).show();
                        errorCode = BungieService.ERROR_NO_CONNECTION;
                        break;
                    case (BungieService.ERROR_HTTP_REQUEST):
                        //Toast.makeText(this, "Problema com o HTTP Request", Toast.LENGTH_SHORT).show();
                        errorCode = BungieService.ERROR_HTTP_REQUEST;
                        break;
                    case (BungieService.ERROR_RESPONSE_CODE):
                        //Toast.makeText(this, "Response code diferente de 200", Toast.LENGTH_SHORT).show();
                        errorCode = BungieService.ERROR_RESPONSE_CODE;
                        break;
                }

                backError = true;
                showAlertDialog();
                progressBar.setVisibility(View.GONE);
                break;
            case BungieService.STATUS_DOCS:

                if (resultData.getInt(BungieService.ERROR_TAG) == BungieService.ERROR_CURRENT_USER){
                    img1.clearColorFilter();
                    img1.setImageResource(R.drawable.ic_error);
                    errorCode = BungieService.ERROR_CURRENT_USER;
                    break;
                } else img1.setColorFilter(ContextCompat.getColor(this, R.color.colorAccent), PorterDuff.Mode.SRC_ATOP);

                membershipId = resultData.getString("bungieId");
                userName = resultData.getString("userName");
                lay2.setVisibility(View.VISIBLE);
                img2.setColorFilter(ContextCompat.getColor(this, android.R.color.black), PorterDuff.Mode.SRC_ATOP);
                break;
            case BungieService.STATUS_VERIFY:
                img2.setColorFilter(ContextCompat.getColor(this, R.color.colorAccent), PorterDuff.Mode.SRC_ATOP);
                lay3.setVisibility(View.VISIBLE);
                img3.setColorFilter(ContextCompat.getColor(this, android.R.color.black), PorterDuff.Mode.SRC_ATOP);
                break;
            case BungieService.STATUS_PICTURE:

                if (resultData.getInt(BungieService.ERROR_TAG) == BungieService.ERROR_NO_ICON){
                    img3.clearColorFilter();
                    img3.setImageResource(R.drawable.ic_warning);
                    errorCode = BungieService.ERROR_NO_ICON;
                } else {
                    img3.setColorFilter(ContextCompat.getColor(this, R.color.colorAccent), PorterDuff.Mode.SRC_ATOP);
                }
                //img3.setColorFilter(ContextCompat.getColor(this, R.color.accent_material_light), PorterDuff.Mode.MULTIPLY);
                lay4.setVisibility(View.VISIBLE);
                img4.setColorFilter(ContextCompat.getColor(this, android.R.color.black), PorterDuff.Mode.SRC_ATOP);

                break;
            case BungieService.STATUS_FRIENDS:

                if (resultData.getInt(BungieService.ERROR_TAG) == BungieService.ERROR_NO_CLAN){
                    img4.clearColorFilter();
                    img4.setImageResource(R.drawable.ic_error);
                    errorCode = BungieService.ERROR_NO_CLAN;
                    break;
                } else img4.setColorFilter(ContextCompat.getColor(this, R.color.colorAccent), PorterDuff.Mode.SRC_ATOP);
                lay5.setVisibility(View.VISIBLE);
                img5.setColorFilter(ContextCompat.getColor(this, android.R.color.black), PorterDuff.Mode.SRC_ATOP);

                break;
            case BungieService.STATUS_PARTY:

                switch (resultData.getInt(BungieService.ERROR_TAG)){
                    case 0:
                        img5.setColorFilter(ContextCompat.getColor(this, R.color.colorAccent), PorterDuff.Mode.SRC_ATOP);
                        txt6.setText("Criando banco de dados de teste...");
                        lay6.setVisibility(View.VISIBLE);
                        img6.setColorFilter(ContextCompat.getColor(this, android.R.color.black), PorterDuff.Mode.SRC_ATOP);
                        break;
                    case BungieService.ERROR_NO_ICON:
                        img5.setImageResource(R.drawable.ic_warning);
                        img5.setColorFilter(null);
                        txt6.setText("Criando banco de dados de teste...");
                        lay6.setVisibility(View.VISIBLE);
                        img6.setColorFilter(ContextCompat.getColor(this, android.R.color.black), PorterDuff.Mode.SRC_ATOP);
                        errorCode = BungieService.ERROR_NO_ICON;
                        break;
                    case BungieService.ERROR_NO_CONNECTION:
                        img5.clearColorFilter();
                        img5.setImageResource(R.drawable.ic_error);
                        errorCode = BungieService.ERROR_NO_CONNECTION;
                        break;
                    case BungieService.ERROR_HTTP_REQUEST:
                        img5.clearColorFilter();
                        img5.setImageResource(R.drawable.ic_error);
                        errorCode = BungieService.ERROR_HTTP_REQUEST;
                        break;
                    case BungieService.ERROR_RESPONSE_CODE:
                        img5.clearColorFilter();
                        img5.setImageResource(R.drawable.ic_error);
                        errorCode = BungieService.ERROR_RESPONSE_CODE;
                        break;
                    case BungieService.ERROR_CLAN_MEMBER:
                        img5.clearColorFilter();
                        img5.setImageResource(R.drawable.ic_error);
                        errorCode = BungieService.ERROR_CLAN_MEMBER;
                        break;
                    case BungieService.ERROR_MEMBERS_OF_CLAN:
                        img5.clearColorFilter();
                        img5.setImageResource(R.drawable.ic_error);
                        errorCode = BungieService.ERROR_MEMBERS_OF_CLAN;
                        break;
                }

                break;
            case BungieService.STATUS_FINISHED:
                img6.setColorFilter(ContextCompat.getColor(this, R.color.colorAccent), PorterDuff.Mode.SRC_ATOP);
                if (errorCode != 0){
                    showAlertDialog();
                }
                beginButton.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                break;
        }

    }

    private void showAlertDialog() {
        DialogFragment dialog = new MyAlertDialog();
        Bundle bundle = new Bundle();
        bundle.putInt("type",MyAlertDialog.ALERT_DIALOG);

        switch (errorCode){
            case BungieService.ERROR_NO_ICON:
                bundle.putString("title",getString(R.string.just_warning));
                bundle.putString("msg",getString(R.string.no_icon_msg));
                bundle.putString("posButton",getString(R.string.got_it));
                break;
            case BungieService.ERROR_NO_CONNECTION:
                bundle.putString("title",getString(R.string.error));
                bundle.putString("msg",getString(R.string.no_connection_msg));
                bundle.putString("posButon",getString(R.string.got_it));
                break;
            case BungieService.ERROR_HTTP_REQUEST:
            case BungieService.ERROR_RESPONSE_CODE:
            case BungieService.ERROR_CLAN_MEMBER:
            case BungieService.ERROR_CURRENT_USER:
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
            default:
                bundle.putString("title",getString(R.string.error));
                bundle.putString("msg", getString(R.string.some_problem_msg));
                bundle.putString("posButton", getString(R.string.got_it));
                break;
        }

        dialog.setArguments(bundle);
        dialog.show(getSupportFragmentManager(),"alert");
    }

    public void onBegin(View view) {
        openDrawerActivity();
    }

    public void openDrawerActivity(){
        Intent intent = new Intent(this, DrawerActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onPositiveClick(String input, int type) {
        if (!backError) {
            openDrawerActivity();
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
    public void onItemSelected(String entry, int value) {

    }

    @Override
    public void onMultiItemSelected(boolean[] items) {

    }
}
