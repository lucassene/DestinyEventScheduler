package com.destiny.event.scheduler.interfaces;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import java.util.Calendar;

public interface ToActivityListener {
    public void updateViewPager();
    public void loadNewFragment(Fragment fragment, Bundle bundle, String tag);
    public void loadWithoutBackStack(Fragment fragment, Bundle bundle, String tag);
    public void onEventTypeSelected(String id);
    public void onEventGameSelected(String id);
    public String getBungieId();
    public String getUserName();
    public String getClanName();
    public String getOrderBy();
    public void closeFragment();
    public void onLoadingData();
    public void onDataLoaded();
    public void onGameSelected(String id, String tag, String creator, String status);
    public void onNoScheduledGames();
    public void setClanOrderBy(String orderBy);
    public void registerRefreshListener(Fragment fragment);
    public void registerAlarmTask(Calendar time, String title, int iconId);
    public void registerUserDataListener(Fragment fragment);
}
