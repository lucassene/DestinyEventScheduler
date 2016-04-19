package com.destiny.event.scheduler.fragments;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.destiny.event.scheduler.R;
import com.destiny.event.scheduler.adapters.CustomCursorAdapter;
import com.destiny.event.scheduler.data.EventTable;
import com.destiny.event.scheduler.data.GameTable;
import com.destiny.event.scheduler.interfaces.ToActivityListener;
import com.destiny.event.scheduler.provider.DataProvider;

public class NewEventsListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = "NewEventsListFragment";

    private static final int URL_LOADER_GAME = 60;

    private static final String[] from = {EventTable.COLUMN_NAME, EventTable.COLUMN_ICON, GameTable.COLUMN_CREATOR, GameTable.COLUMN_TIME, GameTable.COLUMN_LIGHT, GameTable.COLUMN_GUARDIANS};
    private static final int[] to = {R.id.primary_text, R.id.game_image, R.id.secondary_text, R.id.game_date, R.id.game_time, R.id.game_max};

    CustomCursorAdapter adapter;

    View headerView;

    TextView sectionTitle;

    private ToActivityListener callback;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        getLoaderManager().initLoader(URL_LOADER_GAME, null, this);
        adapter = new CustomCursorAdapter(getContext(), R.layout.game_list_item_layout, null, from, to, 0, URL_LOADER_GAME);

        if (headerView != null){
            this.getListView().addHeaderView(headerView);
        }

        setListAdapter(adapter);

    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String[] projection = {GameTable.TABLE_NAME + "." + GameTable.COLUMN_ID, EventTable.COLUMN_NAME, EventTable.COLUMN_ICON, GameTable.COLUMN_CREATOR, GameTable.COLUMN_EVENT_ID, GameTable.COLUMN_TIME, GameTable.COLUMN_LIGHT, GameTable.COLUMN_GUARDIANS};

        switch (id){
            case URL_LOADER_GAME:
                return new CursorLoader(
                        getContext(),
                        DataProvider.GAME_EVENT_URI,
                        projection,
                        null,
                        null,
                        null
                );
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        switch (loader.getId()){
            case URL_LOADER_GAME:
                adapter.swapCursor(data);
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }
}
