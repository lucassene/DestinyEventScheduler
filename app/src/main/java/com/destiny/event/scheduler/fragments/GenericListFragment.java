package com.destiny.event.scheduler.fragments;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import com.destiny.event.scheduler.adapters.CustomCursorAdapter;
import com.destiny.event.scheduler.data.EventTable;
import com.destiny.event.scheduler.data.EventTypeTable;
import com.destiny.event.scheduler.interfaces.ToActivityListener;
import com.destiny.event.scheduler.provider.DataProvider;

public class GenericListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private String title;
    private String tableName;
    private String gameType;

    private Fragment fragment;

    private ToActivityListener callback;

    private TextView titleView;

    private CustomCursorAdapter adapter;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        setHasOptionsMenu(true);
        callback = (ToActivityListener) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.generic_list_layout, container, false);

        titleView = (TextView) v.findViewById(R.id.title);

        fillData(title, tableName);

        return v;
    }

    private void fillData(String title, String tableName) {

        String[] from;
        int[] to;

        titleView.setText(title);
        switch (tableName){
            case EventTypeTable.TABLE_NAME:
                from = new String[] {EventTypeTable.COLUMN_NAME, EventTypeTable.COLUMN_ICON};
                to = new int[] {R.id.primary_text, R.id.icon};
                getLoaderManager().initLoader(10, null, this);
                adapter = new CustomCursorAdapter(getContext(), R.layout.event_list_item_layout, null, from, to, 0, 10);
                setListAdapter(adapter);
                break;
            case EventTable.TABLE_NAME:
                from = new String[] {EventTable.COLUMN_NAME, EventTable.COLUMN_ICON};
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
            if (bundle.getString("type")!=null){
                gameType = bundle.getString("type");
            }
        }
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.clear();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        //Toast.makeText(getContext(),"Você clicou no item: " + id,Toast.LENGTH_SHORT).show();

        //Bundle bundle = new Bundle();
        //bundle.putLong("id",id);
        //String tag = "";
        switch (tableName){
            case EventTypeTable.TABLE_NAME:
                //bundle.putString("Table", EventTypeTable.TABLE_NAME);
                //bundle.putLong("id", id);
                //tag = "new";
                callback.onEventTypeSelected(String.valueOf(id));
                break;
            case EventTable.TABLE_NAME:
                //bundle.putString("Table", EventTable.TABLE_NAME);
                //bundle.putLong("id", id);
                //bundle.putString("Type", gameType);
                //tag = "new";
                callback.onEventGameSelected(String.valueOf(id));
                break;
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String[] projection;
        CursorLoader cursorLoader;
        String[] selectionArgs;

        switch (id){
            case 10:
                projection = new String[] {EventTypeTable.COLUMN_ID, EventTypeTable.COLUMN_NAME, EventTypeTable.COLUMN_ICON};
                cursorLoader = new CursorLoader(
                        getActivity(),
                        DataProvider.EVENT_TYPE_URI,
                        projection,
                        null,
                        null,
                        null);
                break;
            case 20:
                projection = new String[] {EventTable.COLUMN_ID, EventTable.COLUMN_NAME, EventTable.COLUMN_ICON, EventTable.COLUMN_TYPE};
                selectionArgs = new String[] {gameType};
                cursorLoader = new CursorLoader(getActivity(),
                        DataProvider.EVENT_URI,
                        projection,
                        EventTable.COLUMN_TYPE + "=?",
                        selectionArgs,
                        null);
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
            gameType = data.getString(data.getColumnIndexOrThrow(EventTable.COLUMN_TYPE));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }
}
