package com.destiny.event.scheduler.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.destiny.event.scheduler.R;
import com.destiny.event.scheduler.adapters.GameAdapter;
import com.destiny.event.scheduler.interfaces.ToActivityListener;
import com.destiny.event.scheduler.interfaces.UserDataListener;
import com.destiny.event.scheduler.models.GameModel;
import com.destiny.event.scheduler.models.MemberModel;
import com.destiny.event.scheduler.services.ServerService;
import com.destiny.event.scheduler.views.CustomSwipeLayout;

import java.util.List;

public class NewEventsListFragment extends ListFragment implements UserDataListener {

    public static final String TAG = "NewEventsListFragment";

    GameAdapter gameAdapter;
    private ToActivityListener callback;
    private List<GameModel> gameList;
    TextView sectionTitle;
    CustomSwipeLayout swipeLayout;
    private boolean showTitle = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        callback.deleteUserDataListener(this);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        callback = (ToActivityListener) getActivity();
        callback.registerUserDataListener(this);
        Log.w(TAG, "NewEventsListFragment attached!");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.new_list_layout, container, false);
        sectionTitle = (TextView) v.findViewById(R.id.section_title);
        swipeLayout = (CustomSwipeLayout) v.findViewById(R.id.swipe_layout);
        return v;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        callback.onSelectedFragment(0);
        gameList = callback.getGameList(GameModel.STATUS_NEW);
        if (gameList != null){
            onGamesLoaded(gameList);
        }
        if (showTitle){
            sectionTitle.setVisibility(View.VISIBLE);
        } else sectionTitle.setVisibility(View.GONE);
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshList();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        setListAdapter(null);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
            callback.onGameSelected(gameList.get(position), TAG);
    }

    @Override
    public void onUserDataLoaded() {}

    @Override
    public void onGamesLoaded(List<GameModel> gameList) {
        Log.w(TAG, "onGamesLoaded called!");
        if (gameAdapter == null) {
            Log.w(TAG, "adapter estava null");
            if (gameList != null){
                this.gameList = gameList;
                gameAdapter = new GameAdapter(getActivity(), gameList);
                if (gameAdapter.getCount() == 0){
                    showTitle = false;
                    if (sectionTitle != null) sectionTitle.setVisibility(View.GONE);
                } else {
                    showTitle = true;
                    if (sectionTitle != null) sectionTitle.setVisibility(View.VISIBLE);
                }
                setListAdapter(gameAdapter);
            } else Log.w(TAG, "listView null ou size 0");
        } else {
            Log.w(TAG, "adapter já existia");
            if (gameList!=null){
                this.gameList = gameList;
                setListAdapter(gameAdapter);
                gameAdapter.setGameList(gameList);
                gameAdapter.notifyDataSetChanged();
                if (gameAdapter.getCount() == 0){
                    showTitle = false;
                    if (sectionTitle != null) sectionTitle.setVisibility(View.GONE);
                } else {
                    showTitle = true;
                    if (sectionTitle != null) sectionTitle.setVisibility(View.VISIBLE);
                }
            } else Log.w(TAG, "listView null");
        }
        if (swipeLayout != null) swipeLayout.setRefreshing(false);
    }

    @Override
    public void onEntriesLoaded(List<MemberModel> entryList, boolean isUpdateNeeded, int gameId) {}

    @Override
    public void onMemberLoaded(MemberModel member, boolean isUpdateNeeded) {}

    @Override
    public void onMembersUpdated() {}

    public void refreshList() {
        Log.w(TAG, "Refreshing...");
        Bundle bundle = new Bundle();
        bundle.putInt(ServerService.REQUEST_TAG, ServerService.TYPE_ALL_GAMES);
        callback.runServerService(bundle);
        swipeLayout.setRefreshing(true);
    }

}
