package com.app.the.bunker.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.app.the.bunker.BunkerAuthenticator;

public class AuthenticatorService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        BunkerAuthenticator authenticator = new BunkerAuthenticator(this);
        return authenticator.getIBinder();
    }
}
