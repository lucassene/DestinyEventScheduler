package com.destiny.event.scheduler.fragments;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.destiny.event.scheduler.R;
import com.destiny.event.scheduler.adapters.CustomCursorAdapter;
import com.destiny.event.scheduler.data.EntryTable;
import com.destiny.event.scheduler.data.EventTable;
import com.destiny.event.scheduler.data.EventTypeTable;
import com.destiny.event.scheduler.data.GameTable;
import com.destiny.event.scheduler.data.MemberTable;
import com.destiny.event.scheduler.interfaces.RefreshDataListener;
import com.destiny.event.scheduler.interfaces.ToActivityListener;
import com.destiny.event.scheduler.interfaces.UserDataListener;
import com.destiny.event.scheduler.provider.DataProvider;

import java.util.ArrayList;

public class ScheduledListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor>, RefreshDataListener, UserDataListener{

    public static final String TAG = "ScheduledListFragment";

    private static final int LOADER_ENTRY = 70;

    private static final int[] to = {R.id.primary_text, R.id.game_image, R.id.secondary_text, R.id.game_date, R.id.game_time, R.id.game_actual, R.id.game_max, R.id.type_text};

    CustomCursorAdapter adapter;

    View headerView;

    TextView sectionTitle;

    private ToActivityListener callback;

    private String[] projection;
    private String[] from;

    private ArrayList<String> gameIdList;
    private ArrayList<String> gameCreatorList;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.scheduled_list_layout, container, false);

        headerView = inflater.inflate(R.layout.list_section_layout, null);

        sectionTitle = (TextView) headerView.findViewById(R.id.section_title);

        gameIdList = new ArrayList<>();
        gameCreatorList = new ArrayList<>();

        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        callback.onSelectedFragment(1);

        sectionTitle.setText(R.string.scheduled_games);

        String bungieId = callback.getBungieId();

        if (bungieId != null){
            getScheduledEvents();
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
        if (position > 0 ){
            callback.onGameSelected(gameIdList.get(position - 1), TAG, gameCreatorList.get(position - 1), GameTable.STATUS_SCHEDULED);
        }
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        callback = (ToActivityListener) getActivity();
        callback.registerRefreshListener(this);
        callback.registerUserDataListener(this);
        Log.w(TAG, "ScheduledListFragment attached!");
    }

    private void getScheduledEvents() {
        prepareStrings();
        getLoaderManager().initLoader(LOADER_ENTRY, null, this);
        adapter = new CustomCursorAdapter(getContext(), R.layout.game_list_item_layout, null, from, to, 0, LOADER_ENTRY);

        if (headerView != null){
            this.getListView().addHeaderView(headerView);
        }

        setListAdapter(adapter);
    }

    private void prepareStrings() {

        String c1 = EntryTable.getQualifiedColumn(EntryTable.COLUMN_ID);
        String c2 = EntryTable.COLUMN_MEMBERSHIP;

        String c3 = GameTable.getQualifiedColumn(GameTable.COLUMN_ID);
        String c4 = GameTable.COLUMN_EVENT_ID;
        String c5 = GameTable.COLUMN_CREATOR;
        String c6 = GameTable.COLUMN_TIME;
        String c7 = GameTable.COLUMN_LIGHT;
        String c8 = GameTable.COLUMN_INSCRIPTIONS;
        String c9 = GameTable.COLUMN_CREATOR_NAME;
        String c10 = GameTable.COLUMN_STATUS;

        String c12 = MemberTable.COLUMN_NAME;

        String c14 = EventTypeTable.COLUMN_NAME;
        String c15 = EventTypeTable.COLUMN_ICON;

        String c16 = EventTable.COLUMN_ICON;
        String c17 = EventTable.COLUMN_NAME;
        String c18 = EventTable.COLUMN_GUARDIANS;
        String c19 = EventTable.COLUMN_TYPE;

        projection = new String[] {c1, c2, c3, c4, c5, c6, c7, c8, c9, c10, c12, c14, c15, c16, c17, c18, c19};

        from = new String[] {c17, c16, c5, c6, c6, c8, c18, c14};

    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        callback.onLoadingData();

        String where = EntryTable.COLUMN_MEMBERSHIP + "=" + callback.getBungieId() + " AND " + GameTable.COLUMN_STATUS + "=" + GameTable.STATUS_SCHEDULED;

        switch (id){
            case LOADER_ENTRY:
                return new CursorLoader(
                        getContext(),
                        DataProvider.ENTRY_URI,
                        projection,
                        where,
                        null,
                        "datetime(" + GameTable.COLUMN_TIME + ") ASC"
                );
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        //Log.w(TAG, DatabaseUtils.dumpCursorToString(data));

        if (data !=null && data.moveToFirst()){

            switch (loader.getId()){
                case LOADER_ENTRY:

                    adapter.swapCursor(data);
                    data.moveToFirst();

                    for (int i=0; i < data.getCount();i++){
                        gameIdList.add(i, data.getString(data.getColumnIndexOrThrow(GameTable.getQualifiedColumn(GameTable.COLUMN_ID))));
                        gameCreatorList.add(i, data.getString(data.getColumnIndex(GameTable.COLUMN_CREATOR)));
                        data.moveToNext();
                    }

                    callback.onScheduledGames(true);

                    break;
            }

        } else {
            callback.onScheduledGames(false);
            callback.onSelectedFragment(0);
        }

        callback.onDataLoaded();

    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }

    @Override
    public void onRefreshData() {
        callback.onLoadingData();
        getLoaderManager().restartLoader(LOADER_ENTRY, null, this);
    }

    @Override
    public Fragment getFragment() {
        return this;
    }

    @Override
    public void onUserDataLoaded() {
        getScheduledEvents();
    }
}
