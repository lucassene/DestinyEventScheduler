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
import android.widget.Toast;

import com.destiny.event.scheduler.R;
import com.destiny.event.scheduler.activities.DrawerActivity;
import com.destiny.event.scheduler.adapters.DetailEventAdapter;
import com.destiny.event.scheduler.data.EntryTable;
import com.destiny.event.scheduler.data.EventTable;
import com.destiny.event.scheduler.data.EventTypeTable;
import com.destiny.event.scheduler.data.GameTable;
import com.destiny.event.scheduler.data.MemberTable;
import com.destiny.event.scheduler.data.NotificationTable;
import com.destiny.event.scheduler.dialogs.MyAlertDialog;
import com.destiny.event.scheduler.interfaces.FromDialogListener;
import com.destiny.event.scheduler.interfaces.ToActivityListener;
import com.destiny.event.scheduler.models.EntryModel;
import com.destiny.event.scheduler.models.GameModel;
import com.destiny.event.scheduler.provider.DataProvider;
import com.destiny.event.scheduler.services.ServerService;
import com.destiny.event.scheduler.utils.DateUtils;
import com.destiny.event.scheduler.utils.NetworkUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class DetailEventFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor>, FromDialogListener{

    private static final String TAG = "DetailEventFragment";

    private static final int LOADER_NOTIFICATION = 80;

    private static final int DELETE_NOTIFICATION = 0;
    private static final int CREATE_NOTIFICATION = 1;

    private int notificationMethod;

    private String origin;
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

    DetailEventAdapter detailAdapter;

    MyAlertDialog dialog;

    GameModel game;

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        setListAdapter(null);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
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
            //gameId = bundle.getString("gameId");
            origin = bundle.getString("origin");
            //creator = bundle.getString("creator");
            game = (GameModel) bundle.getSerializable("game");
        }

        if (game != null){
            switch (game.getStatus()){
                case GameTable.STATUS_NEW:
                    if (game.isJoined()){
                        if (game.getCreatorId().equals(callback.getBungieId())){
                            joinButton.setText(R.string.delete);
                        } else joinButton.setText(R.string.leave);
                    } else joinButton.setText(R.string.join);
                    break;
                case GameTable.STATUS_WAITING:
                    if (game.getCreatorId().equals(callback.getBungieId())){
                        joinButton.setText(R.string.validate);
                    } else {
                        joinButton.setText(R.string.waiting_validation);
                        joinButton.setEnabled(false);
                    }
                    break;
                case GameTable.STATUS_VALIDATED:
                    joinButton.setText(R.string.evaluate);
                    break;
            }
        }

        if (origin.equals(SearchFragment.TAG) || origin.equals(MyEventsFragment.TAG) || origin.equals(HistoryListFragment.TAG)){
            callback.setFragmentType(DrawerActivity.FRAGMENT_TYPE_WITH_BACKSTACK);
        } else callback.setFragmentType(DrawerActivity.FRAGMENT_TYPE_WITHOUT_BACKSTACK);

        joinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (NetworkUtils.checkConnection(getContext())){
                    switch (origin){
                        case NewEventsListFragment.TAG:
                            showAlertDialog(MyAlertDialog.JOIN_DIALOG);
                            break;
                        case ScheduledListFragment.TAG:
                            if (game.getCreatorId().equals(callback.getBungieId())){
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
                } else {
                    Toast.makeText(getContext(), getString(R.string.check_connection), Toast.LENGTH_SHORT).show();
                }

            }
        });

        bungieIdList = new ArrayList<>();

        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        if (headerView != null && footerView != null){
            this.getListView().addHeaderView(headerView, null, false);
            this.getListView().addFooterView(footerView);
        }

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
            case MyAlertDialog.ALERT_DIALOG:
                bundle.putString(title, getString(R.string.oops));
                bundle.putString(msg, getString(R.string.event_happened));
                bundle.putString(posButton, getString(R.string.got_it));
                break;
        }

        bundle.putInt("type", dialogType);

        dialog = new MyAlertDialog();
        dialog.setArguments(bundle);
        dialog.show(getFragmentManager(),"dialog");

    }

    private void initNotificationLoader(){
        if (getLoaderManager().getLoader(LOADER_NOTIFICATION) != null){
            getLoaderManager().destroyLoader(LOADER_NOTIFICATION);
        }
        getLoaderManager().restartLoader(LOADER_NOTIFICATION, null, this);
    }

    private void deleteGame(Uri uri) {
        getContext().getContentResolver().delete(uri,null,null);
        String selection = EntryTable.COLUMN_GAME + "=" + game.getGameId();
        getContext().getContentResolver().delete(DataProvider.ENTRY_URI, selection, null);

        Bundle bundle = new Bundle();
        bundle.putInt(ServerService.GAMEID_TAG, game.getGameId());
        bundle.putInt(ServerService.REQUEST_TAG, ServerService.TYPE_DELETE_GAME);
        callback.runServerService(bundle);

        notificationMethod = DELETE_NOTIFICATION;
        initNotificationLoader();
    }

    private void leaveGame(ContentValues values, Uri uri) {

        values.put(GameTable.COLUMN_INSCRIPTIONS, inscriptions-1);
        getContext().getContentResolver().update(uri, values,null, null);
        values.clear();
        String selection = EntryTable.COLUMN_GAME + "=" + game.getGameId() + " AND " + EntryTable.COLUMN_MEMBERSHIP + "=" + callback.getBungieId();
        getContext().getContentResolver().delete(DataProvider.ENTRY_URI, selection, null);

        Bundle bundle = new Bundle();
        bundle.putInt(ServerService.GAMEID_TAG, game.getGameId());
        bundle.putInt(ServerService.REQUEST_TAG, ServerService.TYPE_LEAVE_GAME);
        callback.runServerService(bundle);

        notificationMethod = DELETE_NOTIFICATION;
        initNotificationLoader();

    }

    private void joinGame(ContentValues values, Uri uri) {

        values.put(GameTable.COLUMN_INSCRIPTIONS, inscriptions+1);
        values.put(GameTable.COLUMN_STATUS, GameTable.STATUS_SCHEDULED);
        getContext().getContentResolver().update(uri, values,null, null);
        values.clear();

        values.put(EntryTable.COLUMN_GAME, game.getGameId());
        values.put(EntryTable.COLUMN_MEMBERSHIP, callback.getBungieId());
        values.put(EntryTable.COLUMN_TIME, DateUtils.getCurrentTime());
        getContext().getContentResolver().insert(DataProvider.ENTRY_URI, values);
        values.clear();

        Bundle bundle = new Bundle();
        bundle.putInt(ServerService.GAMEID_TAG, game.getGameId());
        bundle.putInt(ServerService.REQUEST_TAG, ServerService.TYPE_JOIN_GAME);
        callback.runServerService(bundle);

        notificationMethod = CREATE_NOTIFICATION;
        initNotificationLoader();

    }

    public void onRequestSuccess(){
        Log.w(TAG, "Request successful!");
        callback.closeFragment();
    }

    private void updateGame(ContentValues values, Uri uri) {

        values.put(GameTable.COLUMN_STATUS, GameTable.STATUS_WAITING);
        getContext().getContentResolver().update(uri, values, null, null);
        values.clear();

        notificationMethod = DELETE_NOTIFICATION;
        initNotificationLoader();

    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        String bungieId = bungieIdList.get(position-1);

        Fragment fragment = new MyNewProfileFragment();

        Bundle bundle = new Bundle();
        bundle.putString("bungieId", bungieId);
        bundle.putInt("type", MyNewProfileFragment.TYPE_DETAIL);

        callback.loadNewFragment(fragment, bundle, "profile");
    }

    private void getGameData() {

        eventIcon.setImageResource(getContext().getResources().getIdentifier(game.getEventIcon(),"drawable",getContext().getPackageName()));
        eventName.setText(getContext().getResources().getIdentifier(game.getEventName(),"string",getContext().getPackageName()));
        eventType.setText(getContext().getResources().getIdentifier(game.getTypeName(),"string",getContext().getPackageName()));

        String gameTime = game.getTime();
        eventCalendar = DateUtils.stringToDate(gameTime);
        date.setText(DateUtils.onBungieDate(gameTime));
        time.setText(DateUtils.getTime(gameTime));
        light.setText(String.valueOf(game.getMinLight()));

        maxGuardians = game.getMaxGuardians();
        inscriptions = game.getInscriptions();
        String sg = inscriptions + " " + getContext().getResources().getString(R.string.of) + " " + maxGuardians;
        guardians.setText(sg);
        int gS = game.getStatus();

        Calendar now = Calendar.getInstance();
        if (now.getTimeInMillis() > eventCalendar.getTimeInMillis() && gS == GameTable.STATUS_SCHEDULED){
            dialogThread();
        } else {
            getMembers(game.getGameId());
        }



        //prepareGameStrings();
        //initGameLoader();
    }

    private void getMembers(int gameId) {
        callback.getGameEntries(gameId);
    }

    public void onEntriesLoaded(List<EntryModel> entryList){
        Log.w(TAG, "entryList size: " + entryList.size());
        setAdapter(entryList, maxGuardians);
    }

    private void prepareGameStrings() {

        String c1 = GameTable.getQualifiedColumn(GameTable.COLUMN_ID);
        String c2 = GameTable.COLUMN_EVENT_ID;
        String c6 = GameTable.COLUMN_CREATOR;
        String c9 = GameTable.COLUMN_TIME;
        String c10 = GameTable.COLUMN_LIGHT;
        String c12 = GameTable.COLUMN_INSCRIPTIONS;
        String c14 = GameTable.COLUMN_CREATOR_NAME;
        String c3 = GameTable.COLUMN_STATUS;

        String c4 = EventTable.COLUMN_ICON;
        String c5 = EventTable.COLUMN_NAME;
        String c11 = EventTable.COLUMN_GUARDIANS;
        String c15 = EventTable.COLUMN_TYPE;

        String c7 = MemberTable.COLUMN_MEMBERSHIP;
        String c8 = MemberTable.COLUMN_NAME;

        String c16 = EventTypeTable.COLUMN_ICON;
        String c17 = EventTypeTable.COLUMN_NAME;

        gameProjection = new String[] {c1, c2, c3, c4, c5, c6, c7, c8, c9, c10, c11, c12, c14, c15, c16, c17};

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
        callback = (ToActivityListener) getActivity();
        callback.setFragmentType(DrawerActivity.FRAGMENT_TYPE_WITHOUT_BACKSTACK);
        setHasOptionsMenu(true);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.clear();
        callback.setToolbarTitle(getString(R.string.event_details));
        getActivity().getMenuInflater().inflate(R.menu.empty_menu, menu);
    }

    @Override
    public void onDetach() {
        super.onDetach();

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        callback.onLoadingData();

        String[] selectionArgs = {String.valueOf(game.getGameId())};

        switch (id){
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

        if (data != null && data.moveToFirst() && loader.getId() == LOADER_NOTIFICATION){

            Log.w(TAG, "onLoaderFinished alcançado com ID LOADER_NOTIFICATION");

            switch (notificationMethod){
                case CREATE_NOTIFICATION:
                    if (data == null || data.getCount()<=0){
                        setAlarmNotification(getNotifyTime(), String.valueOf(game.getGameId()), gameEventName, gameEventTypeName, gameEventIcon);
                    } else{
                        Log.w(TAG, "Notification for this game already created!");
                    }
                    break;
                case DELETE_NOTIFICATION:
                    if (data!= null && data.getCount()>0){
                        for (int i=0;i<data.getCount();i++){
                            String notificationId = data.getString(data.getColumnIndexOrThrow(NotificationTable.COLUMN_ID));
                            Uri uri = Uri.parse(DataProvider.NOTIFICATION_URI + "/" + notificationId);
                            int deletedRow = getContext().getContentResolver().delete(uri, null, null);
                            Log.w(TAG, "NotificationID deleted: " + String.valueOf(deletedRow));
                            int requestId = Integer.parseInt(notificationId);
                            callback.cancelAlarmTask(requestId);
                            data.moveToNext();
                        }
                    } else Log.w(TAG,"There is no Notification to be deleted!");
                    break;
            }
        }

        callback.onDataLoaded();

    }

    private void dialogThread() {
        Handler handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == MyAlertDialog.ALERT_DIALOG && isAdded()) showAlertDialog(MyAlertDialog.ALERT_DIALOG);
            }
        };
        handler.sendEmptyMessage(MyAlertDialog.ALERT_DIALOG);
    }

    private Calendar getNotifyTime() {
        Calendar notifyTime = Calendar.getInstance();
        notifyTime.setTime(eventCalendar.getTime());

        SharedPreferences sharedPrefs = getActivity().getSharedPreferences(DrawerActivity.SHARED_PREFS, Context.MODE_PRIVATE);
        int alarmTime = sharedPrefs.getInt(DrawerActivity.SCHEDULED_TIME_PREF, 0)*-1;
        notifyTime.add(Calendar.MINUTE,alarmTime);

        return notifyTime;
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    private void setAdapter(List<EntryModel> entries, int max) {
        setListAdapter(null);
        //adapter = new MembersAdapter(getContext(), R.layout.member_list_item_layout, null, from, to, 0, max);
        detailAdapter = new DetailEventAdapter(getContext(),entries, max);

/*        if (headerView != null && footerView != null){
            this.getListView().addHeaderView(headerView, null, false);
            this.getListView().addFooterView(footerView);
        }*/

        setListAdapter(detailAdapter);
    }

    private void setAlarmNotification(Calendar notifyTime, String gameId, String title, String typeName, int typeIcon) {

        int firstId = 0;
        int secondId = 0;

        if (notifyTime.getTimeInMillis() == eventCalendar.getTimeInMillis()){
            ContentValues values = new ContentValues();
            values.put(NotificationTable.COLUMN_GAME, gameId);
            values.put(NotificationTable.COLUMN_EVENT, title);
            values.put(NotificationTable.COLUMN_TYPE, typeName);
            values.put(NotificationTable.COLUMN_ICON, typeIcon);
            values.put(NotificationTable.COLUMN_TIME, DateUtils.calendarToString(notifyTime));

            Uri uri = getContext().getContentResolver().insert(DataProvider.NOTIFICATION_URI, values);
            if (uri != null) firstId = Integer.parseInt(uri.getLastPathSegment());
            values.clear();
        } else {
            Calendar now = Calendar.getInstance();
            now.set(Calendar.SECOND, 0);
            now.set(Calendar.MILLISECOND, 0);
            //Log.w(TAG, "Now: " + now.getTimeInMillis() + " / notifyTime: " + notifyTime.getTimeInMillis() );
            if (now.getTimeInMillis() < notifyTime.getTimeInMillis()){
                //Log.w(TAG, "Agora é antes do tempo de Notificação");
                ContentValues values = new ContentValues();
                values.put(NotificationTable.COLUMN_GAME, gameId);
                values.put(NotificationTable.COLUMN_EVENT, title);
                values.put(NotificationTable.COLUMN_TYPE, typeName);
                values.put(NotificationTable.COLUMN_ICON, typeIcon);
                values.put(NotificationTable.COLUMN_TIME, DateUtils.calendarToString(notifyTime));

                Uri uri = getContext().getContentResolver().insert(DataProvider.NOTIFICATION_URI, values);
                if (uri != null) firstId = Integer.parseInt(uri.getLastPathSegment());

                if (firstId != 0) {
                    values.clear();
                    values.put(NotificationTable.COLUMN_GAME, gameId);
                    values.put(NotificationTable.COLUMN_EVENT, title);
                    values.put(NotificationTable.COLUMN_TYPE, typeName);
                    values.put(NotificationTable.COLUMN_ICON, typeIcon);
                    values.put(NotificationTable.COLUMN_TIME, DateUtils.calendarToString(eventCalendar));

                    uri = getContext().getContentResolver().insert(DataProvider.NOTIFICATION_URI, values);
                    if (uri != null) secondId = Integer.parseInt(uri.getLastPathSegment());
                    values.clear();
                }
            } else {
                //Log.w(TAG, "Agora é depois do tempo de Notificação");
                ContentValues values = new ContentValues();
                values.put(NotificationTable.COLUMN_GAME, gameId);
                values.put(NotificationTable.COLUMN_EVENT, title);
                values.put(NotificationTable.COLUMN_TYPE, typeName);
                values.put(NotificationTable.COLUMN_ICON, typeIcon);
                values.put(NotificationTable.COLUMN_TIME, DateUtils.calendarToString(eventCalendar));

                Uri uri = getContext().getContentResolver().insert(DataProvider.NOTIFICATION_URI, values);
                if (uri != null) secondId = Integer.parseInt(uri.getLastPathSegment());
                values.clear();
            }
        }

        callback.registerAlarmTask(notifyTime, firstId, eventCalendar, secondId);

    }

    @Override
    public void onPositiveClick(String input, int type) {

        ContentValues values = new ContentValues();
        String uriString = DataProvider.GAME_URI + "/" + game.getGameId();
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
            case MyAlertDialog.ALERT_DIALOG:
                updateGame(values, uri);
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