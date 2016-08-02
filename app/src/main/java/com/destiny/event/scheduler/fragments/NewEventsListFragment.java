package com.destiny.event.scheduler.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.destiny.event.scheduler.R;
import com.destiny.event.scheduler.adapters.GameAdapter;
import com.destiny.event.scheduler.interfaces.RefreshDataListener;
import com.destiny.event.scheduler.interfaces.ToActivityListener;
import com.destiny.event.scheduler.interfaces.UserDataListener;
import com.destiny.event.scheduler.models.GameModel;
import com.destiny.event.scheduler.services.ServerService;

import java.io.Serializable;
import java.util.List;

public class NewEventsListFragment extends ListFragment implements RefreshDataListener, UserDataListener {

    public static final String TAG = "NewEventsListFragment";

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
        callback.deleteRefreshListener(this);
        callback.deleteUserDataListener(this);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        callback = (ToActivityListener) getActivity();
        callback.registerRefreshListener(this);
        callback.registerUserDataListener(this);
        //Log.w(TAG, "NewEventsListFragment attached!");
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.new_list_layout, container, false);

        headerView = inflater.inflate(R.layout.list_section_layout, null);

        sectionTitle = (TextView) headerView.findViewById(R.id.section_title);

        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        callback.onSelectedFragment(0);

        sectionTitle.setText(R.string.games_available);

        if (savedInstanceState != null && savedInstanceState.containsKey("gameList")){
            onGamesLoaded((List<GameModel>) savedInstanceState.getSerializable("gameList"));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        setListAdapter(null);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        if (position > 0){
            int newPos = position - 1;
            callback.onGameSelected(gameList.get(newPos), TAG);
        }
    }

    @Override
    public void onRefreshData() {
        //initGameLoader();
        Bundle bundle = new Bundle();
        bundle.putInt(ServerService.RESQUEST_TAG,ServerService.TYPE_ALL_GAMES);
        callback.runServerService(bundle);
        Log.w(TAG, "Refreshing New Events data!");
    }

    @Override
    public Fragment getFragment() {
        return this;
    }

    @Override
    public void onUserDataLoaded() {
    }

    @Override
    public void onGamesLoaded(List<GameModel> gameList) {
        Log.w(TAG, "onGamesLoaded called!");
        if (gameAdapter == null) {
            Log.w(TAG, "adapter estava null");
            if (gameList != null){
                this.gameList = gameList;
                gameAdapter = new GameAdapter(getActivity(), gameList);
                setListAdapter(gameAdapter);
                if (headerView != null){
                    this.getListView().addHeaderView(headerView);
                }
            } else Log.w(TAG, "gameList null ou size 0");
        } else {
            Log.w(TAG, "adapter já existia");
            if (gameList!=null){
                gameAdapter.setGameList(gameList);
                gameAdapter.notifyDataSetChanged();
            } else Log.w(TAG, "gameList null");
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("gameList", (Serializable) gameList);
    }
}
