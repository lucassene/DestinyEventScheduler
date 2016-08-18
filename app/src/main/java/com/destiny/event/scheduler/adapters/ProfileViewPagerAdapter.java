package com.destiny.event.scheduler.adapters;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.destiny.event.scheduler.fragments.MyMedalsFragment;
import com.destiny.event.scheduler.fragments.MyStatsFragment;

public class ProfileViewPagerAdapter extends FragmentStatePagerAdapter {

    CharSequence titles[];
    int numOfTabs;
    Bundle bundle;

    public ProfileViewPagerAdapter(FragmentManager fm, CharSequence titles[], int numOfTabs, Bundle bundle) {
        super(fm);
        this.titles = titles;
        this.numOfTabs = numOfTabs;
        this.bundle = bundle;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                MyStatsFragment myStatsFragment = new MyStatsFragment();
                myStatsFragment.setArguments(bundle);
                return myStatsFragment;
            case 1:
                MyMedalsFragment myMedalsFragment = new MyMedalsFragment();
                myMedalsFragment.setArguments(bundle);
                return myMedalsFragment;
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
