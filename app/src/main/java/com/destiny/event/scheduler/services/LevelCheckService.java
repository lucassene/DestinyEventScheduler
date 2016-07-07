package com.destiny.event.scheduler.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Toast;

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

        Bundle bundle = intent.getExtras();
        int actualLevel = bundle.getInt("level");
        String userMembership = "";
        int newLevel;

        Cursor loggedCursor = getContentResolver().query(DataProvider.LOGGED_USER_URI, new String[] {LoggedUserTable.COLUMN_MEMBERSHIP}, null, null, null);
        if (loggedCursor != null && loggedCursor.moveToFirst()){
            userMembership = loggedCursor.getString(loggedCursor.getColumnIndexOrThrow(LoggedUserTable.COLUMN_MEMBERSHIP));
            loggedCursor.close();
        }

        if (!userMembership.equals("")){
            Cursor xpCursor = getContentResolver().query(DataProvider.MEMBER_URI, new String[] {MemberTable.COLUMN_EXP}, MemberTable.COLUMN_MEMBERSHIP + "=" + userMembership, null, null);
            if (xpCursor != null && xpCursor.moveToFirst()){
                int xp = xpCursor.getInt(xpCursor.getColumnIndexOrThrow(MemberTable.COLUMN_EXP));
                newLevel = MemberTable.getMemberLevel(xp);
                xpCursor.close();

                if (newLevel > actualLevel && getSharedPreferences(DrawerActivity.SHARED_PREFS, Context.MODE_PRIVATE).getBoolean(DrawerActivity.FOREGROUND_PREF, false)){
                    Toast.makeText(getApplicationContext(), "Level Up!", Toast.LENGTH_SHORT).show();
                    Toast.makeText(getApplicationContext(), "You achieved level " + StringUtils.parseString(newLevel) + "!", Toast.LENGTH_SHORT).show();
                }
            }
        }

    }
}
