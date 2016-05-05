package com.destiny.event.scheduler.fragments;

import android.database.Cursor;
import android.database.DatabaseUtils;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.destiny.event.scheduler.R;
import com.destiny.event.scheduler.adapters.CustomCursorAdapter;
import com.destiny.event.scheduler.data.EventTable;
import com.destiny.event.scheduler.data.EventTypeTable;
import com.destiny.event.scheduler.data.GameTable;
import com.destiny.event.scheduler.data.MemberTable;
import com.destiny.event.scheduler.interfaces.ToActivityListener;
import com.destiny.event.scheduler.provider.DataProvider;

public class SearchFragment extends Fragment implements AdapterView.OnItemSelectedListener, LoaderManager.LoaderCallbacks<Cursor> {

    public static final String TAG = "SearchFragment";

    Spinner filterSpinner;
    ListView gamesList;
    TextView emptyView;

    CustomCursorAdapter adapter;

    private static final int LOADER_GAME = 60;

    private static final int[] to = {R.id.primary_text, R.id.game_image, R.id.secondary_text, R.id.game_date, R.id.game_time, R.id.game_actual, R.id.game_max, R.id.type_text};

    private ToActivityListener callback;

    private String[] projection;
    private String[] from;

    private int[] eventIdList;
    private int eventId;

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(R.string.search_title);
        View v = inflater.inflate(R.layout.search_event_layout, container, false);

        filterSpinner = (Spinner) v.findViewById(R.id.search_spinner);
        gamesList = (ListView) v.findViewById(R.id.search_list);
        emptyView = (TextView) v.findViewById(R.id.empty_label);

        eventIdList = getContext().getResources().getIntArray(R.array.event_type_ids);

        callback = (ToActivityListener) getActivity();

        gamesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                callback.onGameSelected(String.valueOf(id), TAG, null, null);
            }
        });

        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, getContext().getResources().getStringArray(R.array.event_types));
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filterSpinner.setOnItemSelectedListener(this);
        filterSpinner.setAdapter(spinnerAdapter);
        //filterSpinner.setSelection(0);

        eventId = eventIdList[0];
        getGamesData();

    }

    private void getGamesData() {

        prepareStrings();

        getLoaderManager().initLoader(LOADER_GAME, null, this);

        adapter = new CustomCursorAdapter(getContext(), R.layout.game_list_item_layout, null, from, to, 0, LOADER_GAME);

        gamesList.setAdapter(adapter);

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

        eventId = eventIdList[position];
        filterSpinner.setSelection(position);
        getLoaderManager().restartLoader(LOADER_GAME, null, this);

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        //String where = GameTable.getQualifiedColumn(GameTable.COLUMN_STATUS) + "=" + GameTable.STATUS_NEW + " AND " + EventTypeTable.getAliasColumn(EventTypeTable.COLUMN_ID) + "=" + eventId;
        String where = GameTable.getQualifiedColumn(GameTable.COLUMN_ID) + " NOT IN (SELECT game._id FROM game JOIN entry ON game._id = entry.entry_game_id WHERE entry.entry_membership = " + callback.getBungieId() + ") AND " + EventTypeTable.getQualifiedColumn(EventTypeTable.COLUMN_ID) + "=" + eventId;;

        callback.onLoadingData();

        switch (id){
            case LOADER_GAME:
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
                case LOADER_GAME:
                    adapter.swapCursor(data);
            }
            callback.onDataLoaded();
            emptyView.setVisibility(View.GONE);
        } else {
            callback.onDataLoaded();
            adapter.swapCursor(null);
            emptyView.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }
}
