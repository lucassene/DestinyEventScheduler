package com.destiny.event.scheduler.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.destiny.event.scheduler.R;
import com.destiny.event.scheduler.activities.DrawerActivity;
import com.destiny.event.scheduler.adapters.GameAdapter;
import com.destiny.event.scheduler.data.GameTable;
import com.destiny.event.scheduler.interfaces.ToActivityListener;
import com.destiny.event.scheduler.interfaces.UserDataListener;
import com.destiny.event.scheduler.models.GameModel;
import com.destiny.event.scheduler.services.ServerService;

import java.io.Serializable;
import java.util.List;

public class MyEventsFragment extends Fragment implements AdapterView.OnItemSelectedListener, UserDataListener {

    public static final String TAG = "MyEventsFragment";

    Spinner filterSpinner;
    ListView listView;
    TextView emptyView;

    private ToActivityListener callback;

    private GameAdapter gameAdapter;

    private List<GameModel> gameList;
    private String statusId;
    private String[] statusList;

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        setHasOptionsMenu(true);
        callback = (ToActivityListener) getActivity();
        callback.setFragmentType(DrawerActivity.FRAGMENT_TYPE_WITHOUT_BACKSTACK);
        callback.registerUserDataListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.w(TAG, "MyEventsFragment destroyed!");
        callback.deleteUserDataListener(this);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.clear();
        callback.setToolbarTitle(getString(R.string.my_events));
        getActivity().getMenuInflater().inflate(R.menu.home_menu, menu);
    }

    @SuppressWarnings("unchecked")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.my_events_layout, container, false);

        filterSpinner = (Spinner) v.findViewById(R.id.my_events_spinner);
        listView = (ListView) v.findViewById(R.id.my_events_list);
        emptyView = (TextView) v.findViewById(R.id.empty_label);
        listView.setEmptyView(emptyView);

        callback = (ToActivityListener) getActivity();
        callback.setFragmentType(DrawerActivity.FRAGMENT_TYPE_WITHOUT_BACKSTACK);

        statusList = getContext().getResources().getStringArray(R.array.game_status_id);

        gameList = callback.getGameList(GameTable.STATUS_JOINED);
        if (savedInstanceState != null && savedInstanceState.containsKey("listView")){
            gameList = (List<GameModel>) savedInstanceState.getSerializable("listView");
            onGamesLoaded(gameList);
            Log.w(TAG, "Game data already available");
        } else if (gameList == null){
            Log.w(TAG, "Getting game data...");
            getGamesData();
        } else {
            onGamesLoaded(gameList);
        }

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                callback.onGameSelected(gameAdapter.getItem(position), TAG);
            }
        });

        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, getContext().getResources().getStringArray(R.array.game_status));
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filterSpinner.setOnItemSelectedListener(this);
        filterSpinner.setAdapter(spinnerAdapter);
        filterSpinner.setSelection(callback.getSpinnerSelection(DrawerActivity.TAG_MY_EVENTS));
    }

    private void getGamesData() {
        Bundle bundle = new Bundle();
        bundle.putInt(ServerService.REQUEST_TAG, ServerService.TYPE_JOINED_GAMES);
        callback.runServerService(bundle);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onPause() {
        super.onPause();
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        callback.setSpinnerSelection(DrawerActivity.TAG_MY_EVENTS, position);
        statusId = statusList[position];
        filterGameList(statusId);
    }

    public void filterGameList(String filter){
        if (filter != null){
            if (filter.isEmpty()) filter = statusList[0];
            if (gameAdapter != null) {
                gameAdapter.getFilter().filter(filter, new Filter.FilterListener() {
                    @Override
                    public void onFilterComplete(int count) {
                        listView.setAdapter(gameAdapter);
                    }
                });
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    @Override
    public void onUserDataLoaded() {

    }

    @Override
    public void onGamesLoaded(List<GameModel> gameList) {
        Log.w(TAG, "SearchFragment onGamesLoaded called!");
        if (gameAdapter == null) {
            Log.w(TAG, "adapter estava null");
            if (gameList != null){
                listView.setAdapter(null);
                this.gameList = gameList;
                filterGameList(statusId);
                gameAdapter = new GameAdapter(getActivity(), gameList);
            } else Log.w(TAG, "listView null ou size 0");
        } else {
            Log.w(TAG, "adapter já existia");
            if (gameList!=null){
                listView.setAdapter(null);
                this.gameList = gameList;
                filterGameList(statusId);
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
