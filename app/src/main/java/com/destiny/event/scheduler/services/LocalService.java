package com.destiny.event.scheduler.services;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import com.destiny.event.scheduler.activities.DrawerActivity;
import com.destiny.event.scheduler.data.MemberTable;
import com.destiny.event.scheduler.models.MemberModel;
import com.destiny.event.scheduler.provider.DataProvider;
import com.destiny.event.scheduler.utils.ImageUtils;

import java.util.ArrayList;
import java.util.List;

public class LocalService extends IntentService {

    private static final String TAG = "LocalService";

    public static final int TYPE_UPDATE_MEMBERS = 1;
    public static final int STATUS_FINISHED = 310;

    public static final String REQUEST_HEADER = "request";
    public static final String MEMBERS_HEADER = "memberList";
    public static final String CLAN_HEADER = "clanId";

    private static final String BASE_IMAGE_URL = "http://www.bungie.net";
    public static final String RUNNING_SERVICE = "localRunning";

    private ResultReceiver receiver;

    public LocalService() {
        super(LocalService.class.getName());
    }

    @Override
    public void onCreate() {
        super.onCreate();
        SharedPreferences sharedPrefs = getApplicationContext().getSharedPreferences(DrawerActivity.SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putBoolean(RUNNING_SERVICE, true);
        editor.apply();
        Log.w(TAG, "Service running!");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        SharedPreferences sharedPrefs = getApplicationContext().getSharedPreferences(DrawerActivity.SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putBoolean(RUNNING_SERVICE, false);
        editor.apply();
        Log.w(TAG, "Service destroyed!");
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void onHandleIntent(Intent intent) {

        if (intent.hasExtra(ServerService.RECEIVER_TAG)){
            receiver = intent.getParcelableExtra(ServerService.RECEIVER_TAG);
        } else receiver = null;

        int request = intent.getIntExtra(REQUEST_HEADER, 0);

        switch (request){
            case TYPE_UPDATE_MEMBERS:
                List<MemberModel> memberList = (List<MemberModel>) intent.getSerializableExtra(MEMBERS_HEADER);
                updateMembers(memberList, intent.getStringExtra(CLAN_HEADER));
                break;
        }

        this.stopSelf();

    }

    private void updateMembers(List<MemberModel> memberList, String clanId) {

        ArrayList<String> iconList = new ArrayList<>();
        ArrayList<String> localIdList = new ArrayList<>();
        Cursor memberCursor = null;
        try{
            memberCursor = getContentResolver().query(DataProvider.MEMBER_URI, MemberTable.ALL_COLUMNS,null,null,null);
            if (memberCursor != null && memberCursor.moveToFirst()){
                for (int i=0;i<memberCursor.getCount();i++){
                    localIdList.add(memberCursor.getString(memberCursor.getColumnIndexOrThrow(MemberTable.COLUMN_MEMBERSHIP)));
                    iconList.add(memberCursor.getString(memberCursor.getColumnIndexOrThrow(MemberTable.COLUMN_ICON)));
                    memberCursor.moveToNext();
                }
            }
            downloadIcons(memberList, iconList);
            updateOrInsert(memberList, localIdList, clanId);
            receiver.send(STATUS_FINISHED, Bundle.EMPTY);
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            if (memberCursor != null) memberCursor.close();
        }

    }

    private void updateOrInsert(List<MemberModel> memberList, ArrayList<String> localIdList, String clanId) {
        boolean isInsert = false;
        for (int i=0;i<memberList.size();i++){
            for (int x=0;x<localIdList.size();x++){
                if (memberList.get(i).getMembershipId().equals(localIdList.get(x))){
                    Log.w(TAG, "Updating member " + memberList.get(i).getMembershipId());
                    updateMember(memberList.get(i));
                    isInsert = false;
                    Log.w(TAG, memberList.get(i).getMembershipId() + " is equal to " + localIdList.get(x));
                    break;
                } else {
                    Log.w(TAG, memberList.get(i).getMembershipId() + " is different than " + localIdList.get(x));
                    isInsert = true;
                }
            }
            if (isInsert){
                Log.w(TAG, "Inserting " + memberList.get(i).getMembershipId());
                insertMember(memberList.get(i), clanId);
            }
        }
    }

    private void insertMember(MemberModel memberModel, String clanId) {
        ContentValues memberValues = new ContentValues();
        memberValues.put(MemberTable.COLUMN_MEMBERSHIP, memberModel.getMembershipId());
        memberValues.put(MemberTable.COLUMN_NAME, memberModel.getName());
        memberValues.put(MemberTable.COLUMN_PLATFORM, memberModel.getPlatformId());
        memberValues.put(MemberTable.COLUMN_CLAN, clanId);
        memberValues.put(MemberTable.COLUMN_LIKES, memberModel.getLikes());
        memberValues.put(MemberTable.COLUMN_DISLIKES, memberModel.getDislikes());
        memberValues.put(MemberTable.COLUMN_CREATED, memberModel.getGamesCreated());
        memberValues.put(MemberTable.COLUMN_PLAYED, memberModel.getGamesPlayed());
        memberValues.put(MemberTable.COLUMN_ICON, getIconName(memberModel.getIconPath()));
        memberValues.put(MemberTable.COLUMN_TITLE, memberModel.getFavoriteEvent().getEventId());
        getContentResolver().insert(DataProvider.MEMBER_URI, memberValues);
        memberValues.clear();
    }

    private void updateMember(MemberModel memberModel) {
        ContentValues memberValues = new ContentValues();
        memberValues.put(MemberTable.COLUMN_LIKES, memberModel.getLikes());
        memberValues.put(MemberTable.COLUMN_DISLIKES, memberModel.getDislikes());
        memberValues.put(MemberTable.COLUMN_CREATED, memberModel.getGamesCreated());
        memberValues.put(MemberTable.COLUMN_PLAYED, memberModel.getGamesPlayed());
        memberValues.put(MemberTable.COLUMN_ICON, getIconName(memberModel.getIconPath()));
        memberValues.put(MemberTable.COLUMN_TITLE, memberModel.getFavoriteEvent().getEventId());
        getContentResolver().update(DataProvider.MEMBER_URI, memberValues, MemberTable.COLUMN_MEMBERSHIP + "=" + memberModel.getMembershipId(), null);
        memberValues.clear();
    }

    private void downloadIcons(List<MemberModel> memberList, ArrayList<String> iconList) {
        boolean download = false;
        for (int i=0;i<memberList.size();i++){
            for(int x=0;x<iconList.size();x++){
                if (getIconName(memberList.get(i).getIconPath()).equals(iconList.get(x))){
                    download = false;
                    break;
                } else download = true;
            }
            if (download){
                ImageUtils.downloadImage(getApplicationContext(),BASE_IMAGE_URL + memberList.get(i).getIconPath());
            }
        }
    }

    private String getIconName(String s) {
        return s.substring(s.lastIndexOf("/")+1,s.length());
    }

}
