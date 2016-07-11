package com.destiny.event.scheduler.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.destiny.event.scheduler.R;
import com.destiny.event.scheduler.activities.DrawerActivity;
import com.destiny.event.scheduler.data.LoggedUserTable;
import com.destiny.event.scheduler.data.MemberTable;
import com.destiny.event.scheduler.provider.DataProvider;
import com.destiny.event.scheduler.utils.StringUtils;


public class LevelCheckService extends IntentService {

    private static final String TAG = "LevelCheckService";

    public LevelCheckService() {
        super(LevelCheckService.class.getName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        Log.w(TAG, "LevelCheckService started!");

        Bundle bundle = intent.getExtras();
        final int actualLevel = bundle.getInt("level");
        final String actualTitle = bundle.getString("title");
        String userMembership = "";
        int newLevel;

        Cursor loggedCursor = getContentResolver().query(DataProvider.LOGGED_USER_URI, new String[] {LoggedUserTable.COLUMN_MEMBERSHIP}, null, null, null);
        if (loggedCursor != null && loggedCursor.moveToFirst()){
            userMembership = loggedCursor.getString(loggedCursor.getColumnIndexOrThrow(LoggedUserTable.COLUMN_MEMBERSHIP));
            loggedCursor.close();
        }

        if (!userMembership.equals("")){
            Cursor mCursor = getContentResolver().query(DataProvider.MEMBER_URI, new String[] {MemberTable.COLUMN_EXP, MemberTable.COLUMN_TITLE}, MemberTable.COLUMN_MEMBERSHIP + "=" + userMembership, null, null);
            if (mCursor != null && mCursor.moveToFirst()){
                int xp = mCursor.getInt(mCursor.getColumnIndexOrThrow(MemberTable.COLUMN_EXP));
                final String newTitle = mCursor.getString(mCursor.getColumnIndexOrThrow(MemberTable.COLUMN_TITLE));
                newLevel = MemberTable.getMemberLevel(xp);
                mCursor.close();

                if (newLevel > actualLevel && getSharedPreferences(DrawerActivity.SHARED_PREFS, Context.MODE_PRIVATE).getBoolean(DrawerActivity.FOREGROUND_PREF, false)){

                    Handler handler = new Handler(Looper.getMainLooper());
                    final String levelString = StringUtils.parseString(newLevel);

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            //Log.w(TAG, "actualTitle: " + actualTitle + " / newTitle: " + newTitle);
                            Toast.makeText(LevelCheckService.this, R.string.level_up, Toast.LENGTH_SHORT).show();
                            Toast.makeText(LevelCheckService.this, getString(R.string.level_msg_1) + levelString + "!", Toast.LENGTH_SHORT).show();
                            if (actualTitle != null && !actualTitle.equals(newTitle)){
                                Toast.makeText(LevelCheckService.this, getString(R.string.title_msg) + newTitle + "!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        }

    }
}
