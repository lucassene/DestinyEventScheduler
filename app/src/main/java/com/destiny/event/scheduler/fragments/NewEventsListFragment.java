package com.destiny.event.scheduler.fragments;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
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
import com.destiny.event.scheduler.data.EventTable;
import com.destiny.event.scheduler.data.EventTypeTable;
import com.destiny.event.scheduler.data.GameTable;
import com.destiny.event.scheduler.data.MemberTable;
import com.destiny.event.scheduler.interfaces.RefreshDataListener;
import com.destiny.event.scheduler.interfaces.ToActivityListener;
import com.destiny.event.scheduler.interfaces.UserDataListener;
import com.destiny.event.scheduler.provider.DataProvider;

import java.util.ArrayList;

public class NewEventsListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor>, RefreshDataListener, UserDataListener {

    public static final String TAG = "NewEventsListFragment";

    private static final int URL_LOADER_GAME = 60;

    private static final int[] to = {R.id.primary_text, R.id.game_image, R.id.secondary_text, R.id.game_date, R.id.game_time, R.id.game_actual, R.id.game_max, R.id.type_text};

    CustomCursorAdapter adapter;

    View headerView;

    TextView sectionTitle;

    private ToActivityListener callback;

    private String[] projection;
    private String[] from;

    private ArrayList<String> creatorList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        Log.w(TAG, "NewEventsListFragment attached!");
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.new_list_layout, container, false);

        headerView = inflater.inflate(R.layout.list_section_layout, null);

        sectionTitle = (TextView) headerView.findViewById(R.id.section_title);

        creatorList = new ArrayList<>();

        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        callback.onSelectedFragment(0);

        sectionTitle.setText(R.string.games_available);

        String bungieId = callback.getBungieId();

        if (bungieId != null){
            getNewEvents();
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {

        if (position > 0){
            int newPos = position - 1;
            //Toast.makeText(getContext(), "GameID Selected: " + id + " | Creator: " + creatorList.get(newPos) + "(" + newPos + "/" + position + ")", Toast.LENGTH_SHORT).show();
            callback.onGameSelected(String.valueOf(id), TAG, creatorList.get(newPos), GameTable.STATUS_NEW);
        }

    }

    private void getNewEvents() {
        prepareStrings();
        getLoaderManager().initLoader(URL_LOADER_GAME, null, this);
        adapter = new CustomCursorAdapter(getContext(), R.layout.game_list_item_layout, null, from, to, 0, URL_LOADER_GAME);

        if (headerView != null){
            this.getListView().addHeaderView(headerView);
        }

        setListAdapter(adapter);

    }

    private void prepareStrings() {

        String c1 = GameTable.getQualifiedColumn(GameTable.COLUMN_ID); ;
        String c2 = GameTable.COLUMN_EVENT_ID;
        String c6 = GameTable.COLUMN_CREATOR;
        String c9 = GameTable.COLUMN_TIME;
        String c10 = GameTable.COLUMN_LIGHT;
        String c12 = GameTable.COLUMN_INSCRIPTIONS;
        String c14 = GameTable.COLUMN_CREATOR_NAME;

        String c4 = EventTable.COLUMN_ICON;
        String c5 = EventTable.COLUMN_NAME;
        String c11 = EventTable.COLUMN_GUARDIANS;

        String c8 = MemberTable.COLUMN_NAME;

        String c17 = EventTypeTable.COLUMN_NAME;

        projection = new String[] {c1, c2, c4, c5, c6, c8, c9, c10, c11, c12, c14, c17};

        from = new String[] {c5, c4, c14, c9, c9, c12, c11, c17};

    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String where = GameTable.getQualifiedColumn(GameTable.COLUMN_ID) + " NOT IN (SELECT game._id FROM game JOIN entry ON game._id = entry.entry_game_id WHERE entry.entry_membership = " + callback.getBungieId() + ")";

        callback.onLoadingData();

        switch (id){
            case URL_LOADER_GAME:
                return new CursorLoader(
                        getContext(),
                        DataProvider.GAME_URI,
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

        Log.w(TAG, DatabaseUtils.dumpCursorToString(data));

        if (data != null && data.moveToFirst()){
            switch (loader.getId()){
                case URL_LOADER_GAME:
                    adapter.swapCursor(data);

                    data.moveToFirst();
                    for (int i=0; i<data.getCount(); i++){
                        creatorList.add(data.getString(data.getColumnIndex(GameTable.COLUMN_CREATOR)));
                        data.moveToNext();
                    }

            }
            callback.onDataLoaded();
        }



    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }

    @Override
    public void onRefreshData() {
        callback.onLoadingData();
        getLoaderManager().restartLoader(URL_LOADER_GAME, null, this);
    }

    @Override
    public Fragment getFragment() {
        return this;
    }

    @Override
    public void onUserDataLoaded() {
        getNewEvents();
    }
}
