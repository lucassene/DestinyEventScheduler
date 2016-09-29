package com.app.the.bunker.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.app.the.bunker.fragments.NewEventsListFragment;
import com.app.the.bunker.fragments.ScheduledListFragment;
import com.app.the.bunker.fragments.ValidateListFragment;

public class ViewPagerAdapter extends FragmentStatePagerAdapter {

    private CharSequence titles[];
    private int numOfTabs;

    public ViewPagerAdapter(FragmentManager fm, CharSequence titles[], int numOfTabs) {
        super(fm);
        this.titles = titles;
        this.numOfTabs = numOfTabs;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                return new NewEventsListFragment();
            case 1:
                return new ScheduledListFragment();
            case 2:
                return new ValidateListFragment();
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
