package com.destiny.event.scheduler.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.destiny.event.scheduler.fragments.NewEventsListFragment;
import com.destiny.event.scheduler.fragments.ScheduledListFragment;
import com.destiny.event.scheduler.fragments.ValidateListFragment;

public class ViewPagerAdapter extends FragmentStatePagerAdapter {

    CharSequence titles[];
    int numOfTabs;

    public ViewPagerAdapter(FragmentManager fm, CharSequence titles[], int numOfTabs) {
        super(fm);
        this.titles = titles;
        this.numOfTabs = numOfTabs;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                NewEventsListFragment newEventsListFragment = new NewEventsListFragment();
                return newEventsListFragment;
            case 1:
                ScheduledListFragment scheduledListFragment = new ScheduledListFragment();
                return scheduledListFragment;
            case 2:
                ValidateListFragment validateListFragment = new ValidateListFragment();
                return validateListFragment;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return numOfTabs;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return titles[position];
    }
}
