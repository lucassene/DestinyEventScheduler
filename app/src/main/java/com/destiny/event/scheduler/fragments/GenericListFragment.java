package com.destiny.event.scheduler.fragments;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.destiny.event.scheduler.R;
import com.destiny.event.scheduler.activities.DrawerActivity;
import com.destiny.event.scheduler.adapters.CustomCursorAdapter;
import com.destiny.event.scheduler.data.EventTable;
import com.destiny.event.scheduler.data.EventTypeTable;
import com.destiny.event.scheduler.interfaces.ToActivityListener;
import com.destiny.event.scheduler.provider.DataProvider;

public class GenericListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private String title;
    private String tableName;
    private int gameType;

    private ToActivityListener callback;

    private TextView titleView;

    private CustomCursorAdapter adapter;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        setHasOptionsMenu(true);
        callback = (ToActivityListener) getActivity();
        callback.setFragmentType(DrawerActivity.FRAGMENT_TYPE_WITH_BACKSTACK);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.generic_list_layout, container, false);

        titleView = (TextView) v.findViewById(R.id.title);

        fillData(title, tableName);

        return v;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        setListAdapter(null);
    }

    private void fillData(String title, String tableName) {

        String[] from;
        int[] to;

        titleView.setText(title);
        switch (tableName){
            case EventTypeTable.TABLE_NAME:
                from = new String[] {EventTypeTable.COLUMN_EN, EventTypeTable.COLUMN_PT, EventTypeTable.COLUMN_ES, EventTypeTable.COLUMN_ICON};
                to = new int[] {R.id.primary_text, R.id.icon};
                getLoaderManager().initLoader(10, null, this);
                adapter = new CustomCursorAdapter(getContext(), R.layout.event_list_item_layout, null, from, to, 0, 10);
                setListAdapter(adapter);
                break;
            case EventTable.TABLE_NAME:
                from = new String[] {EventTable.COLUMN_EN, EventTable.COLUMN_PT, EventTable.COLUMN_ES, EventTable.COLUMN_ICON};
                to = new int[] {R.id.primary_text, R.id.icon};
                getLoaderManager().initLoader(20, null, this);
                adapter = new CustomCursorAdapter(getContext(), R.layout.event_list_item_layout, null, from, to, 0, 20);
                setListAdapter(adapter);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        Bundle bundle = getArguments();
        if (bundle != null){
            title = bundle.getString("title");
            tableName = bundle.getString("table");
            if (bundle.getInt("type") != 0){
                gameType = bundle.getInt("type");
            }
        }
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.clear();
        getActivity().getMenuInflater().inflate(R.menu.empty_menu, menu);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {

        switch (tableName){
            case EventTypeTable.TABLE_NAME:
                callback.onEventTypeSelected(String.valueOf(id));
                break;
            case EventTable.TABLE_NAME:
                callback.onEventGameSelected(String.valueOf(id));
                break;
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        callback.onLoadingData();

        String[] projection;
        CursorLoader cursorLoader;
        String[] selectionArgs;

        switch (id){
            case 10:
                projection = EventTypeTable.ALL_COLUMNS;
                getContext().getResources();
                cursorLoader = new CursorLoader(
                        getActivity(),
                        DataProvider.EVENT_TYPE_URI,
                        projection,
                        null,
                        null,
                        Resources.getSystem().getConfiguration().locale.getLanguage() + " ASC");
                break;
            case 20:
                projection = EventTable.ALL_COLUMNS;
                selectionArgs = new String[] {String.valueOf(gameType)};
                getContext().getResources();
                cursorLoader = new CursorLoader(getActivity(),
                        DataProvider.EVENT_URI,
                        projection,
                        EventTable.COLUMN_TYPE + "=?",
                        selectionArgs,
                        Resources.getSystem().getConfiguration().locale.getLanguage() + " ASC");
                break;
            default:
                cursorLoader = null;
        }

        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.swapCursor(data);

        if (loader.getId()==20){
            data.moveToFirst();
            gameType = data.getInt(data.getColumnIndexOrThrow(EventTable.COLUMN_TYPE));
        }

        callback.onDataLoaded();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }
}
