package com.destiny.event.scheduler.fragments;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
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
import com.destiny.event.scheduler.utils.DateUtils;

public class DetailEventFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final String TAG = "DetailEventFragment";

    private static final int URL_LOADER_GAME = 60;
    private static final int URL_LOADER_ENTRY_MEMBERS = 72;

    private String gameId;

    View headerView;
    View includedView;
    ImageView eventIcon;
    TextView eventType;
    TextView eventName;
    TextView eventCreator;

    TextView date;
    TextView time;
    TextView light;
    TextView guardians;

    View footerView;
    Button joinButton;

    private ToActivityListener callback;

    private String[] gameProjection;
    private String[] membersProjection;

    private static final String[] from = {MemberTable.COLUMN_NAME, MemberTable.COLUMN_ICON, MemberTable.COLUMN_SINCE};
    private static final int[] to = {R.id.primary_text, R.id.profile_pic, R.id.text_points};

    CustomCursorAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        Bundle bundle = getArguments();
        if (bundle != null){
            gameId = bundle.getString("gameId");
        }

        callback = (ToActivityListener) getActivity();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.event_details);
        View v = inflater.inflate(R.layout.detail_event_layout, container, false);

        headerView = inflater.inflate(R.layout.detail_header_layout, null);
        footerView = inflater.inflate(R.layout.detail_footer_layout, null);

        includedView = headerView.findViewById(R.id.header);

        eventIcon = (ImageView) includedView.findViewById(R.id.icon_list);
        eventType = (TextView) includedView.findViewById(R.id.secondary_text);
        eventName = (TextView) includedView.findViewById(R.id.primary_text);

        date = (TextView) headerView.findViewById(R.id.date);
        time = (TextView) headerView.findViewById(R.id.time);
        light = (TextView) headerView.findViewById(R.id.light);
        guardians = (TextView) headerView.findViewById(R.id.guardians);

        joinButton = (Button) footerView.findViewById(R.id.btn_join);

        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        getGameData();

    }

    private void getGameData() {

        callback.onLoadingData();

        prepareGameStrings();
        getLoaderManager().initLoader(URL_LOADER_GAME, null, this);

        prepareMemberStrings();
        getLoaderManager().initLoader(URL_LOADER_ENTRY_MEMBERS, null, this);
        adapter = new CustomCursorAdapter(getContext(), R.layout.member_list_item_layout, null, from, to, 0, URL_LOADER_ENTRY_MEMBERS);

        if (headerView != null && footerView != null){
            this.getListView().addHeaderView(headerView);
            this.getListView().addFooterView(footerView);
        }

        setListAdapter(adapter);

    }

    private void prepareGameStrings() {

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

        gameProjection = new String[] {c1, c2, c3, c4, c5, c6, c7, c8, c9, c10, c11, c12, c13, c14, c15, c16, c17};

    }

    private void prepareMemberStrings() {

        String c1 = EntryTable.getQualifiedColumn(EntryTable.COLUMN_ID);
        String c2 = EntryTable.getQualifiedColumn(EntryTable.COLUMN_GAME);
        String c3 = EntryTable.getQualifiedColumn(EntryTable.COLUMN_MEMBERSHIP);
        String c4 = EntryTable.getQualifiedColumn(EntryTable.COLUMN_TIME);
        String c5 = MemberTable.getQualifiedColumn(MemberTable.COLUMN_MEMBERSHIP);
        String c6 = MemberTable.getQualifiedColumn(MemberTable.COLUMN_NAME);
        String c7 = MemberTable.getQualifiedColumn(MemberTable.COLUMN_ICON);
        String c8 = MemberTable.getQualifiedColumn(MemberTable.COLUMN_LIKES);
        String c9 = MemberTable.getQualifiedColumn(MemberTable.COLUMN_DISLIKES);
        String c10 = MemberTable.getQualifiedColumn(MemberTable.COLUMN_CREATED);
        String c11 = MemberTable.getQualifiedColumn(MemberTable.COLUMN_PLAYED);
        String c12 = MemberTable.getQualifiedColumn(MemberTable.COLUMN_SINCE);
        String c13 = GameTable.getAliasExpression(GameTable.COLUMN_ID);

        membersProjection = new String[] {c1, c2, c3, c4, c5, c6, c7, c8, c9, c10, c11, c12, c13};

    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        setHasOptionsMenu(true);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.clear();
    }

    @Override
    public void onDetach() {
        super.onDetach();

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String[] selectionArgs = {gameId};

        switch (id){
            case URL_LOADER_GAME:
                return new CursorLoader(
                        getContext(),
                        DataProvider.GAME_URI,
                        gameProjection,
                        GameTable.getQualifiedColumn(GameTable.COLUMN_ID)+ "=?",
                        selectionArgs,
                        null
                );
            case URL_LOADER_ENTRY_MEMBERS:
                return new CursorLoader(
                        getContext(),
                        DataProvider.ENTRY_MEMBERS_URI,
                        membersProjection,
                        EntryTable.getQualifiedColumn(EntryTable.COLUMN_GAME) + "=?",
                        selectionArgs,
                        "datetime(" + EntryTable.getQualifiedColumn(EntryTable.COLUMN_TIME) + ") ASC"
                );
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        if (data != null && data.moveToFirst()){
            switch (loader.getId()){
                case URL_LOADER_GAME:
                    eventIcon.setImageResource(getContext().getResources().getIdentifier(data.getString(data.getColumnIndexOrThrow(EventTable.getQualifiedColumn(EventTable.COLUMN_ICON))), "drawable", getContext().getPackageName() ));
                    eventName.setText(getContext().getResources().getIdentifier(data.getString(data.getColumnIndexOrThrow(EventTable.getAliasColumn(EventTable.COLUMN_NAME))), "string", getContext().getPackageName()));
                    eventType.setText(getContext().getResources().getIdentifier(data.getString(data.getColumnIndexOrThrow(EventTypeTable.getQualifiedColumn(EventTypeTable.COLUMN_NAME))), "string", getContext().getPackageName()));
                    date.setText(DateUtils.onBungieDate(data.getString(data.getColumnIndexOrThrow(GameTable.COLUMN_TIME))));
                    time.setText(DateUtils.getTime(data.getString(data.getColumnIndexOrThrow(GameTable.COLUMN_TIME))));
                    light.setText(data.getString(data.getColumnIndexOrThrow(GameTable.getQualifiedColumn(GameTable.COLUMN_LIGHT))));

                    int max = data.getInt(data.getColumnIndexOrThrow(EventTable.getQualifiedColumn(EventTable.COLUMN_GUARDIANS)));
                    int insc = data.getInt(data.getColumnIndexOrThrow(GameTable.COLUMN_INSCRIPTIONS));
                    String sg = insc + " " + getContext().getResources().getString(R.string.of) + " " + max;
                    guardians.setText(sg);
                    Log.w(TAG, "Game Cursor: " + DatabaseUtils.dumpCursorToString(data));
                    break;
                case URL_LOADER_ENTRY_MEMBERS:
                    adapter.swapCursor(data);
                    Log.w(TAG, "Entry Cursor: " + DatabaseUtils.dumpCursorToString(data));
                    break;

            }
        }

        callback.onDataLoaded();

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        switch (loader.getId()){
            case URL_LOADER_ENTRY_MEMBERS:
                adapter.swapCursor(null);
                break;
        }

    }
}
