package com.destiny.event.scheduler.fragments;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import com.destiny.event.scheduler.data.EventTable;
import com.destiny.event.scheduler.data.EventTypeTable;
import com.destiny.event.scheduler.data.GameTable;
import com.destiny.event.scheduler.data.MemberTable;
import com.destiny.event.scheduler.interfaces.RefreshDataListener;
import com.destiny.event.scheduler.interfaces.ToActivityListener;
import com.destiny.event.scheduler.interfaces.UserDataListener;
import com.destiny.event.scheduler.provider.DataProvider;

import java.util.ArrayList;

public class ValidateListFragment extends ListFragment implements RefreshDataListener, UserDataListener, LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = "ValidateListFragment";

    private static final int LOADER_GAME = 60;

    private ToActivityListener callback;

    CustomCursorAdapter adapter;

    View headerView;

    TextView sectionTitle;

    private String[] projection;
    private String[] from;
    private static final int[] to = {R.id.primary_text, R.id.game_image, R.id.secondary_text, R.id.type_text, R.id.status_img};

    private ArrayList<String> statusIdList;
    private ArrayList<String> creatorList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        callback.deleteRefreshListener(this);
        callback.deleteUserDataListener(this);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        callback = (ToActivityListener) getActivity();
        callback.registerRefreshListener(this);
        callback.registerUserDataListener(this);
        Log.w(TAG, "ValidateListFragment attached!");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.validate_list_layout, container, false);

        headerView = inflater.inflate(R.layout.list_section_layout, null);

        sectionTitle = (TextView) headerView.findViewById(R.id.section_title);

        statusIdList = new ArrayList<>();
        creatorList = new ArrayList<>();

        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        callback.onSelectedFragment(2);

        sectionTitle.setText(R.string.games_available);

        String bungieId = callback.getBungieId();

        if (bungieId != null){
            getEvents();
        }

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        setListAdapter(null);
    }

    private void getEvents() {

        prepareStrings();
        getLoaderManager().initLoader(LOADER_GAME, null, this);
        adapter = new CustomCursorAdapter(getContext(), R.layout.done_game_item, null, from, to, 0, LOADER_GAME);

        if (headerView != null){
            this.getListView().addHeaderView(headerView);
        }

        setListAdapter(adapter);

    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        if (position > 0 ){
            callback.onGameSelected(String.valueOf(id), TAG, creatorList.get(position - 1), statusIdList.get(position-1));
        }
    }

    private void prepareStrings() {

        String c1 = GameTable.getQualifiedColumn(GameTable.COLUMN_ID);
        String c2 = GameTable.COLUMN_CREATOR;
        String c3 = GameTable.COLUMN_TIME;
        String c4 = GameTable.COLUMN_LIGHT;
        String c5 = GameTable.COLUMN_INSCRIPTIONS;
        String c6 = GameTable.COLUMN_CREATOR_NAME;
        String c7 = GameTable.COLUMN_STATUS;

        String c8 = MemberTable.COLUMN_NAME;

        String c9 = EventTypeTable.COLUMN_NAME;
        String c10 = EventTypeTable.COLUMN_ICON;

        String c11 = EventTable.COLUMN_ICON;
        String c12 = EventTable.COLUMN_NAME;
        String c13 = EventTable.COLUMN_GUARDIANS;
        String c14 = EventTable.COLUMN_TYPE;

        projection = new String[] {c1, c2, c3, c4, c5, c6, c7, c8, c9, c10, c11, c12, c13, c14};

        from = new String[] {c2, c11, c12, c13, c9};

    }

    @Override
    public void onRefreshData() {

    }

    @Override
    public Fragment getFragment() {
        return this;
    }

    @Override
    public void onUserDataLoaded() {

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
                        GameTable.COLUMN_STATUS + "=" + GameTable.STATUS_WAITING + " OR " + GameTable.COLUMN_STATUS + "=" + GameTable.STATUS_VALIDATED,
                        null,
                        "datetime(" + GameTable.COLUMN_TIME + ") ASC"
                );
            default:
                return null;
        }

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        if (data != null && data.moveToFirst()){
            switch (loader.getId()){
                case LOADER_GAME:
                    adapter.swapCursor(data);

                    data.moveToFirst();
                    for (int i=0; i < data.getCount();i++){
                        statusIdList.add(i, data.getString(data.getColumnIndexOrThrow(GameTable.COLUMN_STATUS)));
                        creatorList.add(i, data.getString(data.getColumnIndex(GameTable.COLUMN_CREATOR)));
                        data.moveToNext();
                    }

                    break;
            }
        }

        callback.onDataLoaded();

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }
}
