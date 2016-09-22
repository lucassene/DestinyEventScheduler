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

import com.destiny.event.scheduler.R;
import com.destiny.event.scheduler.adapters.DoneGamesAdapter;
import com.destiny.event.scheduler.interfaces.ToActivityListener;
import com.destiny.event.scheduler.interfaces.UserDataListener;
import com.destiny.event.scheduler.models.GameModel;
import com.destiny.event.scheduler.models.MemberModel;
import com.destiny.event.scheduler.services.ServerService;
import com.destiny.event.scheduler.views.CustomSwipeLayout;

import java.util.List;

public class ValidateListFragment extends ListFragment implements UserDataListener{

    public static final String TAG = "ValidateListFragment";

    private ToActivityListener callback;
    DoneGamesAdapter gameAdapter;
    private List<GameModel> gameList;
    CustomSwipeLayout swipeLayout;

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
        //Log.w(TAG, "ValidateListFragment attached!");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.validate_list_layout, container, false);
        swipeLayout = (CustomSwipeLayout) v.findViewById(R.id.swipe_layout);
        return v;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        callback.onSelectedFragment(2);
        gameList = callback.getGameList(GameModel.STATUS_DONE);
        if (gameList != null){
            onGamesLoaded(gameList);
        }
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshList();
            }
        });
    }

    private void refreshList() {
        Log.w(TAG, "Refreshing...");
        Bundle bundle = new Bundle();
        bundle.putInt(ServerService.REQUEST_TAG, ServerService.TYPE_ALL_GAMES);
        callback.runServerService(bundle);
        swipeLayout.setRefreshing(true);
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
    public void onUserDataLoaded() {
    }

    @Override
    public void onGamesLoaded(List<GameModel> gameList) {
        Log.w(TAG, "onGamesLoaded called!");
        if (gameAdapter == null) {
            Log.w(TAG, "adapter estava null");
            if (gameList != null) {
                this.gameList = gameList;
                gameAdapter = new DoneGamesAdapter(getActivity(), gameList, getStartPos(gameList, GameModel.STATUS_WAITING), getStartPos(gameList, GameModel.STATUS_VALIDATED));
                setListAdapter(gameAdapter);
                gameAdapter.notifyDataSetChanged();
            } else Log.w(TAG, "listView null ou size 0");
        } else {
            Log.w(TAG, "adapter já existia");
            if (gameList != null) {
                this.gameList = gameList;
                gameAdapter.setGameList(gameList);
                gameAdapter.setStartPositions(getStartPos(gameList, GameModel.STATUS_WAITING), getStartPos(gameList, GameModel.STATUS_VALIDATED));
                setListAdapter(gameAdapter);
                gameAdapter.notifyDataSetChanged();
            } else Log.w(TAG, "listView null");
        }
        swipeLayout.setRefreshing(false);
    }

    private int getStartPos(List<GameModel> gameList, int gameStatus) {
        int pos = -1;
        for (int i=0;i<gameList.size();i++){
            if (gameList.get(i).getStatus() == gameStatus){
                return i;
            }
        }
        return pos;
    }

    @Override
    public void onEntriesLoaded(List<MemberModel> entryList, boolean isUpdateNeeded, int gameId) {

    }

    @Override
    public void onMemberLoaded(MemberModel member, boolean isUpdateNeeded) {

    }

    @Override
    public void onMembersUpdated() {

    }

}