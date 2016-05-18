package com.destiny.event.scheduler.fragments;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.destiny.event.scheduler.R;
import com.destiny.event.scheduler.adapters.DBViewerCursorAdapter;
import com.destiny.event.scheduler.data.ClanTable;
import com.destiny.event.scheduler.data.EntryTable;
import com.destiny.event.scheduler.data.EvaluationTable;
import com.destiny.event.scheduler.data.EventTable;
import com.destiny.event.scheduler.data.EventTypeTable;
import com.destiny.event.scheduler.data.GameTable;
import com.destiny.event.scheduler.data.LoggedUserTable;
import com.destiny.event.scheduler.data.MemberTable;
import com.destiny.event.scheduler.data.NotificationTable;
import com.destiny.event.scheduler.provider.DataProvider;

public class DBViewerFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, AdapterView.OnItemSelectedListener {

    private static final String TAG = "DBViewerFragment";

    private static final int EVENT_TYPE = 10;
    private static final int EVENT = 20;
    private static final int LOGGED_USER = 30;
    private static final int CLAN = 40;
    private static final int MEMBER = 50;
    private static final int GAME = 60;
    private static final int ENTRY = 70;
    private static final int NOTIFICATION = 80;
    private static final int EVALUATION = 90;

    private DBViewerCursorAdapter adapter;

    private Spinner spinner;
    private ListView list;

    private int[] to = {R.id.txt1, R.id.txt2, R.id.txt3, R.id.txt4, R.id.txt5, R.id.txt6};

    private String selectedTable;
    private int selectedLoader;
    private Uri selectedUri;
    private String[] projection;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("DB Viewer");
        View v = inflater.inflate(R.layout.db_viewer_layout, container, false);

        spinner = (Spinner) v.findViewById(R.id.search_spinner);
        list = (ListView) v.findViewById(R.id.search_list);

        selectedTable = ClanTable.TABLE_NAME;
        selectedLoader = CLAN;
        selectedUri = DataProvider.CLAN_URI;
        to = new int[] {R.id.txt1, R.id.txt2, R.id.txt3, R.id.txt4, R.id.txt5, R.id.txt6};
        projection = ClanTable.ALL_COLUMNS;

        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, getContext().getResources().getStringArray(R.array.table_names));
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setOnItemSelectedListener(this);
        spinner.setAdapter(spinnerAdapter);


    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(
                getContext(),
                selectedUri,
                projection,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null && data.moveToFirst()){
            adapter.swapCursor(data);
        } else {
            Toast.makeText(getContext(), "Query vazia!", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        switch (position){
            case 0:
                selectedTable = ClanTable.TABLE_NAME;
                selectedLoader = CLAN;
                selectedUri = DataProvider.CLAN_URI;
                to = new int[] {R.id.txt1, R.id.txt2, R.id.txt3, R.id.txt4, R.id.txt5, R.id.txt6};
                projection = ClanTable.ALL_COLUMNS;
                break;
            case 1:
                selectedTable = EntryTable.TABLE_NAME;
                selectedLoader = ENTRY;
                selectedUri = DataProvider.ALL_ENTRIES_URI;
                to = new int[] {R.id.txt1, R.id.txt2, R.id.txt3, R.id.txt4};
                projection = EntryTable.ALL_COLUMNS;
                break;
            case 2:
                selectedTable = EventTable.TABLE_NAME;
                selectedLoader = EVENT;
                selectedUri = DataProvider.EVENT_URI;
                to = new int[] {R.id.txt1, R.id.txt2, R.id.txt3, R.id.txt4, R.id.txt5, R.id.txt6};
                projection = EventTable.ALL_COLUMNS;
                break;
            case 3:
                selectedTable = EventTypeTable.TABLE_NAME;
                selectedLoader = EVENT_TYPE;
                selectedUri = DataProvider.EVENT_TYPE_URI;
                to = new int[] {R.id.txt1, R.id.txt2, R.id.txt3};
                projection = EventTypeTable.ALL_COLUMNS;
                break;
            case 4:
                selectedTable = GameTable.TABLE_NAME;
                selectedLoader = GAME;
                selectedUri = DataProvider.ALL_GAME_URI;
                to = new int[] {R.id.txt1, R.id.txt2, R.id.txt3, R.id.txt4, R.id.txt5, R.id.txt6, R.id.txt7, R.id.txt8};
                projection = GameTable.ALL_COLUMNS;
                break;
            case 5:
                selectedTable = LoggedUserTable.TABLE_NAME;
                selectedLoader = LOGGED_USER;
                selectedUri = DataProvider.LOGGED_USER_URI;
                to = new int[] {R.id.txt1, R.id.txt2, R.id.txt3, R.id.txt4, R.id.txt5};
                projection = LoggedUserTable.ALL_COLUMNS;
                break;
            case 6:
                selectedTable = MemberTable.TABLE_NAME;
                selectedLoader = MEMBER;
                selectedUri = DataProvider.MEMBER_URI;
                to = new int[] {R.id.txt1, R.id.txt2, R.id.txt3, R.id.txt4, R.id.txt5, R.id.txt6, R.id.txt7, R.id.txt8, R.id.txt9, R.id.txt10};
                projection = MemberTable.ALL_COLUMNS;
                break;
            case 7:
                selectedTable = NotificationTable.TABLE_NAME;
                selectedLoader = NOTIFICATION;
                selectedUri = DataProvider.NOTIFICATION_URI;
                to = new int[] {R.id.txt1, R.id.txt2, R.id.txt3, R.id.txt4, R.id.txt5, R.id.txt6};
                projection = NotificationTable.ALL_COLUMNS;
                break;
            case 8:
                selectedTable = EvaluationTable.TABLE_NAME;
                selectedLoader = EVALUATION;
                selectedUri = DataProvider.EVALUATION_URI;
                to = new int[] {R.id.txt1, R.id.txt2, R.id.txt3, R.id.txt4, R.id.txt5};
                projection = EvaluationTable.ALL_COLUMNS;
                break;
        }

        adapter = new DBViewerCursorAdapter(getContext(), R.layout.db_item_layout, null, projection, to, 0, selectedTable);
        list.setAdapter(adapter);
        getLoaderManager().initLoader(selectedLoader, null, this);

    }


    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
