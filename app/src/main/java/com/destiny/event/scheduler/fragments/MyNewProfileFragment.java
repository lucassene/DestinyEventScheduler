package com.destiny.event.scheduler.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;

import com.destiny.event.scheduler.R;
import com.destiny.event.scheduler.activities.DrawerActivity;
import com.destiny.event.scheduler.adapters.ProfileViewPagerAdapter;
import com.destiny.event.scheduler.interfaces.ToActivityListener;
import com.destiny.event.scheduler.views.SlidingTabLayout;

public class MyNewProfileFragment extends Fragment {

    public static final int TYPE_MENU = 1;
    public static final int TYPE_DETAIL = 2;

    private ViewPager viewPager;
    private ProfileViewPagerAdapter viewPagerAdapter;
    private SlidingTabLayout tabLayout;

    private ToActivityListener callback;

    private int type;
    private String memberId;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.my_new_profile_layout, container, false);

        viewPager = (ViewPager) v.findViewById(R.id.profile_view);

        String titles[] = getResources().getStringArray(R.array.profile_tab_titles);
        int numOfTabs = titles.length;
        viewPagerAdapter = new ProfileViewPagerAdapter(getChildFragmentManager(), titles, numOfTabs, getArguments());

        viewPager.setAdapter(viewPagerAdapter);

        tabLayout = (SlidingTabLayout) v.findViewById(R.id.profile_tab);
        tabLayout.setDistributeEvenly(true);
        tabLayout.setViewPager(viewPager);
        tabLayout.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return getResources().getColor(R.color.tabIndicatorColor);
            }
        });

        if (getArguments()!=null){
            memberId = getArguments().getString("bungieId");
            type = getArguments().getInt("type");
        }

        callback = (ToActivityListener) getActivity();
        if (type == TYPE_MENU) {
            callback.setFragmentType(DrawerActivity.FRAGMENT_TYPE_WITHOUT_BACKSTACK);
        } else callback.setFragmentType(DrawerActivity.FRAGMENT_TYPE_WITH_BACKSTACK);
        setHasOptionsMenu(true);

        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        setHasOptionsMenu(true);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.clear();
        callback.setToolbarTitle(getString(R.string.my_profile));
        getActivity().getMenuInflater().inflate(R.menu.empty_menu, menu);
    }
}
