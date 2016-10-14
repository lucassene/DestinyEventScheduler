package com.app.the.bunker.fragments;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
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

import com.app.the.bunker.R;
import com.app.the.bunker.activities.DrawerActivity;
import com.app.the.bunker.adapters.CustomCursorAdapter;
import com.app.the.bunker.adapters.MemberAdapter;
import com.app.the.bunker.data.EventTable;
import com.app.the.bunker.data.EventTypeTable;
import com.app.the.bunker.data.MemberTable;
import com.app.the.bunker.interfaces.ToActivityListener;
import com.app.the.bunker.models.MemberModel;
import com.app.the.bunker.provider.DataProvider;

import java.util.ArrayList;
import java.util.List;

public class GenericListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final String TAG = "GenericListFragment";

    private static final int LOADER_TYPE = 10;
    private static final int LOADER_EVENT = 20;
    private static final int LOADER_MEMBERS = 50;

    private String title;
    private String tableName;
    private ListView listView;
    private int gameType;
    private int count;

    private ToActivityListener callback;
    private TextView titleView;
    private CustomCursorAdapter adapter;
    private MemberAdapter memberAdapter;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        setHasOptionsMenu(true);
        callback = (ToActivityListener) getActivity();
        callback.setFragmentType(DrawerActivity.FRAGMENT_TYPE_WITH_BACKSTACK);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        registerForContextMenu(getListView());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.generic_list_layout, container, false);
        titleView = (TextView) v.findViewById(R.id.title);
        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle bundle = getArguments();
        if (bundle != null) {
            title = bundle.getString("title");
            tableName = bundle.getString("table");
            if (bundle.containsKey("type") && bundle.getInt("type") != 0) {
                gameType = bundle.getInt("type");
            }
        }
        fillData(title, tableName);
        listView = getListView();
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
                getLoaderManager().initLoader(LOADER_TYPE, null, this);
                adapter = new CustomCursorAdapter(getContext(), R.layout.event_list_item_layout, null, from, to, 0, LOADER_TYPE);
                setListAdapter(adapter);
                break;
            case EventTable.TABLE_NAME:
                from = new String[] {EventTable.COLUMN_EN, EventTable.COLUMN_PT, EventTable.COLUMN_ES, EventTable.COLUMN_ICON};
                to = new int[] {R.id.primary_text, R.id.icon};
                getLoaderManager().initLoader(LOADER_EVENT, null, this);
                adapter = new CustomCursorAdapter(getContext(), R.layout.event_list_item_layout, null, from, to, 0, LOADER_EVENT);
                setListAdapter(adapter);
                break;
            case MemberTable.TABLE_NAME:
                callback.setToolbarTitle("NO MEMBER");
                getLoaderManager().initLoader(LOADER_MEMBERS, null, this);
                break;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.clear();
        if (tableName.equals(MemberTable.TABLE_NAME)){
            getActivity().getMenuInflater().inflate(R.menu.add_menu, menu);
        } else getActivity().getMenuInflater().inflate(R.menu.empty_menu, menu);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {

        switch (tableName){
            case EventTypeTable.TABLE_NAME:
                callback.onEventTypeSelected((int)id);
                break;
            case EventTable.TABLE_NAME:
                callback.onEventGameSelected((int)id);
                break;
            case MemberTable.TABLE_NAME:
                if(memberAdapter != null){
                    memberAdapter.toggleMemberCheck(position);
                    memberAdapter.notifyDataSetChanged();
                    int count = memberAdapter.getCheckedMemberCount();
                    if (count == 0){
                        callback.setToolbarTitle("NO MEMBER");
                    } else if (count == 1){
                        callback.setToolbarTitle(count + " MEMBER");
                    } else callback.setToolbarTitle(count + " MEMBERS");
                }
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
            case LOADER_TYPE:
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
            case LOADER_EVENT:
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
            case LOADER_MEMBERS:
                cursorLoader = new CursorLoader(getActivity(),
                        DataProvider.MEMBER_URI,
                        MemberTable.ALL_COLUMNS,
                        null,
                        null,
                        MemberTable.COLUMN_NAME + " COLLATE NOCASE ASC");
                break;
            default:
                cursorLoader = null;
        }

        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (adapter != null) { adapter.swapCursor(data); }
        if (data != null && data.moveToFirst()){
            switch (loader.getId()){
                case LOADER_EVENT:
                    data.moveToFirst();
                    gameType = data.getInt(data.getColumnIndexOrThrow(EventTable.COLUMN_TYPE));
                    break;
                case LOADER_MEMBERS:
                    data.moveToFirst();
                    List<MemberModel> memberList = new ArrayList<>();
                    for (int i=0;i<data.getCount();i++){
                        MemberModel currentMember = new MemberModel();
                        currentMember.setMembershipId(data.getString(data.getColumnIndexOrThrow(MemberTable.COLUMN_MEMBERSHIP)));
                        if (!currentMember.getMembershipId().equals(callback.getBungieId())){
                            currentMember.setName(data.getString(data.getColumnIndexOrThrow(MemberTable.COLUMN_NAME)));
                            currentMember.setIconPath(data.getString(data.getColumnIndexOrThrow(MemberTable.COLUMN_ICON)));
                            currentMember.setTitle(data.getString(data.getColumnIndexOrThrow(MemberTable.COLUMN_TITLE)));
                            currentMember.setLikes(data.getInt(data.getColumnIndexOrThrow(MemberTable.COLUMN_LIKES)));
                            currentMember.setDislikes(data.getInt(data.getColumnIndexOrThrow(MemberTable.COLUMN_DISLIKES)));
                            currentMember.setGamesPlayed(data.getInt(data.getColumnIndexOrThrow(MemberTable.COLUMN_PLAYED)));
                            currentMember.setGamesCreated(data.getInt(data.getColumnIndexOrThrow(MemberTable.COLUMN_CREATED)));
                            currentMember.setChecked(false);
                            memberList.add(currentMember);
                        }
                        data.moveToNext();
                    }
                    if (memberAdapter == null){
                        memberAdapter = new MemberAdapter(getActivity(), memberList);
                        setListAdapter(memberAdapter);
                    } else memberAdapter.setMemberList(memberList);
                    break;
            }
        }
        callback.onDataLoaded();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

}
