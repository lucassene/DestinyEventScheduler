package com.app.the.bunker.services;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.app.the.bunker.activities.DrawerActivity;
import com.app.the.bunker.data.SavedImagesTable;
import com.app.the.bunker.provider.DataProvider;
import com.app.the.bunker.utils.ImageUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;


public class DownloadService extends IntentService {

    private static final String TAG = "DownloadService";

    public DownloadService() {
        super(DownloadService.class.getName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        SharedPreferences prefs = getSharedPreferences(DrawerActivity.SHARED_PREFS, Context.MODE_PRIVATE);
        Set<String> set = prefs.getStringSet(DrawerActivity.DOWNLOAD_PREF, null);
        String icon;
        if (set != null){
            ArrayList<String> downloadList = new ArrayList<>(set);
            ContentValues values = new ContentValues();
            for (int i=0;i<downloadList.size();i++){
                int error = ImageUtils.downloadImage(getApplicationContext(),downloadList.get(i));
                if (error == ImageUtils.NO_ERROR){
                    //Log.w(TAG, "Image " + downloadList.get(i) + " downloaded successfully");
                    icon = downloadList.get(i).substring(downloadList.get(i).lastIndexOf("/")+1, downloadList.get(i).length());
                    values.put(SavedImagesTable.COLUMN_PATH, icon);
                    getContentResolver().insert(DataProvider.SAVED_IMAGES_URI, values);
                    downloadList.remove(i);
                    i--;
                } else {
                    Log.w(TAG, "Error (" + error + ") when trying to download " + downloadList.get(i));
                }
                values.clear();
            }
            SharedPreferences.Editor editor = prefs.edit();
            if (downloadList.size()==0){
                editor.remove(DrawerActivity.DOWNLOAD_PREF);
                editor.apply();
            } else {
                Set<String> newSet = new HashSet<>();
                newSet.addAll(downloadList);
                editor.putStringSet(DrawerActivity.DOWNLOAD_PREF,newSet);
                editor.apply();
            }
        }

        this.stopSelf();

    }
}
