package com.destiny.event.scheduler.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.destiny.event.scheduler.R;
import com.destiny.event.scheduler.activities.DrawerActivity;
import com.destiny.event.scheduler.adapters.ProfileViewPagerAdapter;
import com.destiny.event.scheduler.interfaces.ToActivityListener;
import com.destiny.event.scheduler.interfaces.UserDataListener;
import com.destiny.event.scheduler.models.GameModel;
import com.destiny.event.scheduler.models.MemberModel;
import com.destiny.event.scheduler.services.ServerService;
import com.destiny.event.scheduler.views.SlidingTabLayout;

import java.util.List;

public class MyNewProfileFragment extends Fragment implements UserDataListener {

    private static final String TAG = "MyNewProfileFragment";

    public static final int TYPE_MENU = 1;
    public static final int TYPE_DETAIL = 2;

    private ToActivityListener callback;
    ViewPager viewPager;
    SlidingTabLayout tabLayout;
    TextView emptyText;

    private int type;
    String memberId;
    MemberModel member;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.my_new_profile_layout, container, false);

        viewPager = (ViewPager) v.findViewById(R.id.profile_view);
        tabLayout = (SlidingTabLayout) v.findViewById(R.id.profile_tab);
        emptyText = (TextView) v.findViewById(R.id.text_empty);

        if (getArguments()!=null){
            memberId = getArguments().getString("bungieId");
            type = getArguments().getInt("type");
        }

        callback = (ToActivityListener) getActivity();
        if (type == TYPE_MENU) {
            callback.setFragmentType(DrawerActivity.FRAGMENT_TYPE_WITHOUT_BACKSTACK);
        } else callback.setFragmentType(DrawerActivity.FRAGMENT_TYPE_WITH_BACKSTACK);
        setHasOptionsMenu(true);

        MemberModel member = callback.getMemberProfile();
        if (member == null){
            Bundle bundle = new Bundle();
            bundle.putInt(ServerService.REQUEST_TAG, ServerService.TYPE_PROFILE);
            bundle.putString(ServerService.PROFILE_TAG, memberId);
            boolean success = callback.runServerService(bundle);
            if (!success){
                emptyText.setVisibility(View.VISIBLE);
            } else emptyText.setVisibility(View.GONE);
        } else onMemberLoaded(member, false);

        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    private void setAdapter(MemberModel member){
        ProfileViewPagerAdapter viewPagerAdapter;
        String titles[] = getResources().getStringArray(R.array.profile_tab_titles);
        int numOfTabs = titles.length;
        viewPagerAdapter = new ProfileViewPagerAdapter(getChildFragmentManager(), titles, numOfTabs, member);

        viewPager.setAdapter(viewPagerAdapter);

        tabLayout.setDistributeEvenly(true);
        tabLayout.setViewPager(viewPager);
        tabLayout.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @SuppressWarnings("deprecation")
            @Override
            public int getIndicatorColor(int position) {
                return getResources().getColor(R.color.tabIndicatorColor);
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        setHasOptionsMenu(true);
        callback = (ToActivityListener) getActivity();
        //callback.registerUserDataListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //callback.deleteUserDataListener(this);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.clear();
        callback.setToolbarTitle(getString(R.string.my_profile));
        callback.deleteUserDataListener(this);
        getActivity().getMenuInflater().inflate(R.menu.home_menu, menu);
    }

    @Override
    public void onUserDataLoaded() {

    }

    @Override
    public void onGamesLoaded(List<GameModel> gameList) {

    }

    @Override
    public void onEntriesLoaded(List<MemberModel> entryList, boolean isUpdateNeeded) {

    }

    @Override
    public void onMemberLoaded(MemberModel member, boolean isUpdateNeeded) {
        Log.w(TAG, "onMemberLoaded called");
        if (member != null){
            emptyText.setVisibility(View.GONE);
            setAdapter(member);
        } else emptyText.setVisibility(View.VISIBLE);
    }

    @Override
    public void onMembersUpdated() {

    }

    public MemberModel getMember(){
        return member;
    }
}
