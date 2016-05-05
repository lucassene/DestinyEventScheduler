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
import com.destiny.event.scheduler.data.EntryTable;
import com.destiny.event.scheduler.data.EventTable;
import com.destiny.event.scheduler.data.EventTypeTable;
import com.destiny.event.scheduler.data.GameTable;
import com.destiny.event.scheduler.data.MemberTable;
import com.destiny.event.scheduler.interfaces.ToActivityListener;
import com.destiny.event.scheduler.provider.DataProvider;

import java.util.ArrayList;

public class MyEventsFragment extends Fragment implements AdapterView.OnItemSelectedListener, LoaderManager.LoaderCallbacks<Cursor> {

    public static final String TAG = "MyEventsFragment";

    Spinner filterSpinner;
    ListView gameList;
    TextView emptyView;

    CustomCursorAdapter adapter;

    private static final int LOADER_ENTRY = 70;

    private static final int[] to = {R.id.primary_text, R.id.game_image, R.id.secondary_text, R.id.game_date, R.id.game_time, R.id.game_actual, R.id.game_max, R.id.type_text};

    private ToActivityListener callback;

    private String[] projection;
    private String[] from;

    private String[] gameStatusList;
    private String gameStatus;
    private ArrayList<String> gameCreatorList;

    private String where;

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(R.string.my_events);
        View v = inflater.inflate(R.layout.my_events_layout, container, false);

        filterSpinner = (Spinner) v.findViewById(R.id.my_events_spinner);
        gameList = (ListView) v.findViewById(R.id.my_events_list);
        emptyView = (TextView) v.findViewById(R.id.empty_label);

        gameCreatorList = new ArrayList<>();

        gameStatusList = new String[] {GameTable.STATUS_NEW, GameTable.STATUS_WAITING, GameTable.STATUS_VALIDATED};
        gameStatus = GameTable.STATUS_NEW;

        if (savedInstanceState != null) gameCreatorList = savedInstanceState.getStringArrayList("gameCreatorList");

        callback = (ToActivityListener) getActivity();

        gameList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                callback.onGameSelected(String.valueOf(id), TAG, gameCreatorList.get(position), gameStatus);
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
        filterSpinner.setSelection(0);

        getGamesData();

    }

    private void getGamesData() {

        prepareStrings();

        getLoaderManager().initLoader(LOADER_ENTRY, null, this);

        adapter = new CustomCursorAdapter(getContext(), R.layout.game_list_item_layout, null, from, to, 0, LOADER_ENTRY);
        gameList.setAdapter(adapter);

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

        String c16 = EventTable.COLUMN_ICON;
        String c17 = EventTable.COLUMN_NAME;
        String c18 = EventTable.COLUMN_GUARDIANS;
        String c19 = EventTable.COLUMN_TYPE;

        projection = new String[] {c1, c2, c3, c4, c5, c6, c7, c8, c9, c10, c12, c14, c16, c17, c18, c19};

        from = new String[] {c17, c16, c5, c6, c6, c8, c18, c14};

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

        switch (position){
            case 0:
                where = EntryTable.COLUMN_MEMBERSHIP + "=" + callback.getBungieId();
                gameStatus = GameTable.STATUS_NEW;
                break;
            case 1:
                where = GameTable.COLUMN_STATUS + "=" + GameTable.STATUS_WAITING;
                gameStatus = GameTable.STATUS_WAITING;
                break;
            case 2:
                gameStatus = GameTable.STATUS_VALIDATED;
                where = GameTable.COLUMN_STATUS + "=" + GameTable.STATUS_VALIDATED;
                break;
            case 3:
                break;
        }

        filterSpinner.setSelection(position);
        getLoaderManager().restartLoader(LOADER_ENTRY, null, this);

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

       callback.onLoadingData();

        switch (id){
            case LOADER_ENTRY:
                return new CursorLoader(
                        getContext(),
                        DataProvider.ENTRY_URI,
                        projection,
                        where,
                        null,
                        "datetime(" + GameTable.getQualifiedColumn(GameTable.COLUMN_TIME) + ") ASC"
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
                case LOADER_ENTRY:
                    adapter.swapCursor(data);
                    for (int i=0; i < data.getCount(); i++){
                        gameCreatorList.add(i, data.getString(data.getColumnIndex(GameTable.getQualifiedColumn(GameTable.COLUMN_CREATOR))));
                        data.moveToNext();
                        Log.w(TAG, "Creator position " + i + " = " + gameCreatorList.get(i) + " userID: " + callback.getBungieId());
                    }
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

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putStringArrayList("gameCreatorList", gameCreatorList);
        super.onSaveInstanceState(outState);
    }
}
