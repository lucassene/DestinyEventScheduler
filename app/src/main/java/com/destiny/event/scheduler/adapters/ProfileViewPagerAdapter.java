package com.destiny.event.scheduler.adapters;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.destiny.event.scheduler.fragments.MyMedalsFragment;
import com.destiny.event.scheduler.fragments.MyStatsFragment;
import com.destiny.event.scheduler.models.MemberModel;

public class ProfileViewPagerAdapter extends FragmentStatePagerAdapter {

    CharSequence titles[];
    int numOfTabs;
    Bundle bundle = new Bundle();

    public ProfileViewPagerAdapter(FragmentManager fm, CharSequence titles[], int numOfTabs, MemberModel member) {
        super(fm);
        this.titles = titles;
        this.numOfTabs = numOfTabs;
        this.bundle.putSerializable("member", member);
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
