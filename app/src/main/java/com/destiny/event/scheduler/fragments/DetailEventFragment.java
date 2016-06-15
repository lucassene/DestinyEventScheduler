package com.destiny.event.scheduler.fragments;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.destiny.event.scheduler.R;
import com.destiny.event.scheduler.activities.DrawerActivity;
import com.destiny.event.scheduler.adapters.MembersAdapter;
import com.destiny.event.scheduler.data.EntryTable;
import com.destiny.event.scheduler.data.EventTable;
import com.destiny.event.scheduler.data.EventTypeTable;
import com.destiny.event.scheduler.data.GameTable;
import com.destiny.event.scheduler.data.MemberTable;
import com.destiny.event.scheduler.data.NotificationTable;
import com.destiny.event.scheduler.dialogs.MyAlertDialog;
import com.destiny.event.scheduler.interfaces.FromDialogListener;
import com.destiny.event.scheduler.interfaces.ToActivityListener;
import com.destiny.event.scheduler.provider.DataProvider;
import com.destiny.event.scheduler.utils.DateUtils;

import java.util.ArrayList;
import java.util.Calendar;

public class DetailEventFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor>, FromDialogListener{

    private static final String TAG = "DetailEventFragment";

    private static final int LOADER_GAME = 60;
    private static final int LOADER_ENTRY_MEMBERS = 72;
    private static final int LOADER_NOTIFICATION = 80;

    private static final int DELETE_NOTIFICATION = 0;
    private static final int CREATE_NOTIFICATION = 1;

    private int notificationMethod;

    private String gameId;
    private String origin;
    private String gameStatus;
    private String creator;
    private int inscriptions;
    private int maxGuardians;
    private String gameEventName;
    private String gameEventTypeName;
    private int gameEventIcon;
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

    View footerView;
    Button joinButton;

    private ToActivityListener callback;

    private String[] gameProjection;
    private String[] membersProjection;

    private static final String[] from = {MemberTable.COLUMN_NAME, MemberTable.COLUMN_ICON, MemberTable.COLUMN_EXP, MemberTable.COLUMN_TITLE};
    private static final int[] to = {R.id.primary_text, R.id.profile_pic, R.id.text_points, R.id.secondary_text};

    MembersAdapter adapter;

    MyAlertDialog dialog;

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        setListAdapter(null);
    }

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

        setListAdapter(null);

        Bundle bundle = getArguments();
        if (bundle != null){
            gameId = bundle.getString("gameId");
            origin = bundle.getString("origin");
            creator = bundle.getString("creator");
            gameStatus = bundle.getString("status");
        }

         if (gameStatus != null) {
             switch (gameStatus){
                 case GameTable.STATUS_NEW:
                     joinButton.setText(R.string.join);
                     break;
                 case GameTable.STATUS_SCHEDULED:
                     if (creator.equals(callback.getBungieId())){
                         joinButton.setText(R.string.delete);
                     } else joinButton.setText(getContext().getResources().getString(R.string.leave));
                     break;
                 case GameTable.STATUS_WAITING:
                     if (creator.equals(callback.getBungieId())){
                         joinButton.setText(R.string.validate);
                     } else {
                         joinButton.setText(R.string.waiting_validation);
                         joinButton.setEnabled(false);
                     }
                     break;
                 case GameTable.STATUS_VALIDATED:
                     joinButton.setText(R.string.evaluate);
                     break;
             };
         }


        joinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                switch (origin){
                    case NewEventsListFragment.TAG:
                        showAlertDialog(MyAlertDialog.JOIN_DIALOG);
                        break;
                    case ScheduledListFragment.TAG:
                        if (creator.equals(callback.getBungieId())){
                            showAlertDialog(MyAlertDialog.DELETE_DIALOG);
                        } else showAlertDialog(MyAlertDialog.LEAVE_DIALOG);
                        break;
                    case SearchFragment.TAG:
                        showAlertDialog(MyAlertDialog.JOIN_DIALOG);
                        break;
                    case MyEventsFragment.TAG:
                        showAlertDialog(MyAlertDialog.LEAVE_DIALOG);
                        break;
                }
            }
        });

        bungieIdList = new ArrayList<>();

        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        getGameData();

    }

    private void showAlertDialog(int dialogType) {

        final String title = "title";
        final String msg = "msg";
        final String posButton = "posButton";

        Bundle bundle = new Bundle();
        switch (dialogType){
            case MyAlertDialog.LEAVE_DIALOG:
                bundle.putString(title, getContext().getResources().getString(R.string.leave_game_title));
                bundle.putString(msg, getContext().getResources().getString(R.string.leave_dialog_msg));
                bundle.putString(posButton, getContext().getResources().getString(R.string.leave));
                break;
            case MyAlertDialog.DELETE_DIALOG:
                bundle.putString(title, getContext().getResources().getString(R.string.delete_dialog_title));
                bundle.putString(msg, getContext().getResources().getString(R.string.delete_dialog_msg));
                bundle.putString(posButton, getContext().getResources().getString(R.string.delete));
                break;
            case MyAlertDialog.JOIN_DIALOG:
                bundle.putString(title, getContext().getResources().getString(R.string.join_dialog_title));
                bundle.putString(posButton, getContext().getResources().getString(R.string.join));
                if (inscriptions > maxGuardians){
                    bundle.putString(msg, getContext().getResources().getString(R.string.join_full_dialog_msg));
                } else bundle.putString(msg, getContext().getResources().getString(R.string.join_dialog_msg));
                break;
        }

        bundle.putInt("type", dialogType);

        dialog = new MyAlertDialog();
        dialog.setArguments(bundle);
        dialog.show(getFragmentManager(),"dialog");

    }

    private void deleteGame(Uri uri) {
        getContext().getContentResolver().delete(uri,null,null);
        String selection = EntryTable.COLUMN_GAME + "=" + gameId;
        getContext().getContentResolver().delete(DataProvider.ENTRY_URI, selection, null);
        notificationMethod = DELETE_NOTIFICATION;
        getLoaderManager().initLoader(LOADER_NOTIFICATION, null, this);
    }

    private void leaveGame(ContentValues values, Uri uri) {

        values.put(GameTable.COLUMN_INSCRIPTIONS, inscriptions-1);
        getContext().getContentResolver().update(uri, values,null, null);
        values.clear();
        String selection = EntryTable.COLUMN_GAME + "=" + gameId + " AND " + EntryTable.COLUMN_MEMBERSHIP + "=" + callback.getBungieId();
        getContext().getContentResolver().delete(DataProvider.ENTRY_URI, selection, null);
        notificationMethod = DELETE_NOTIFICATION;
        getLoaderManager().initLoader(LOADER_NOTIFICATION, null, this);

    }

    private void joinGame(ContentValues values, Uri uri) {

        values.put(GameTable.COLUMN_INSCRIPTIONS, inscriptions+1);
        values.put(GameTable.COLUMN_STATUS, GameTable.STATUS_SCHEDULED);
        getContext().getContentResolver().update(uri, values,null, null);
        values.clear();

        values.put(EntryTable.COLUMN_GAME,gameId);
        values.put(EntryTable.COLUMN_MEMBERSHIP, callback.getBungieId());
        values.put(EntryTable.COLUMN_TIME, DateUtils.getCurrentTime());
        getContext().getContentResolver().insert(DataProvider.ENTRY_URI, values);
        values.clear();

        notificationMethod = CREATE_NOTIFICATION;
        getLoaderManager().initLoader(LOADER_NOTIFICATION, null, this);

    }


    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        String bungieId = bungieIdList.get(position-1);

        Fragment fragment = new MyNewProfileFragment();

        Bundle bundle = new Bundle();
        bundle.putString("bungieId", bungieId);

        callback.loadNewFragment(fragment, bundle, "profile");
    }

    private void getGameData() {

        callback.onLoadingData();
        prepareGameStrings();
        getLoaderManager().initLoader(LOADER_GAME, null, this);
    }

    private void prepareGameStrings() {

        String c1 = GameTable.getQualifiedColumn(GameTable.COLUMN_ID);
        String c2 = GameTable.COLUMN_EVENT_ID;
        String c6 = GameTable.COLUMN_CREATOR;
        String c9 = GameTable.COLUMN_TIME;
        String c10 = GameTable.COLUMN_LIGHT;
        String c12 = GameTable.COLUMN_INSCRIPTIONS;
        String c14 = GameTable.COLUMN_CREATOR_NAME;

        String c4 = EventTable.COLUMN_ICON;
        String c5 = EventTable.COLUMN_NAME;
        String c11 = EventTable.COLUMN_GUARDIANS;
        String c15 = EventTable.COLUMN_TYPE;

        String c7 = MemberTable.COLUMN_MEMBERSHIP;
        String c8 = MemberTable.COLUMN_NAME;

        String c16 = EventTypeTable.COLUMN_ICON;
        String c17 = EventTypeTable.COLUMN_NAME;

        gameProjection = new String[] {c1, c2, c4, c5, c6, c7, c8, c9, c10, c11, c12,  c14, c15, c16, c17};

    }

    private void prepareMemberStrings() {

        String c1 = EntryTable.getQualifiedColumn(EntryTable.COLUMN_ID);
        String c2 = EntryTable.COLUMN_GAME;
        String c3 = EntryTable.COLUMN_MEMBERSHIP;
        String c4 = EntryTable.COLUMN_TIME;

        String c5 = MemberTable.COLUMN_MEMBERSHIP;
        String c6 = MemberTable.COLUMN_NAME;
        String c7 = MemberTable.COLUMN_ICON;
        String c8 = MemberTable.COLUMN_LIKES;
        String c9 = MemberTable.COLUMN_DISLIKES;
        String c10 = MemberTable.COLUMN_CREATED;
        String c11 = MemberTable.COLUMN_PLAYED;

        String c14 = MemberTable.COLUMN_EXP;
        String c15 = MemberTable.COLUMN_TITLE;

        membersProjection = new String[] {c1, c2, c3, c4, c5, c6, c14, c7, c8, c9, c10, c11, c15};

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
            case LOADER_GAME:
                return new CursorLoader(
                        getContext(),
                        DataProvider.GAME_URI,
                        gameProjection,
                        GameTable.getQualifiedColumn(GameTable.COLUMN_ID)+ "=?",
                        selectionArgs,
                        null
                );
            case LOADER_ENTRY_MEMBERS:
                return new CursorLoader(
                        getContext(),
                        DataProvider.ENTRY_MEMBERS_URI,
                        membersProjection,
                        EntryTable.getQualifiedColumn(EntryTable.COLUMN_GAME) + "=?",
                        selectionArgs,
                        "datetime(" + EntryTable.getQualifiedColumn(EntryTable.COLUMN_TIME) + ") ASC"
                );
            case LOADER_NOTIFICATION:
                //Log.w(TAG,"onCreateLoader de ID LOADER_NOTIFICATION criado");
                return new CursorLoader(
                        getContext(),
                        DataProvider.NOTIFICATION_URI,
                        NotificationTable.ALL_COLUMNS,
                        NotificationTable.COLUMN_GAME + "=?",
                        selectionArgs,
                        null
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
                    eventIcon.setImageResource(getContext().getResources().getIdentifier(data.getString(data.getColumnIndexOrThrow(EventTable.COLUMN_ICON)), "drawable", getContext().getPackageName() ));

                    gameEventName = getContext().getResources().getString(getContext().getResources().getIdentifier(data.getString(data.getColumnIndexOrThrow(EventTable.COLUMN_NAME)), "string", getContext().getPackageName()));
                    eventName.setText(gameEventName);

                    gameEventTypeName = getContext().getResources().getString(getContext().getResources().getIdentifier(data.getString(data.getColumnIndexOrThrow(EventTypeTable.COLUMN_NAME)), "string", getContext().getPackageName()));
                    gameEventIcon = getContext().getResources().getIdentifier(data.getString(data.getColumnIndexOrThrow(EventTable.COLUMN_ICON)),"drawable",getContext().getPackageName());
                    eventType.setText(gameEventTypeName);

                    String gameTime = data.getString(data.getColumnIndexOrThrow(GameTable.COLUMN_TIME));
                    eventCalendar = DateUtils.stringToDate(gameTime);
                    Log.w(TAG, "Game Calendar: " + eventCalendar.getTime());
                    date.setText(DateUtils.onBungieDate(data.getString(data.getColumnIndexOrThrow(GameTable.COLUMN_TIME))));
                    time.setText(DateUtils.getTime(data.getString(data.getColumnIndexOrThrow(GameTable.COLUMN_TIME))));
                    light.setText(data.getString(data.getColumnIndexOrThrow(GameTable.COLUMN_LIGHT)));

                    maxGuardians = data.getInt(data.getColumnIndexOrThrow(EventTable.COLUMN_GUARDIANS));
                    inscriptions = data.getInt(data.getColumnIndexOrThrow(GameTable.COLUMN_INSCRIPTIONS));
                    String sg = inscriptions + " " + getContext().getResources().getString(R.string.of) + " " + maxGuardians;
                    guardians.setText(sg);
                    //Log.w(TAG, "Game Cursor: " + DatabaseUtils.dumpCursorToString(data));
                    setAdapter(maxGuardians);
                    prepareMemberStrings();
                    getLoaderManager().initLoader(LOADER_ENTRY_MEMBERS, null, this);
                    break;
                case LOADER_ENTRY_MEMBERS:
                    adapter.swapCursor(data);

                    data.moveToFirst();
                    for (int i=0; i < data.getCount();i++){
                        bungieIdList.add(i, data.getString(data.getColumnIndexOrThrow(EntryTable.COLUMN_MEMBERSHIP)));
                        data.moveToNext();
                    }
                    //Log.w(TAG, "Entry Cursor: " + DatabaseUtils.dumpCursorToString(data));
                    break;
            }
        }

        if (loader.getId() == LOADER_NOTIFICATION){

            //Log.w(TAG, "onLoaderFinished alcançado com ID LOADER_NOTIFICATION");

            switch (notificationMethod){
                case CREATE_NOTIFICATION:
                    if (data == null || data.getCount()<=0){
                        setAlarmNotification(getNotifyTime(), gameId, gameEventName, gameEventTypeName, gameEventIcon);
                    } else{
                        Log.w(TAG, "Notification for this game already created!");
                    }
                    break;
                case DELETE_NOTIFICATION:
                    if (data!= null && data.getCount()>0){
                        String notificationId = data.getString(data.getColumnIndexOrThrow(NotificationTable.COLUMN_ID));
                        String notificationTime = data.getString(data.getColumnIndexOrThrow(NotificationTable.COLUMN_TIME));
                        Uri uri = Uri.parse(DataProvider.NOTIFICATION_URI + "/" + notificationId);
                        int deletedRow = getContext().getContentResolver().delete(uri, null, null);
                        Log.w(TAG, "NotificationID deleted: " + String.valueOf(deletedRow));
                        int requestId = Integer.parseInt(notificationId);
                        callback.cancelAlarmTask(requestId);
                    } else Log.w(TAG,"There is no Notification to be deleted!");
                    break;
            }

        }

        callback.onDataLoaded();
        checkIfCloses(loader.getId());

    }

    private Calendar getNotifyTime() {
        Calendar notifyTime = Calendar.getInstance();
        notifyTime.setTime(eventCalendar.getTime());

        SharedPreferences sharedPrefs = getActivity().getSharedPreferences(DrawerActivity.SHARED_PREFS, Context.MODE_PRIVATE);
        int alarmTime = sharedPrefs.getInt(DrawerActivity.SCHEDULED_TIME_PREF, 10)*-1;
        notifyTime.add(Calendar.MINUTE,alarmTime);

        return notifyTime;
    }

    private void checkIfCloses(int loaderId) {
        if (loaderId == LOADER_NOTIFICATION){
            Handler handler = new Handler(){
                @Override
                public void handleMessage(Message msg){
                    if (msg.what == LOADER_NOTIFICATION) callback.closeFragment();
                }
            };
            handler.sendEmptyMessage(LOADER_NOTIFICATION);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        switch (loader.getId()){
            case LOADER_ENTRY_MEMBERS:
                adapter.swapCursor(null);
                break;
        }

    }

    private void setAdapter(int max) {
        setListAdapter(null);
        adapter = new MembersAdapter(getContext(), R.layout.member_list_item_layout, null, from, to, 0, max);

        if (headerView != null && footerView != null){
            this.getListView().addHeaderView(headerView, null, false);
            this.getListView().addFooterView(footerView);
        }

        setListAdapter(adapter);
    }

    private void setAlarmNotification(Calendar notifyTime, String gameId, String title, String typeName, int typeIcon) {

        ContentValues values = new ContentValues();
        values.put(NotificationTable.COLUMN_GAME, gameId);
        values.put(NotificationTable.COLUMN_EVENT, title);
        values.put(NotificationTable.COLUMN_TYPE, typeName);
        values.put(NotificationTable.COLUMN_ICON, typeIcon);
        values.put(NotificationTable.COLUMN_TIME, notifyTime.getTimeInMillis());

        Uri uri = getContext().getContentResolver().insert(DataProvider.NOTIFICATION_URI, values);

        if (uri == null){
            Log.w(TAG, "Notificação não foi criada");
        } else {
            int id = Integer.parseInt(uri.getLastPathSegment());
            values.clear();
            callback.registerAlarmTask(notifyTime, id);
        }

    }

    @Override
    public void onPositiveClick(String input, int type) {

        ContentValues values = new ContentValues();
        String uriString = DataProvider.GAME_URI + "/" + gameId;
        Uri uri = Uri.parse(uriString);

        switch (type){
            case MyAlertDialog.JOIN_DIALOG:
                joinGame(values, uri);
                break;
            case MyAlertDialog.LEAVE_DIALOG:
                leaveGame(values, uri);
                break;
            case MyAlertDialog.DELETE_DIALOG:
                deleteGame(uri);
                break;
        }

    }

    @Override
    public void onDateSent(Calendar date) {

    }

    @Override
    public void onTimeSent(int hour, int minute) {

    }

    @Override
    public void onLogoff() {

    }

    @Override
    public void onItemSelected(String entry, int value) {

    }

    @Override
    public void onMultiItemSelected(boolean[] items) {

    }
}