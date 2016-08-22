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
import com.destiny.event.scheduler.models.MemberModel;
import com.destiny.event.scheduler.services.ServerService;

import java.io.Serializable;
import java.util.List;

public class HistoryListFragment extends Fragment implements AdapterView.OnItemSelectedListener, UserDataListener{

    public static final String TAG = "HistoryListFragment";

    Spinner filterSpinner;
    ListView listView;
    TextView emptyView;
    TextView sectionTitle;

    private ToActivityListener callback;

    GameAdapter gameAdapter;
    private List<GameModel> gameList;
    private String[] typeList;
    private String eventType;

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
        setHasOptionsMenu(true);
    }

    @Override
    public void onDestroy() {
        callback.deleteUserDataListener(this);
        super.onDestroy();
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.clear();
        callback.setToolbarTitle(getString(R.string.history));
        getActivity().getMenuInflater().inflate(R.menu.home_menu, menu);
    }

    @SuppressWarnings("unchecked")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.search_event_layout, container, false);

        filterSpinner = (Spinner) v.findViewById(R.id.search_spinner);
        listView = (ListView) v.findViewById(R.id.search_list);
        emptyView = (TextView) v.findViewById(R.id.empty_label);
        sectionTitle = (TextView) v.findViewById(R.id.section_title);

        sectionTitle.setText(R.string.matches_played);
        listView.setEmptyView(emptyView);
        emptyView.setText(R.string.no_event_all);

        typeList = getContext().getResources().getStringArray(R.array.type_ids);

        callback = (ToActivityListener) getActivity();
        callback.setFragmentType(DrawerActivity.FRAGMENT_TYPE_WITHOUT_BACKSTACK);

        gameList = callback.getGameList(GameTable.STATUS_VALIDATED);
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

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, getContext().getResources().getStringArray(R.array.spinner_types));
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filterSpinner.setOnItemSelectedListener(this);
        filterSpinner.setAdapter(spinnerAdapter);
    }

    private void getGamesData() {
        Bundle bundle = new Bundle();
        bundle.putInt(ServerService.REQUEST_TAG, ServerService.TYPE_HISTORY_GAMES);
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
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        callback.setSpinnerSelection(DrawerActivity.TAG_SEARCH_EVENTS, position);
        eventType = typeList[position];
        filterGameList(eventType);
    }

    public void filterGameList(String filter){
        if (filter != null){
            if (filter.isEmpty()) filter = typeList[0];
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
        Log.w(TAG, "HistoryFragment onGamesLoaded called!");
        if (gameAdapter == null) {
            Log.w(TAG, "adapter estava null");
            if (gameList != null){
                Log.w(TAG, "gameList size: " + gameList.size());
                listView.setAdapter(null);
                this.gameList = gameList;
                gameAdapter = new GameAdapter(getActivity(), gameList);
                filterGameList(eventType);
            } else Log.w(TAG, "listView null ou size 0");
        } else {
            Log.w(TAG, "adapter já existia");
            if (gameList!=null){
                listView.setAdapter(null);
                this.gameList = gameList;
                filterGameList(eventType);
                gameAdapter.setGameList(gameList);
                gameAdapter.notifyDataSetChanged();
            } else Log.w(TAG, "listView null");
        }
    }

    @Override
    public void onEntriesLoaded(List<MemberModel> entryList, boolean isUpdateNeeded) {

    }

    @Override
    public void onMemberLoaded(MemberModel member, boolean isUpdateNeeded) {

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("listView", (Serializable) gameList);
    }
}