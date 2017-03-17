package com.app.the.bunker.fragments;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
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
import android.widget.Toast;

import com.app.the.bunker.R;
import com.app.the.bunker.activities.DrawerActivity;
import com.app.the.bunker.adapters.GameAdapter;
import com.app.the.bunker.data.EventTypeTable;
import com.app.the.bunker.interfaces.SwipeListener;
import com.app.the.bunker.interfaces.ToActivityListener;
import com.app.the.bunker.interfaces.UserDataListener;
import com.app.the.bunker.models.GameModel;
import com.app.the.bunker.models.MemberModel;
import com.app.the.bunker.provider.DataProvider;
import com.app.the.bunker.services.ServerService;
import com.app.the.bunker.utils.NetworkUtils;
import com.app.the.bunker.utils.StringUtils;
import com.app.the.bunker.views.CustomSwipeLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SearchFragment extends Fragment implements AdapterView.OnItemSelectedListener, UserDataListener, LoaderManager.LoaderCallbacks<Cursor>, SwipeListener {

    public static final String TAG = "SearchFragment";

    private static final int LOADER_TYPE = 10;

    Spinner filterSpinner;
    ListView listView;
    TextView emptyView;
    CustomSwipeLayout swipeLayout;
    GameAdapter gameAdapter;
    private ToActivityListener callback;
    private List<GameModel> gameList;
    private HashMap<String,String> typeList;
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
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.w(TAG, "SearchFragment destroyed!");
        callback.deleteUserDataListener(this);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.clear();
        callback.setToolbarTitle(getString(R.string.search_title));
        getActivity().getMenuInflater().inflate(R.menu.home_menu, menu);
    }


    @SuppressWarnings("unchecked")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.search_event_layout, container, false);

        filterSpinner = (Spinner) v.findViewById(R.id.search_spinner);
        listView = (ListView) v.findViewById(R.id.search_list);
        emptyView = (TextView) v.findViewById(R.id.empty_label);

        listView.setFooterDividersEnabled(false);
        listView.setEmptyView(emptyView);

        callback = (ToActivityListener) getActivity();
        callback.setFragmentType(DrawerActivity.FRAGMENT_TYPE_WITHOUT_BACKSTACK);

        swipeLayout = (CustomSwipeLayout) v.findViewById(R.id.swipe_layout);
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshList();
            }
        });

        gameList = callback.getGameList(GameModel.STATUS_NEW);
        if (gameList == null){
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

    private void initLoader(int loaderType){
        callback.onLoadingData();
        if (getLoaderManager().getLoader(loaderType) != null){
            getLoaderManager().destroyLoader(loaderType);
        }
        getLoaderManager().restartLoader(loaderType, null, this);
    }

    private void refreshList() {
        if (NetworkUtils.checkConnection(getContext())){
            getGamesData();
            swipeLayout.setRefreshing(true);
        } else {
            swipeLayout.setRefreshing(false);
            Toast.makeText(getContext(), R.string.check_connection, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        initLoader(LOADER_TYPE);
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
        if (typeList != null){
            eventType = typeList.get(parent.getItemAtPosition(position).toString());
            callback.setSpinnerSelection(DrawerActivity.TAG_SEARCH_EVENTS, position);
        } else {
            int eventId = callback.getSpinnerSelection(DrawerActivity.TAG_SEARCH_EVENTS);
            if (eventId == 0){
                eventType = "type:all";
            } else eventType = "type:" + eventId;
        }
        filterGameList(eventType);
    }

    public void filterGameList(String filter){
        if (filter != null){
            if (filter.isEmpty()) filter = "type:all";
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
    public void onNothingSelected(AdapterView<?> parent) {}

    @Override
    public void onUserDataLoaded() {}

    @Override
    public void onGamesLoaded(List<GameModel> gameList) {
        Log.w(TAG, "SearchFragment onGamesLoaded called!");
        if (gameAdapter == null) {
            Log.w(TAG, "adapter estava null");
            if (gameList != null){
                this.gameList = gameList;
                gameAdapter = new GameAdapter(getActivity(), gameList);
                filterGameList(eventType);
            } else Log.w(TAG, "listView null ou size 0");
        } else {
            Log.w(TAG, "adapter já existia");
            if (gameList != null){
                this.gameList = gameList;
                gameAdapter.setGameList(gameList);
                filterGameList(eventType);
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

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        callback.onLoadingData();
        switch (id){
            case LOADER_TYPE:
                return new CursorLoader(
                        getContext(),
                        DataProvider.EVENT_TYPE_URI,
                        EventTypeTable.ALL_COLUMNS,
                        null,
                        null,
                        getNameColumn() + " ASC"
                );
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        String nameColumn = getNameColumn();
        if (data != null && data.moveToFirst()){
            if (loader.getId() == LOADER_TYPE){
                ArrayList<String> names = new ArrayList<>();
                typeList = new HashMap<>();
                typeList.put(getString(R.string.all),"type:all");
                names.add(getString(R.string.all));
                for (int i=0;i<data.getCount();i++){
                    String name = data.getString(data.getColumnIndexOrThrow(nameColumn));
                    String type = "type:" + data.getInt(data.getColumnIndexOrThrow(EventTypeTable.COLUMN_ID));
                    names.add(name);
                    typeList.put(name, type);
                    data.moveToNext();
                }
                setSpinnerAdapter(names);
            }
        }
        callback.onDataLoaded();
    }

    private void setSpinnerAdapter(ArrayList<String> nameList){
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, nameList);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filterSpinner.setOnItemSelectedListener(this);
        filterSpinner.setAdapter(spinnerAdapter);
        filterSpinner.setSelection(callback.getSpinnerSelection(DrawerActivity.TAG_SEARCH_EVENTS));
    }

    private String getNameColumn() {
        switch (StringUtils.getLanguageString()) {
            case "pt":
                return EventTypeTable.COLUMN_PT;
            case "es":
                return EventTypeTable.COLUMN_ES;
            default:
                return EventTypeTable.COLUMN_EN;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public void toggleSwipeProgress(boolean b) {
        swipeLayout.setRefreshing(b);
    }
}
