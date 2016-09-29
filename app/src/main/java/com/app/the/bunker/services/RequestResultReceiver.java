package com.app.the.bunker.services;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

@SuppressLint("ParcelCreator")
public class RequestResultReceiver extends ResultReceiver {

    private Receiver mReceiver;

    public RequestResultReceiver(Handler handler){
        super(handler);
    }

    public void setReceiver(Receiver receiver){
        mReceiver = receiver;
    }

    public interface Receiver{
        void onReceiveResult(int resultCode, Bundle resultData);
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
        if (mReceiver != null){
            //Log.w("ResultReceiver", "ResultReceiver Bundle: " + resultData.toString());
            mReceiver.onReceiveResult(resultCode, resultData);
        }
    }
}
