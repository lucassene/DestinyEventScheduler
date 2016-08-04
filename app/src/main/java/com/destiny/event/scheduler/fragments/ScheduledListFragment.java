package com.destiny.event.scheduler.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
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

import java.io.Serializable;
import java.util.List;

public class ScheduledListFragment extends ListFragment implements UserDataListener{

    public static final String TAG = "ScheduledListFragment";

   GameAdapter gameAdapter;

    View headerView;

    TextView sectionTitle;

    private ToActivityListener callback;

    private List<GameModel> gameList;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.scheduled_list_layout, container, false);

        headerView = inflater.inflate(R.layout.list_section_layout, null);

        sectionTitle = (TextView) headerView.findViewById(R.id.section_title);

        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        callback.onSelectedFragment(1);

        sectionTitle.setText(R.string.scheduled_games);

        if (savedInstanceState != null && savedInstanceState.containsKey("listView")){
            onGamesLoaded((List<GameModel>) savedInstanceState.getSerializable("listView"));
        }

        if (headerView != null){
            this.getListView().addHeaderView(headerView);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        setListAdapter(null);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        //Toast.makeText(getContext(), "GameID Selected: " + gameIdList.get(position-1), Toast.LENGTH_SHORT).show();
        if (position > 0){
            int newPos = position - 1;
            callback.onGameSelected(gameList.get(newPos), TAG);
        }
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        callback = (ToActivityListener) getActivity();
        callback.registerUserDataListener(this);
        Log.w(TAG, "ScheduledListFragment attached!");
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
                setListAdapter(gameAdapter);
            } else Log.w(TAG, "listView null ou size 0");
        } else {
            Log.w(TAG, "adapter já existia");
            if (gameList!=null){
                this.gameList = gameList;
                gameAdapter.setGameList(gameList);
                gameAdapter.notifyDataSetChanged();
            } else Log.w(TAG, "listView null");
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("listView", (Serializable) gameList);
    }

}
