package com.destiny.event.scheduler.fragments;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.destiny.event.scheduler.R;
import com.destiny.event.scheduler.adapters.CustomCursorAdapter;
import com.destiny.event.scheduler.data.EntryTable;
import com.destiny.event.scheduler.data.EvaluationTable;
import com.destiny.event.scheduler.data.EventTable;
import com.destiny.event.scheduler.data.EventTypeTable;
import com.destiny.event.scheduler.data.GameTable;
import com.destiny.event.scheduler.data.MemberTable;
import com.destiny.event.scheduler.interfaces.ToActivityListener;
import com.destiny.event.scheduler.provider.DataProvider;

import java.util.ArrayList;
import java.util.Calendar;

public class DetailHistoryFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = "DetailEventFragment";

    private static final int LOADER_HISTORY = 74;

    private String gameId;
    private String origin;
    private String gameStatus;
    private String creator;
    private int inscriptions;
    private int maxGuardians;
    private String gameEventName;
    private String gameEventTypeName;
    private int gameEventIcon;
    private String gameTime;
    private Calendar eventCalendar;

    private ArrayList<String> bungieIdList;

    View headerView;
    View includedView;
    ImageView eventIcon;
    TextView eventType;
    TextView eventName;

    TextView date;
    TextView time;
    TextView light;
    TextView guardians;

    private ToActivityListener callback;

    private String[] gameProjection;

    private static final String[] from = {MemberTable.COLUMN_NAME, MemberTable.COLUMN_ICON}; //Atualizar com os campos que serão exibidos
    private static final int[] to = {R.id.primary_text, R.id.profile_pic, R.id.txt_xp, R.id.txt_likes, R.id.txt_dislikes};

    CustomCursorAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        callback = (ToActivityListener) getActivity();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.event_details);
        View v = inflater.inflate(R.layout.detail_event_layout, container, false);

        headerView = inflater.inflate(R.layout.detail_header_layout, null);

        includedView = headerView.findViewById(R.id.header);

        eventIcon = (ImageView) includedView.findViewById(R.id.icon_list);
        eventType = (TextView) includedView.findViewById(R.id.secondary_text);
        eventName = (TextView) includedView.findViewById(R.id.primary_text);

        date = (TextView) headerView.findViewById(R.id.date);
        time = (TextView) headerView.findViewById(R.id.time);
        light = (TextView) headerView.findViewById(R.id.light);
        guardians = (TextView) headerView.findViewById(R.id.guardians);

        Bundle bundle = getArguments();
        if (bundle != null) {
            gameId = bundle.getString("gameId");
        }

        bungieIdList = new ArrayList<>();

        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        getGameData();

    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        String bungieId = bungieIdList.get(position - 1);

        Fragment fragment = new MyProfileFragment();

        Bundle bundle = new Bundle();
        bundle.putString("bungieId", bungieId);
        if (bungieId.equals(callback.getBungieId())) {
            bundle.putInt("type", MyProfileFragment.TYPE_USER);
        } else {
            bundle.putInt("type", MyProfileFragment.TYPE_MEMBER);
        }
        bundle.putString("clanName", callback.getClanName());

        callback.loadNewFragment(fragment, bundle, "profile");
    }

    private void getGameData() {

        if (headerView != null){
            this.getListView().addHeaderView(headerView, null, false);
        }

        adapter = new CustomCursorAdapter(getContext(), R.layout.history_member_item, null, from, to, 0, 0);
        setListAdapter(adapter);

        callback.onLoadingData();
        prepareStrings();
        getLoaderManager().initLoader(LOADER_HISTORY, null, this);
    }

    private void prepareStrings() {

        String c1 = EntryTable.getQualifiedColumn(EntryTable.COLUMN_ID);
        String c2 = EntryTable.COLUMN_TIME;

        String c3 = GameTable.getQualifiedColumn(GameTable.COLUMN_ID);
        String c4 = GameTable.COLUMN_INSCRIPTIONS;
        String c5 = GameTable.COLUMN_TIME;
        String c6 = GameTable.COLUMN_LIGHT;

        String c7 = EventTable.COLUMN_NAME;
        String c8 = EventTable.COLUMN_ICON;

        String c9= EventTypeTable.COLUMN_NAME;

        String c10 = MemberTable.COLUMN_NAME;
        String c11 = MemberTable.COLUMN_ICON;

        String c12 = "SUM(CASE WHEN " + EvaluationTable.COLUMN_EVALUATION + " = -1 THEN 1 ELSE 0) AS dislikes"; // colocar um AND para somar apenas os que são do memberId
        String c13 = "SUM(CASE WHEN " + EvaluationTable.COLUMN_EVALUATION + " = 1 THEN 1 ELSE 0) AS likes";

        gameProjection = new String[]{c1, c2, c3, c4, c5, c6, c7, c8, c9, c10, c11, c12, c13};

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

        switch (id) {
            case LOADER_HISTORY:
                return new CursorLoader(
                        getContext(),
                        DataProvider.ENTRY_HISTORY_URI,
                        gameProjection,
                        GameTable.getQualifiedColumn(GameTable.COLUMN_ID) + "=?",
                        selectionArgs,
                        null
                );
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        if (data != null && data.moveToFirst()) {
            switch (loader.getId()) {
                case LOADER_HISTORY:
                    adapter.swapCursor(data);

                    data.moveToFirst();
                    for (int i = 0; i < data.getCount(); i++) {
                        bungieIdList.add(i, data.getString(data.getColumnIndexOrThrow(EntryTable.COLUMN_MEMBERSHIP)));
                        data.moveToNext();
                    }
                    //Log.w(TAG, "Entry Cursor: " + DatabaseUtils.dumpCursorToString(data));
                    break;
            }
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}