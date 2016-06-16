package com.destiny.event.scheduler.services;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;

import com.destiny.event.scheduler.R;
import com.destiny.event.scheduler.data.EntryTable;
import com.destiny.event.scheduler.data.EventTable;
import com.destiny.event.scheduler.data.EventTypeTable;
import com.destiny.event.scheduler.data.MemberTable;
import com.destiny.event.scheduler.data.TitleTable;
import com.destiny.event.scheduler.provider.DataProvider;

import java.util.ArrayList;
import java.util.Locale;

public class TitleService extends IntentService {

    private static final String TAG = "TitleService";

    public TitleService() {
        super(TitleService.class.getName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        Bundle bundle = intent.getExtras();
        int xp;
        int lvl = 0;
        int eventId = 0;

        Log.w(TAG, "Title service started!");

        ArrayList<String> membershipList = bundle.getStringArrayList("membershipList");

        if (membershipList!=null){
            //Log.w(TAG, "membershipList is not null");
            for (int i=0;i<membershipList.size();i++){

                String memberName = "";

                Cursor xpCursor = getContentResolver().query(DataProvider.MEMBER_URI, MemberTable.ALL_COLUMNS, MemberTable.COLUMN_MEMBERSHIP + "=" + membershipList.get(i), null, null);
                if (xpCursor != null && xpCursor.moveToFirst()){
                    xp = xpCursor.getInt(xpCursor.getColumnIndexOrThrow(MemberTable.COLUMN_EXP));
                    lvl = MemberTable.getMemberLevel(xp);
                    memberName = xpCursor.getString(xpCursor.getColumnIndexOrThrow(MemberTable.COLUMN_NAME));
                    xpCursor.close();
                }

                Cursor favCursor = getContentResolver().query(DataProvider.ENTRY_FAVORITE_URI, getFavoriteProjection(), EntryTable.COLUMN_MEMBERSHIP + "=" + membershipList.get(i), null, "total DESC");
                if (favCursor != null && favCursor.moveToFirst()){
                    if (favCursor.getCount() == 0){
                        eventId = 999;
                    } else eventId = favCursor.getInt(favCursor.getColumnIndexOrThrow(EventTable.getQualifiedColumn(EventTable.COLUMN_ID)));
                    favCursor.close();
                } else eventId = 999;

                Log.w(TAG, memberName + " fav event: " + eventId);

                String newTitle = getTitle(lvl, eventId);
                Log.w(TAG, memberName + " new Title: " + newTitle);
                updateMemberTitle(membershipList.get(i),newTitle);

            }
        } else Log.w(TAG, "membershipList not found");

        this.stopSelf();

    }

    private void updateMemberTitle(String membershipId, String newTitle) {

        ContentValues values = new ContentValues();
        values.put(MemberTable.COLUMN_TITLE,newTitle);

        int confirm = getContentResolver().update(DataProvider.MEMBER_URI, values, MemberTable.COLUMN_MEMBERSHIP + "=" + membershipId,null);

        if (confirm == 0) Log.w(TAG, "Update failed!");

        values.clear();

    }

    private String getTitle(int lvl, int eventId) {

        String[] levelTitles = getResources().getStringArray(R.array.level_title);
        String[] eventTitles = getResources().getStringArray(R.array.event_title);

        int type = 0;
        int titleIndex = 0;

        String newTitle = "";

        if (eventId != 999){
            Cursor titleCursor = getContentResolver().query(DataProvider.TITLE_URI, TitleTable.ALL_COLUMNS, TitleTable.COLUMN_EVENT + "=" + eventId, null, null);
            if (titleCursor != null && titleCursor.moveToFirst()){
                type = titleCursor.getInt(titleCursor.getColumnIndexOrThrow(TitleTable.COLUMN_ORDER));
                titleIndex = titleCursor.getInt(titleCursor.getColumnIndexOrThrow(TitleTable.COLUMN_TITLE));
                titleCursor.close();
            }

            if (lvl<=25){
                newTitle = levelTitles[0];
            } else if (lvl<=50){
                newTitle = levelTitles[1];
            } else if (lvl<=75){
                newTitle = levelTitles[2];
            } else newTitle = levelTitles[3];
        }

        if (eventId == 999){
            newTitle = getString(R.string.new_guardian);
        } else {
            String eventTitle = eventTitles[titleIndex];

            Locale current = getResources().getConfiguration().locale;

            if (current.getLanguage().equals("pt")){
                newTitle = newTitle + " " + eventTitle;
            } else {
                if (type == 0){
                    newTitle = eventTitle + " " + newTitle;
                } else newTitle = newTitle + " " + eventTitle;
            }
        }

        //Log.w(TAG, "newTitle: " + newTitle);
        return newTitle;
    }

    private String[] getFavoriteProjection() {

        String c1 = "COUNT(*) AS total";
        String c2 = EventTable.COLUMN_NAME;
        String c3 = EventTypeTable.COLUMN_NAME;
        String c4 = EventTable.COLUMN_ICON;
        String c5 = EventTable.getQualifiedColumn(EventTable.COLUMN_ID);

        return new String[] {c1, c2, c3, c4, c5};

    }
}
