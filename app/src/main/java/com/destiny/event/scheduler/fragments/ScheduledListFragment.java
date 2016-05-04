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
import com.destiny.event.scheduler.utils.DateUtils;

import java.util.ArrayList;
import java.util.Calendar;

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

        sectionTitle.setText(R.string.scheduled_games);

        String bungieId = callback.getBungieId();

        if (bungieId != null){
            getScheduledEvents();
        }

        callback.onLoadingData();

    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        //Toast.makeText(getContext(), "GameID Selected: " + gameIdList.get(position-1), Toast.LENGTH_SHORT).show();
        callback.onGameSelected(gameIdList.get(position-1), TAG, gameCreatorList.get(position-1), GameTable.GAME_NEW);
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        callback = (ToActivityListener) getActivity();
        callback.registerRefreshListener(this);
        callback.registerUserDataListener(this);
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

        String c1 = EntryTable.getQualifiedColumn(EntryTable.COLUMN_ID); // entry._ID;
        String c2 = EntryTable.getQualifiedColumn(EntryTable.COLUMN_MEMBERSHIP); //entry.entry_membership;

        String c3 = GameTable.getAliasExpression(GameTable.COLUMN_ID); // game._ID AS game__ID;
        String c4 = GameTable.getQualifiedColumn(GameTable.COLUMN_EVENT_ID); // game.event_id;
        String c5 = GameTable.getQualifiedColumn(GameTable.COLUMN_CREATOR); // game.creator;
        String c6 = GameTable.getQualifiedColumn(GameTable.COLUMN_TIME); // game.time;
        String c7 = GameTable.getQualifiedColumn(GameTable.COLUMN_LIGHT); // game.light;
        String c8 = GameTable.getQualifiedColumn(GameTable.COLUMN_INSCRIPTIONS); // game.inscriptions;
        String c9 = GameTable.getQualifiedColumn(GameTable.COLUMN_CREATOR_NAME); // game.creator AS game_creator;

        String c10 = MemberTable.getAliasExpression(MemberTable.COLUMN_ID); // member._ID AS member__ID;
        String c11 = MemberTable.getQualifiedColumn(MemberTable.COLUMN_MEMBERSHIP); // member.membership;
        String c12 = MemberTable.getQualifiedColumn(MemberTable.COLUMN_NAME); // member.name AS member_name;

        String c13 = EventTypeTable.getAliasExpression(EventTypeTable.COLUMN_ID); // event_type._ID AS event_type__ID;
        String c14 = EventTypeTable.getQualifiedColumn(EventTypeTable.COLUMN_NAME); // event_type.type_name;

        String c15 = EventTable.getAliasExpression(EventTable.COLUMN_ID); // event._ID AS event__ID;
        String c16 = EventTable.getQualifiedColumn(EventTable.COLUMN_ICON); // event.icon;
        String c17 = EventTable.getQualifiedColumn(EventTable.COLUMN_NAME); // event.name AS event_name;
        String c18 = EventTable.getQualifiedColumn(EventTable.COLUMN_GUARDIANS); // event.guardians;
        String c19 = EventTable.getQualifiedColumn(EventTable.COLUMN_TYPE); // event.type_of_event;

        projection = new String[] {c1, c2, c3, c4, c5, c6, c7, c8, c9, c10, c11, c12, c13, c14, c15, c16, c17, c18, c19};

        from = new String[] {c17, c16, c5, c6, c6, c8, c18, c14};

    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] selectionArgs = {callback.getBungieId()};

        switch (id){
            case LOADER_ENTRY:
                return new CursorLoader(
                        getContext(),
                        DataProvider.ENTRY_URI,
                        projection,
                        EntryTable.getQualifiedColumn(EntryTable.COLUMN_MEMBERSHIP) + "=?",
                        selectionArgs,
                        "datetime(" + GameTable.getQualifiedColumn(GameTable.COLUMN_TIME) + ") ASC"
                );
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        Log.w(TAG, DatabaseUtils.dumpCursorToString(data));

        String time = "";
        int icon = 0;
        String title = "";

        if (data !=null && data.moveToFirst()){
            switch (loader.getId()){
                case LOADER_ENTRY:
                    adapter.swapCursor(data);
                    data.moveToFirst();

                    time = data.getString(data.getColumnIndexOrThrow(GameTable.getQualifiedColumn(GameTable.COLUMN_TIME)));
                    icon = getContext().getResources().getIdentifier(data.getString(data.getColumnIndexOrThrow(EventTable.COLUMN_ICON)), "drawable", getContext().getPackageName());
                    title = getContext().getResources().getString(getContext().getResources().getIdentifier(data.getString(data.getColumnIndexOrThrow(EventTable.getQualifiedColumn(EventTable.COLUMN_NAME))), "string", getContext().getPackageName()));
                    Log.w("NotificationService", "Origin Icon: " + icon + " / Origin Title: " + title);

                    for (int i=0; i < data.getCount();i++){
                        gameIdList.add(i, data.getString(data.getColumnIndexOrThrow(GameTable.getAliasColumn(GameTable.COLUMN_ID))));
                        gameCreatorList.add(i, data.getString(data.getColumnIndex(GameTable.getQualifiedColumn(GameTable.COLUMN_CREATOR))));
                        data.moveToNext();
                    }
            }

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.YEAR, Integer.parseInt(DateUtils.getYear(time)));
            calendar.set(Calendar.MONTH, Integer.parseInt(DateUtils.getMonth(time))-1);
            calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(DateUtils.getDay(time)));
            calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(DateUtils.getHour(time)));
            calendar.set(Calendar.MINUTE, Integer.parseInt(DateUtils.getMinute(time)));
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.AM_PM, Calendar.PM);

            callback.registerAlarmTask(calendar, title, icon);
            callback.onDataLoaded();
        } else {
            callback.onNoScheduledGames();
        }

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
    public void onUserDataLoaded() {
        getScheduledEvents();
    }
}
