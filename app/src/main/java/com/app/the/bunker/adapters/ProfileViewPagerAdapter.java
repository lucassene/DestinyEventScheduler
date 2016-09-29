package com.app.the.bunker.adapters;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.app.the.bunker.fragments.MyMedalsFragment;
import com.app.the.bunker.fragments.MyStatsFragment;
import com.app.the.bunker.models.MemberModel;

public class ProfileViewPagerAdapter extends FragmentStatePagerAdapter {

    private CharSequence titles[];
    private int numOfTabs;
    private Bundle bundle = new Bundle();

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
