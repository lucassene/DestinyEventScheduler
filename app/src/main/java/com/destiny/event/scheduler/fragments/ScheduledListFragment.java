package com.destiny.event.scheduler.fragments;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.destiny.event.scheduler.R;
import com.destiny.event.scheduler.adapters.CustomCursorAdapter;
import com.destiny.event.scheduler.data.EntryTable;
import com.destiny.event.scheduler.data.EventTable;
import com.destiny.event.scheduler.data.GameTable;
import com.destiny.event.scheduler.data.MemberTable;
import com.destiny.event.scheduler.interfaces.ToActivityListener;
import com.destiny.event.scheduler.provider.DataProvider;

public class ScheduledListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final String TAG = "ScheduledListFragment";

    private static final int LOADER_ENTRY = 70;

    private static final int[] to = {R.id.primary_text, R.id.game_image, R.id.secondary_text, R.id.game_date, R.id.game_time, R.id.game_actual, R.id.game_max};

    CustomCursorAdapter adapter;

    View headerView;

    TextView sectionTitle;

    private ToActivityListener callback;

    private String[] projection;
    private String[] from;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        callback = (ToActivityListener) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.scheduled_list_layout, container, false);

        headerView = inflater.inflate(R.layout.list_section_layout, null);

        sectionTitle = (TextView) headerView.findViewById(R.id.section_title);

        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sectionTitle.setText(R.string.scheduled_games);

        getScheduledEvents();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
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
        String c2 = EntryTable.getQualifiedColumn(EntryTable.COLUMN_MEMBERSHIP);
        String c3 = EntryTable.getQualifiedColumn(EntryTable.COLUMN_GAME);
        String c4 = GameTable.getAliasExpression(GameTable.COLUMN_ID);
        String c5 = GameTable.getQualifiedColumn(GameTable.COLUMN_EVENT_ID);
        String c6 = GameTable.getQualifiedColumn(GameTable.COLUMN_CREATOR);
        String c7 = GameTable.getAliasExpression(GameTable.COLUMN_CREATOR_NAME);
        String c8 = GameTable.getQualifiedColumn(GameTable.COLUMN_TIME);
        String c9 = GameTable.getQualifiedColumn(GameTable.COLUMN_LIGHT);
        String c10 = GameTable.getQualifiedColumn(GameTable.COLUMN_GUARDIANS);
        String c11 = GameTable.getQualifiedColumn(GameTable.COLUMN_INSCRIPTIONS);
        String c12 = GameTable.getQualifiedColumn(GameTable.COLUMN_STATUS);
        String c13 = EventTable.getAliasExpression(EventTable.COLUMN_ID);
        String c14 = EventTable.getQualifiedColumn(EventTable.COLUMN_ICON);
        String c15 = EventTable.getAliasExpression(EventTable.COLUMN_NAME);
        String c16 = EventTable.getQualifiedColumn(EventTable.COLUMN_GUARDIANS);
        String c17 = MemberTable.getAliasExpression(MemberTable.COLUMN_ID);
        String c18 = MemberTable.getQualifiedColumn(MemberTable.COLUMN_MEMBERSHIP);

        projection = new String[] {c1, c2, c3, c4, c5, c6, c7, c8, c9, c10, c11, c12, c13, c14, c15, c16, c17, c18};

        String f1 = EventTable.getAliasColumn(EventTable.COLUMN_NAME);
        String f2 = GameTable.getAliasColumn(GameTable.COLUMN_CREATOR_NAME);

        from = new String[] {f1, c14, f2, c8, c8, c11, c16};

    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] selectionArgs = {GameTable.GAME_SCHEDULED};
        String orderBy = GameTable.getQualifiedColumn(GameTable.COLUMN_TIME);

        callback.onLoadingData();

        switch (id){
            case LOADER_ENTRY:
                return new CursorLoader(
                        getContext(),
                        DataProvider.ENTRY_URI,
                        projection,
                        GameTable.getQualifiedColumn(GameTable.COLUMN_STATUS) + "=?",
                        selectionArgs,
                        null
                );
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {


        Log.w(TAG, DatabaseUtils.dumpCursorToString(data));

        if (data !=null && data.moveToFirst()){
            switch (loader.getId()){
                case LOADER_ENTRY:
                    adapter.swapCursor(data);
            }
            callback.onDataLoaded();
        }




    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }

}
