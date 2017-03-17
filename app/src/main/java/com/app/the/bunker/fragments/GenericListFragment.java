package com.app.the.bunker.fragments;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
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
import com.app.the.bunker.interfaces.SwipeListener;
import com.app.the.bunker.interfaces.ToActivityListener;
import com.app.the.bunker.models.MemberModel;
import com.app.the.bunker.provider.DataProvider;
import com.app.the.bunker.utils.StringUtils;
import com.app.the.bunker.views.CustomSwipeLayout;

import java.util.ArrayList;
import java.util.List;

public class GenericListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor>, SwipeListener{

    @SuppressWarnings("unused")
    private static final String TAG = "GenericListFragment";

    private static final int LOADER_TYPE = 10;
    private static final int LOADER_EVENT = 20;
    private static final int LOADER_MEMBERS = 50;

    private String title;
    private String tableName;
    private int gameType;
    private int maxGuardians;

    private ToActivityListener callback;
    private TextView titleView;
    private MemberAdapter memberAdapter;
    private ArrayList<String> entryList;
    private ArrayList<String> membershipList;
    private CustomSwipeLayout swipeLayout;

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
        swipeLayout = (CustomSwipeLayout) v.findViewById(R.id.swipe_layout);
        return v;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle bundle = getArguments();
        if (bundle != null) {
            title = bundle.getString("title");
            tableName = bundle.getString("table");
            maxGuardians = bundle.getInt("max");
            entryList = (ArrayList<String>) bundle.getSerializable("list");
            if (bundle.containsKey("type") && bundle.getInt("type") != 0) {
                gameType = bundle.getInt("type");
            }
        }
        fillData(title, tableName);

        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                switch (tableName){
                    case MemberTable.TABLE_NAME:
                        callback.updateClan(membershipList);
                        break;
                    case EventTable.TABLE_NAME:
                    case EventTypeTable.TABLE_NAME:
                        callback.updateEvents();
                        break;
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        setListAdapter(null);
    }

    private void fillData(String title, String tableName) {
        titleView.setText(title);
        switch (tableName){
            case EventTypeTable.TABLE_NAME:
                initLoader(LOADER_TYPE);
                break;
            case EventTable.TABLE_NAME:
                initLoader(LOADER_EVENT);
                break;
            case MemberTable.TABLE_NAME:
                callback.setToolbarTitle(getString(R.string.no_member));
                initLoader(LOADER_MEMBERS);
                break;
        }
    }

    private void initLoader(int type){
        if (getLoaderManager().getLoader(type) != null){
            getLoaderManager().destroyLoader(type);
        }
        getLoaderManager().restartLoader(type, null, this);
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
            getActivity().getMenuInflater().inflate(R.menu.done_menu, menu);
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
                    if (maxGuardians > memberAdapter.getCheckedMemberCount()){
                        memberAdapter.toggleMemberCheck(position);
                        memberAdapter.notifyDataSetChanged();
                    } else if (memberAdapter.getItem(position).isChecked()){
                        memberAdapter.toggleMemberCheck(position);
                        memberAdapter.notifyDataSetChanged();
                    }
                    updateTitle();
                }
                break;
        }
    }

    private void updateTitle(){
        int count = memberAdapter.getCheckedMemberCount();
        if (count == 0){
            callback.setToolbarTitle(getString(R.string.no_member));
        } else if (count == 1){
            callback.setToolbarTitle(count + getString(R.string.member));
        } else callback.setToolbarTitle(count + getString(R.string.members_));
    }

    public List<String> getCheckedMembershipList(){
        if (memberAdapter != null){
            return memberAdapter.getCheckedMembershipList();
        }
        return null;
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
                        StringUtils.getLanguageString() + " ASC");
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
                        StringUtils.getLanguageString() + " ASC");
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
        String[] from;
        int[] to;
        CustomCursorAdapter adapter;
        if (data != null && data.moveToFirst()){
            switch (loader.getId()){
                case LOADER_TYPE:
                    from = new String[] {EventTypeTable.COLUMN_EN, EventTypeTable.COLUMN_PT, EventTypeTable.COLUMN_ES, EventTypeTable.COLUMN_ICON};
                    to = new int[] {R.id.primary_text, R.id.icon};
                    adapter = new CustomCursorAdapter(getContext(), R.layout.event_list_item_layout, null, from, to, 0, LOADER_TYPE);
                    adapter.swapCursor(data);
                    setListAdapter(adapter);
                    break;
                case LOADER_EVENT:
                    data.moveToFirst();
                    gameType = data.getInt(data.getColumnIndexOrThrow(EventTable.COLUMN_TYPE));
                    from = new String[] {EventTable.COLUMN_EN, EventTable.COLUMN_PT, EventTable.COLUMN_ES, EventTable.COLUMN_ICON};
                    to = new int[] {R.id.primary_text, R.id.icon};
                    adapter = new CustomCursorAdapter(getContext(), R.layout.event_list_item_layout, null, from, to, 0, LOADER_EVENT);
                    adapter.swapCursor(data);
                    setListAdapter(adapter);
                    break;
                case LOADER_MEMBERS:
                    data.moveToFirst();
                    List<MemberModel> memberList = new ArrayList<>();
                    membershipList = new ArrayList<>();
                    for (int i=0;i<data.getCount();i++){
                        MemberModel currentMember = new MemberModel();
                        currentMember.setMembershipId(data.getString(data.getColumnIndexOrThrow(MemberTable.COLUMN_MEMBERSHIP)));
                        membershipList.add(currentMember.getMembershipId());
                        if (!currentMember.getMembershipId().equals(callback.getBungieId())){
                            currentMember.setName(data.getString(data.getColumnIndexOrThrow(MemberTable.COLUMN_NAME)));
                            currentMember.setIconPath(data.getString(data.getColumnIndexOrThrow(MemberTable.COLUMN_ICON)));
                            currentMember.setTitle(data.getString(data.getColumnIndexOrThrow(MemberTable.COLUMN_TITLE)));
                            currentMember.setLikes(data.getInt(data.getColumnIndexOrThrow(MemberTable.COLUMN_LIKES)));
                            currentMember.setDislikes(data.getInt(data.getColumnIndexOrThrow(MemberTable.COLUMN_DISLIKES)));
                            currentMember.setGamesPlayed(data.getInt(data.getColumnIndexOrThrow(MemberTable.COLUMN_PLAYED)));
                            currentMember.setGamesCreated(data.getInt(data.getColumnIndexOrThrow(MemberTable.COLUMN_CREATED)));
                            currentMember.setChecked(false);
                            for (int x=0;x<entryList.size();x++){
                                if (currentMember.getMembershipId().equals(entryList.get(x))){
                                    currentMember.setChecked(true);
                                    break;
                                }
                            }
                            memberList.add(currentMember);
                        }
                        data.moveToNext();
                    }
                    if (memberAdapter == null){
                        memberAdapter = new MemberAdapter(getActivity(), memberList);
                        setListAdapter(memberAdapter);
                    } else { memberAdapter.setMemberList(memberList); }
                    updateTitle();
                    break;
            }
        }
        callback.onDataLoaded();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {    }

    @Override
    public void toggleSwipeProgress(boolean b) {
        if (swipeLayout != null) swipeLayout.setRefreshing(b);
        fillData(title, tableName);
    }
}
