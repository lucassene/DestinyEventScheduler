package com.destiny.event.scheduler.interfaces;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.destiny.event.scheduler.models.GameModel;
import com.destiny.event.scheduler.models.MemberModel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public interface ToActivityListener {
    void updateViewPager();
    void loadNewFragment(Fragment fragment, Bundle bundle, String tag);
    void onEventTypeSelected(int id);
    void onEventGameSelected(int id);
    String getBungieId();
    String getUserName();
    int getPlatform();
    String getClanName();
    String getOrderBy();
    void closeFragment();
    void onLoadingData();
    void onDataLoaded();
    void onGameSelected(GameModel game, String tag);
    void setClanOrderBy(String orderBy);
    void registerAlarmTask(Calendar firstNotification, int firstId, Calendar secondNotification, int secondId);
    void registerUserDataListener(Fragment fragment);
    void deleteUserDataListener(Fragment fragment);
    void cancelAlarmTask(int requestId);
    void setSpinnerSelection(String tag, int position);
    int getSpinnerSelection(String tag);
    void onSelectedFragment(int id);
    void setFragmentType(int type);
    int getFmBackStackCount();
    void setToolbarTitle(String title);
    int getSelectedItem();
    boolean runServerService(Bundle bundle);
    List<GameModel> getGameList(int type);
    void getGameEntries(int gameId);
    void getHistoryEntries(int gameId);
    List<GameModel> getHistoryGames();
    void updateGameStatus(GameModel game, int status);
    void updateGameEntries(int status, int gameId, int entries);
    void updateMembers(List<MemberModel> list);
    void registerNewGamesAlarm();
    void deleteNewGamesAlarm();
    MemberModel getMemberProfile();
    void callAndroidIntent(int type, String text);
    void updateClan(ArrayList<String> idList);
}
