package com.destiny.event.scheduler.fragments;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.destiny.event.scheduler.R;
import com.destiny.event.scheduler.adapters.HistoryAdapter;
import com.destiny.event.scheduler.data.EntryTable;
import com.destiny.event.scheduler.data.EvaluationTable;
import com.destiny.event.scheduler.data.EventTable;
import com.destiny.event.scheduler.data.EventTypeTable;
import com.destiny.event.scheduler.data.GameTable;
import com.destiny.event.scheduler.data.MemberTable;
import com.destiny.event.scheduler.interfaces.ToActivityListener;
import com.destiny.event.scheduler.provider.DataProvider;
import com.destiny.event.scheduler.utils.DateUtils;

import java.util.ArrayList;

public class DetailHistoryFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = "DetailHistoryFragment";

    private static final int LOADER_HISTORY = 74;

    private String gameId;
    private ArrayList<String> bungieIdList;

    View headerView;
    View includedView;
    ImageView eventIcon;
    TextView eventType;
    TextView eventName;
    TextView sectionTitle;

    TextView date;
    TextView time;
    TextView light;
    TextView guardians;

    private ToActivityListener callback;

    private String[] gameProjection;

    private static final String[] from = {MemberTable.COLUMN_NAME, MemberTable.COLUMN_ICON, "likes", "dislikes", "likes", "dislikes"}; //Atualizar com os campos que serão exibidos
    private static final int[] to = {R.id.primary_text, R.id.profile_pic, R.id.txt_xp, R.id.txt_likes, R.id.txt_dislikes};

    HistoryAdapter adapter;

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
        sectionTitle = (TextView) headerView.findViewById(R.id.section_guardians);
        sectionTitle.setText(R.string.participants_guardians);

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
    public void onDestroyView() {
        super.onDestroyView();
        setListAdapter(null);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        String bungieId = bungieIdList.get(position - 1);

        Fragment fragment = new MyNewProfileFragment();

        Bundle bundle = new Bundle();
        bundle.putString("bungieId", bungieId);
        if (bungieId.equals(callback.getBungieId())) {
            bundle.putInt("type", MyNewProfileFragment.TYPE_USER);
        } else {
            bundle.putInt("type", MyNewProfileFragment.TYPE_MEMBER);
        }
        bundle.putString("clanName", callback.getClanName());

        callback.loadNewFragment(fragment, bundle, "profile");
    }

    private void getGameData() {

        if (headerView != null){
            this.getListView().addHeaderView(headerView, null, false);
        }

        adapter = new HistoryAdapter(getContext(), R.layout.history_member_item, null, from, to, 0);
        setListAdapter(adapter);

        callback.onLoadingData();
        prepareStrings();
        getLoaderManager().initLoader(LOADER_HISTORY, null, this);
    }

    private void prepareStrings() {

        String c1 = EntryTable.getQualifiedColumn(EntryTable.COLUMN_ID);
        String c2 = EntryTable.COLUMN_TIME;

        String c3 = GameTable.COLUMN_INSCRIPTIONS;
        String c4 = GameTable.COLUMN_TIME;
        String c5 = GameTable.COLUMN_LIGHT;
        String c6 = GameTable.COLUMN_CREATOR;

        String c7 = EventTable.COLUMN_NAME;
        String c8 = EventTable.COLUMN_ICON;
        String c9 = EventTable.COLUMN_GUARDIANS;

        String c10= EventTypeTable.COLUMN_NAME;

        String c11 = MemberTable.COLUMN_MEMBERSHIP;
        String c12 = MemberTable.COLUMN_NAME;
        String c13 = MemberTable.COLUMN_ICON;

        String c14 = "SUM(CASE WHEN " + EvaluationTable.COLUMN_EVALUATION + " = -1 AND " + EvaluationTable.COLUMN_MEMBERSHIP_B + "=" + MemberTable.COLUMN_MEMBERSHIP + " THEN 1 ELSE 0 END) AS dislikes"; // colocar um AND para somar apenas os que são do memberId
        String c15 = "SUM(CASE WHEN " + EvaluationTable.COLUMN_EVALUATION + " = 1 AND " + EvaluationTable.COLUMN_MEMBERSHIP_B + "=" + MemberTable.COLUMN_MEMBERSHIP + " THEN 1 ELSE 0 END) AS likes";

        gameProjection = new String[]{c1, c2, c3, c4, c5, c6, c7, c8, c9, c10, c11, c12, c13, c14, c15};

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

        String sumDislikes = "SUM(CASE WHEN " + EvaluationTable.COLUMN_EVALUATION + "=-1 THEN 1 ELSE 0 END) AS dislikes";
        String sumLikes = "SUM(CASE WHEN " + EvaluationTable.COLUMN_EVALUATION + "=1 THEN 1 ELSE 0 END) AS likes";
        String totalSum = "SUM(" + EvaluationTable.COLUMN_EVALUATION + ") AS sum";

        String[] proj = new String[] {EvaluationTable.COLUMN_GAME, EvaluationTable.COLUMN_MEMBERSHIP_A, EvaluationTable.COLUMN_MEMBERSHIP_B, sumLikes, sumDislikes};

        switch (id) {
            case LOADER_HISTORY:
                return new CursorLoader(
                        getContext(),
                        DataProvider.ENTRY_HISTORY_URI,
                        gameProjection,
                        EntryTable.COLUMN_GAME + "=" + gameId,
                        null,
                        "datetime(" + EntryTable.COLUMN_TIME + ") ASC"
                );
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        if (data != null && data.moveToFirst()) {
            Log.w(TAG, DatabaseUtils.dumpCursorToString(data));
            switch (loader.getId()) {
                case LOADER_HISTORY:
                    adapter.swapCursor(data);

                    eventIcon.setImageResource(getContext().getResources().getIdentifier(data.getString(data.getColumnIndexOrThrow(EventTable.COLUMN_ICON)), "drawable", getContext().getPackageName() ));

                    String gameEventName = getContext().getResources().getString(getContext().getResources().getIdentifier(data.getString(data.getColumnIndexOrThrow(EventTable.COLUMN_NAME)), "string", getContext().getPackageName()));
                    eventName.setText(gameEventName);

                    String gameEventTypeName = getContext().getResources().getString(getContext().getResources().getIdentifier(data.getString(data.getColumnIndexOrThrow(EventTypeTable.COLUMN_NAME)), "string", getContext().getPackageName()));
                    eventType.setText(gameEventTypeName);

                    date.setText(DateUtils.onBungieDate(data.getString(data.getColumnIndexOrThrow(GameTable.COLUMN_TIME))));
                    time.setText(DateUtils.getTime(data.getString(data.getColumnIndexOrThrow(GameTable.COLUMN_TIME))));
                    light.setText(data.getString(data.getColumnIndexOrThrow(GameTable.COLUMN_LIGHT)));

                    int maxGuardians = data.getInt(data.getColumnIndexOrThrow(EventTable.COLUMN_GUARDIANS));
                    int inscriptions = data.getInt(data.getColumnIndexOrThrow(GameTable.COLUMN_INSCRIPTIONS));
                    String sg = inscriptions + " " + getContext().getResources().getString(R.string.of) + " " + maxGuardians;
                    guardians.setText(sg);

                    data.moveToFirst();
                    for (int i = 0; i < data.getCount(); i++) {
                        bungieIdList.add(i, data.getString(data.getColumnIndexOrThrow(MemberTable.COLUMN_MEMBERSHIP)));
                        data.moveToNext();
                    }
                    //Log.w(TAG, "Entry Cursor: " + DatabaseUtils.dumpCursorToString(data));
                    break;
            }
            callback.onDataLoaded();
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }
}