package com.destiny.event.scheduler.fragments;

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
import com.destiny.event.scheduler.data.EventTable;
import com.destiny.event.scheduler.data.GameTable;
import com.destiny.event.scheduler.data.MemberTable;
import com.destiny.event.scheduler.interfaces.ToActivityListener;
import com.destiny.event.scheduler.provider.DataProvider;

public class NewEventsListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = "NewEventsListFragment";

    private static final int URL_LOADER_GAME = 60;

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

        View v = inflater.inflate(R.layout.new_list_layout, container, false);

        headerView = inflater.inflate(R.layout.list_section_layout, null);

        sectionTitle = (TextView) headerView.findViewById(R.id.section_title);

        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sectionTitle.setText(R.string.games_available);

        getNewEvents();
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
        String c11 = GameTable.getQualifiedColumn(GameTable.COLUMN_GUARDIANS); // game.guardians;
        String c12 = GameTable.getQualifiedColumn(GameTable.COLUMN_INSCRIPTIONS); // game.inscriptions;
        String c13 = MemberTable.getAliasExpression(MemberTable.COLUMN_ID); // member._ID AS member__ID;
        String c14 = GameTable.getAliasExpression(GameTable.COLUMN_CREATOR_NAME);

        projection = new String[] {c1, c2, c3, c4, c5, c6, c7, c8, c9, c10, c11, c12, c13, c14};

        String f1 = EventTable.getAliasColumn(EventTable.COLUMN_NAME);
        String f2 = GameTable.getAliasColumn(GameTable.COLUMN_CREATOR_NAME);

        from = new String[] {f1, c4, f2, c9, c9, c12, c11};

    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String[] selectionArgs = {GameTable.GAME_NEW};

        callback.onLoadingData();

        switch (id){
            case URL_LOADER_GAME:
                return new CursorLoader(
                        getContext(),
                        DataProvider.GAME_URI,
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

        if (data != null && data.moveToFirst()){
            switch (loader.getId()){
                case URL_LOADER_GAME:
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
