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
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.destiny.event.scheduler.R;
import com.destiny.event.scheduler.activities.DrawerActivity;
import com.destiny.event.scheduler.adapters.CustomCursorAdapter;
import com.destiny.event.scheduler.adapters.GameAdapter;
import com.destiny.event.scheduler.data.GameTable;
import com.destiny.event.scheduler.interfaces.RefreshDataListener;
import com.destiny.event.scheduler.interfaces.ToActivityListener;
import com.destiny.event.scheduler.interfaces.UserDataListener;
import com.destiny.event.scheduler.models.GameModel;
import com.destiny.event.scheduler.services.ServerService;

import java.io.Serializable;
import java.util.List;

public class SearchFragment extends Fragment implements AdapterView.OnItemSelectedListener, RefreshDataListener, UserDataListener {

    public static final String TAG = "SearchFragment";

    Spinner filterSpinner;
    ListView listView;
    TextView emptyView;

    CustomCursorAdapter adapter;
    GameAdapter gameAdapter;

    private ToActivityListener callback;

    private String[] typeList;
    private String eventType;

    private List<GameModel> gameList;

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
        callback.registerRefreshListener(this);
        callback.registerUserDataListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.w(TAG, "SearchFragment destroyed!");
        callback.deleteRefreshListener(this);
        callback.deleteUserDataListener(this);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.clear();
        callback.setToolbarTitle(getString(R.string.search_title));
        getActivity().getMenuInflater().inflate(R.menu.home_menu, menu);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.search_event_layout, container, false);

        filterSpinner = (Spinner) v.findViewById(R.id.search_spinner);
        listView = (ListView) v.findViewById(R.id.search_list);
        emptyView = (TextView) v.findViewById(R.id.empty_label);

        typeList = getContext().getResources().getStringArray(R.array.type_ids);

        listView.setFooterDividersEnabled(false);
        listView.setEmptyView(emptyView);

        callback = (ToActivityListener) getActivity();
        callback.setFragmentType(DrawerActivity.FRAGMENT_TYPE_WITHOUT_BACKSTACK);

        if (savedInstanceState != null && savedInstanceState.containsKey("gameList")){
            gameList = (List<GameModel>) savedInstanceState.getSerializable("gameList");
            onGamesLoaded(gameList);
            Log.w(TAG, "Game data already available");
        } else if (callback.getGameList(GameTable.STATUS_AVAILABLE) == null){
            Log.w(TAG, "Getting game data...");
            getGamesData();
        } else {
            gameList = callback.getGameList(GameTable.STATUS_AVAILABLE);
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

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, getContext().getResources().getStringArray(R.array.spinner_types));
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filterSpinner.setOnItemSelectedListener(this);
        filterSpinner.setAdapter(spinnerAdapter);

    }

    private void getGamesData() {
        Bundle bundle = new Bundle();
        bundle.putInt(ServerService.REQUEST_TAG, ServerService.TYPE_NEW_GAMES);
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
        eventType = typeList[position];
        filterGameList(eventType);
    }

    public void filterGameList(String filter){
        if (filter.isEmpty()) filter = typeList[0];
        if (gameAdapter != null) {
            gameAdapter.getFilter().filter(filter);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onRefreshData() {
        Bundle bundle = new Bundle();
        bundle.putInt(ServerService.REQUEST_TAG,ServerService.TYPE_NEW_GAMES);
        callback.runServerService(bundle);
        Log.w(TAG, "Refreshing Search New Events data!");
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
        Log.w(TAG, "SearchFragment onGamesLoaded called!");
        if (gameAdapter == null) {
            Log.w(TAG, "adapter estava null");
            if (gameList != null){
                this.gameList = gameList;
                gameAdapter = new GameAdapter(getActivity(), gameList);
                listView.setAdapter(gameAdapter);
                filterGameList(eventType);
            } else Log.w(TAG, "gameList null ou size 0");
        } else {
            Log.w(TAG, "adapter já existia");
            if (gameList!=null){
                this.gameList = gameList;
                listView.setAdapter(gameAdapter);
                filterGameList(eventType);
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
