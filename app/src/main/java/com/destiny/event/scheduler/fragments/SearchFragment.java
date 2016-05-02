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

        String c1 = GameTable.getQualifiedColumn(GameTable.COLUMN_ID); // game._ID;
        String c2 = GameTable.getQualifiedColumn(GameTable.COLUMN_EVENT_ID); // game.event_id;
        String c3 = EventTable.getAliasExpression(EventTable.COLUMN_ID); // event._ID AS event__ID;
        String c4 = EventTable.getQualifiedColumn(EventTable.COLUMN_ICON); // event.icon;
        String c5 = EventTable.getAliasExpression(EventTable.COLUMN_NAME); // event.name AS event_name;
        String c6 = GameTable.getQualifiedColumn(GameTable.COLUMN_CREATOR); // game.creator;
        String c7 = MemberTable.getQualifiedColumn(MemberTable.COLUMN_MEMBERSHIP); // member.membership;
        String c8 = MemberTable.getAliasExpression(MemberTable.COLUMN_NAME); // member.name AS member_name;
        String c9 = GameTable.getQualifiedColumn(GameTable.COLUMN_TIME); // game.time;
        String c10 = GameTable.getQualifiedColumn(GameTable.COLUMN_LIGHT); // game.light;
        String c11 = EventTable.getQualifiedColumn(EventTable.COLUMN_GUARDIANS); // game.guardians;
        String c12 = GameTable.getQualifiedColumn(GameTable.COLUMN_INSCRIPTIONS); // game.inscriptions;
        String c13 = MemberTable.getAliasExpression(MemberTable.COLUMN_ID); // member._ID AS member__ID;
        String c14 = GameTable.getAliasExpression(GameTable.COLUMN_CREATOR_NAME); // game.creator AS game_creator;
        String c15 = EventTable.getQualifiedColumn(EventTable.COLUMN_TYPE); // event.type_of_event;
        String c16 = EventTypeTable.getAliasExpression(EventTypeTable.COLUMN_ID); // event_type._ID AS event_type__ID;
        String c17 = EventTypeTable.getQualifiedColumn(EventTypeTable.COLUMN_NAME); // event_type.type_name;


        projection = new String[] {c1, c2, c3, c4, c5, c6, c7, c8, c9, c10, c11, c12, c13, c14, c15, c16, c17};

        String f1 = EventTable.getAliasColumn(EventTable.COLUMN_NAME);
        String f2 = GameTable.getAliasColumn(GameTable.COLUMN_CREATOR_NAME);

        from = new String[] {f1, c4, f2, c9, c9, c12, c11, c17};

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

        String where = GameTable.getQualifiedColumn(GameTable.COLUMN_STATUS) + "=" + GameTable.GAME_NEW + " AND " + EventTypeTable.getAliasColumn(EventTypeTable.COLUMN_ID) + "=" + eventId;

        callback.onLoadingData();

        switch (id){
            case LOADER_GAME:
                return new CursorLoader(
                        getContext(),
                        DataProvider.GAME_URI,
                        projection,
                        where,
                        null,
                        "datetime(" + GameTable.getQualifiedColumn(GameTable.COLUMN_TIME) + ") DESC"
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
