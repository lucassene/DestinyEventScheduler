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
import com.destiny.event.scheduler.activities.DrawerActivity;
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

    private static final int LOADER_GAME = 60;
    private static final int LOADER_ENTRY = 70;

    private static final int[] to = {R.id.primary_text, R.id.game_image, R.id.secondary_text, R.id.game_date, R.id.game_time, R.id.game_actual, R.id.game_max, R.id.type_text};

    private ToActivityListener callback;

    private String[] projection;
    private String[] from;

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
        filterSpinner.setSelection(callback.getSpinnerSelection(DrawerActivity.TAG_MY_EVENTS));

        getGamesData();

    }

    private void getGamesData() {

        prepareStrings();

        /*switch (filterSpinner.getSelectedItemPosition()){
            case 0:
                where = EntryTable.COLUMN_MEMBERSHIP + "=" + callback.getBungieId() + " AND " + GameTable.COLUMN_STATUS + "=" + GameTable.STATUS_SCHEDULED;
                getLoaderManager().initLoader(LOADER_ENTRY, null, this);
                break;
            case 1:
                where = EntryTable.COLUMN_MEMBERSHIP + "=" + callback.getBungieId() + " AND " + GameTable.COLUMN_STATUS + "=" + GameTable.STATUS_WAITING;
                getLoaderManager().initLoader(LOADER_ENTRY, null, this);
                break;
            case 2:
                where = GameTable.COLUMN_STATUS + "=" + GameTable.STATUS_VALIDATED;
                prepareGameStrings();
                getLoaderManager().initLoader(LOADER_GAME, null, this);
                break;
            case 3:
                where = GameTable.COLUMN_STATUS + "=" + GameTable.STATUS_EVALUATED;
                prepareGameStrings();
                getLoaderManager().initLoader(LOADER_GAME, null, this);
                break;
        }*/

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

    private void prepareGameStrings(){

        String c1 = GameTable.getQualifiedColumn(GameTable.COLUMN_ID);
        String c2 = GameTable.COLUMN_EVENT_ID;
        String c3 = GameTable.COLUMN_CREATOR;
        String c4 = GameTable.COLUMN_TIME;
        String c5 = GameTable.COLUMN_LIGHT;
        String c6 = GameTable.COLUMN_INSCRIPTIONS;
        String c7 = GameTable.COLUMN_CREATOR_NAME;
        String c8 = GameTable.COLUMN_STATUS;

        String c9 = EventTypeTable.COLUMN_NAME;

        String c10 = EventTable.COLUMN_ICON;
        String c11 = EventTable.COLUMN_NAME;
        String c12 = EventTable.COLUMN_GUARDIANS;
        String c13 = EventTable.COLUMN_TYPE;

        projection = new String[] {c1, c2, c3, c4, c5, c6, c7, c8, c9, c10, c11, c12, c13};

        from = new String[] {c3, c4, c4, c6, c9, c10, c11, c12};

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
                where = EntryTable.COLUMN_MEMBERSHIP + "=" + callback.getBungieId() + " AND " + GameTable.COLUMN_STATUS + "=" + GameTable.STATUS_SCHEDULED;
                prepareStrings();
                getLoaderManager().destroyLoader(LOADER_GAME);
                getLoaderManager().restartLoader(LOADER_ENTRY, null, this);
                break;
            case 1:
                where = EntryTable.COLUMN_MEMBERSHIP + "=" + callback.getBungieId() + " AND " + GameTable.COLUMN_STATUS + "=" + GameTable.STATUS_WAITING;
                prepareStrings();
                getLoaderManager().destroyLoader(LOADER_GAME);
                getLoaderManager().restartLoader(LOADER_ENTRY, null, this);
                break;
            case 2:
                where = GameTable.COLUMN_STATUS + "=" + GameTable.STATUS_VALIDATED;
                prepareGameStrings();
                getLoaderManager().destroyLoader(LOADER_ENTRY);
                getLoaderManager().restartLoader(LOADER_GAME, null, this);
                break;
            case 3:
                where = GameTable.COLUMN_STATUS + "=" + GameTable.STATUS_EVALUATED;
                prepareGameStrings();
                getLoaderManager().destroyLoader(LOADER_ENTRY);
                getLoaderManager().restartLoader(LOADER_GAME, null, this);
                break;
        }

        callback.setSpinnerSelection(DrawerActivity.TAG_MY_EVENTS, position);

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

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

        Log.w(TAG, DatabaseUtils.dumpCursorToString(data));

        if (data != null && data.moveToFirst()){

            switch (loader.getId()){
                case LOADER_GAME:
                    adapter.swapCursor(data);
                    gameStatus = data.getString(data.getColumnIndexOrThrow(GameTable.COLUMN_STATUS));
                case LOADER_ENTRY:
                    adapter.swapCursor(data);
                    gameStatus = data.getString(data.getColumnIndexOrThrow(GameTable.COLUMN_STATUS));
            }

            for (int i=0; i < data.getCount(); i++){
                gameCreatorList.add(i, data.getString(data.getColumnIndex(GameTable.COLUMN_CREATOR)));
                data.moveToNext();
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
        outState.putString("status",gameStatus);
        super.onSaveInstanceState(outState);
    }
}
