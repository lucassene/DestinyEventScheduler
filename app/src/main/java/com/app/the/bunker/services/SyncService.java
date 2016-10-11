package com.app.the.bunker.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.app.the.bunker.adapters.SyncAdapter;

public class SyncService extends Service {

    private static final Object sSyncAdapterLock = new Object();
    private SyncAdapter syncAdapter = null;

    @Override
    public void onCreate() {
        super.onCreate();
        synchronized (sSyncAdapterLock){
            if (syncAdapter == null){
                syncAdapter = new SyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return syncAdapter.getSyncAdapterBinder();
    }
}
