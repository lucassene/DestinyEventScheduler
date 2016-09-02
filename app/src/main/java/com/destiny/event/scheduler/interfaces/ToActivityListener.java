package com.destiny.event.scheduler.interfaces;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.destiny.event.scheduler.models.GameModel;
import com.destiny.event.scheduler.models.MemberModel;

import java.util.Calendar;
import java.util.List;

public interface ToActivityListener {
    public void updateViewPager();
    public void loadNewFragment(Fragment fragment, Bundle bundle, String tag);
    public void onEventTypeSelected(String id);
    public void onEventGameSelected(String id);
    public String getBungieId();
    public String getUserName();
    public int getPlatform();
    public String getClanName();
    public String getOrderBy();
    public void closeFragment();
    public void onLoadingData();
    public void onDataLoaded();
    public void onGameSelected(GameModel game, String tag);
    public void onScheduledGames(boolean status);
    public void onValidateGames(boolean status);
    public void setClanOrderBy(String orderBy);
    public void registerAlarmTask(Calendar firstNotification, int firstId, Calendar secondNotification, int secondId);
    public void registerUserDataListener(Fragment fragment);
    public void deleteUserDataListener(Fragment fragment);
    public void cancelAlarmTask(int requestId);
    public void setSpinnerSelection(String tag, int position);
    public int getSpinnerSelection(String tag);
    public void onSelectedFragment(int id);
    public void setFragmentType(int type);
    public int getFmBackStackCount();
    public void setToolbarTitle(String title);
    public int getSelectedItem();
    public boolean runServerService(Bundle bundle);
    public List<GameModel> getGameList(int type);
    public void getGameEntries(int gameId);
    public void getHistoryEntries(int gameId);
    public List<GameModel> getHistoryGames();
    public void updateGameStatus(GameModel game, int status);
    public void updateGameEntries(int status, int gameId, int entries);
    public void updateMembers(List<MemberModel> list);
    public void registerNewGamesAlarm();
    public void deleteNewGamesAlarm();
    public MemberModel getMemberProfile();
}
