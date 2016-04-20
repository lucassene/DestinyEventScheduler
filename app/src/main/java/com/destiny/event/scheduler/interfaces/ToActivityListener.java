package com.destiny.event.scheduler.interfaces;

import android.os.Bundle;
import android.support.v4.app.Fragment;

public interface ToActivityListener {
    public void updateViewPager();
    public void loadNewFragment(Fragment fragment, Bundle bundle, String tag);
    public void loadWithoutBackStack(Fragment fragment, Bundle bundle, String tag);
    public void onEventTypeSelected(String id);
    public void onEventGameSelected(String id);
    public String getBungieId();
    public String getUserName();
    public void closeFragment();
}
