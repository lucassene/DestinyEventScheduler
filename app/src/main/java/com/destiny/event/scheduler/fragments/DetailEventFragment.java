package com.destiny.event.scheduler.fragments;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
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
import com.destiny.event.scheduler.data.NotificationTable;
import com.destiny.event.scheduler.dialogs.MyAlertDialog;
import com.destiny.event.scheduler.interfaces.FromDialogListener;
import com.destiny.event.scheduler.interfaces.ToActivityListener;
import com.destiny.event.scheduler.interfaces.UserDataListener;
import com.destiny.event.scheduler.models.GameModel;
import com.destiny.event.scheduler.models.MemberModel;
import com.destiny.event.scheduler.provider.DataProvider;
import com.destiny.event.scheduler.services.ServerService;
import com.destiny.event.scheduler.utils.DateUtils;
import com.destiny.event.scheduler.utils.NetworkUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class DetailEventFragment extends ListFragment implements FromDialogListener, UserDataListener{

    public static final String TAG = "DetailEventFragment";

    private String origin;
    private int inscriptions;
    private int maxGuardians;
    private Calendar eventCalendar;

    private ArrayList<MemberModel> entryList;

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

    @SuppressWarnings("unchecked")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.detail_event_layout, container, false);

        callback.registerUserDataListener(this);

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
            Calendar now = Calendar.getInstance();
            if (DateUtils.stringToDate(game.getTime()).getTimeInMillis() > now.getTimeInMillis()){
                switch (game.getStatus()){
                    case GameModel.STATUS_NEW:
                        if (game.isJoined()){
                            if (game.getCreatorId().equals(callback.getBungieId())){
                                joinButton.setText(R.string.delete);
                            } else joinButton.setText(R.string.leave);
                        } else joinButton.setText(R.string.join);
                        break;
                    case GameModel.STATUS_WAITING:
                        if (game.getCreatorId().equals(callback.getBungieId())){
                            joinButton.setText(R.string.validate);
                        } else {
                            joinButton.setText(R.string.waiting_validation);
                            joinButton.setEnabled(false);
                        }
                        break;
                    case GameModel.STATUS_VALIDATED:
                        joinButton.setText(R.string.evaluate);
                        break;
                }
            } else {
                showAlertDialog(MyAlertDialog.ALERT_DIALOG);
            }
        }

        if (origin.equals(SearchFragment.TAG) || origin.equals(MyEventsFragment.TAG) || origin.equals(HistoryListFragment.TAG)){
            callback.setFragmentType(DrawerActivity.FRAGMENT_TYPE_WITH_BACKSTACK);
        } else callback.setFragmentType(DrawerActivity.FRAGMENT_TYPE_WITHOUT_BACKSTACK);

        joinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (NetworkUtils.checkConnection(getContext())){
                    Calendar now = Calendar.getInstance();
                    if (DateUtils.stringToDate(game.getTime()).getTimeInMillis() > now.getTimeInMillis()){
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
                        showAlertDialog(MyAlertDialog.ALERT_DIALOG);
                    }
                } else {
                    Toast.makeText(getContext(), getString(R.string.check_connection), Toast.LENGTH_SHORT).show();
                }

            }
        });

        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        entryList = new ArrayList<>();
        detailAdapter = new DetailEventAdapter(getContext(), entryList, game.getMaxGuardians());
        if (headerView != null){
            getListView().addHeaderView(headerView, null, false);
        }
        getListView().setAdapter(detailAdapter);
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

    private void deleteGame() {
        Bundle bundle = new Bundle();
        bundle.putInt(ServerService.GAMEID_TAG, game.getGameId());
        bundle.putInt(ServerService.REQUEST_TAG, ServerService.TYPE_DELETE_GAME);
        callback.runServerService(bundle);

        deleteNotifications(game.getGameId());
    }

    private void leaveGame() {
        Bundle bundle = new Bundle();
        bundle.putInt(ServerService.GAMEID_TAG, game.getGameId());
        bundle.putInt(ServerService.REQUEST_TAG, ServerService.TYPE_LEAVE_GAME);
        callback.runServerService(bundle);

        deleteNotifications(game.getGameId());
    }

    private void joinGame() {
        Bundle bundle = new Bundle();
        bundle.putInt(ServerService.GAMEID_TAG, game.getGameId());
        bundle.putInt(ServerService.REQUEST_TAG, ServerService.TYPE_JOIN_GAME);
        callback.runServerService(bundle);

        createNotifications(game);
    }

    private void updateGame() {
        callback.updateGameStatus(game, getStatus());
    }

    private void createNotifications(GameModel game) {
        int eventIcon = getResources().getIdentifier(game.getEventIcon(),"drawable",getContext().getPackageName());
        setAlarmNotification(getNotifyTime(), game.getTime(), game.getGameId(), game.getEventName(), game.getTypeName(), eventIcon);
    }

    private void deleteNotifications(int gameId) {
        getActivity().getContentResolver().delete(DataProvider.NOTIFICATION_URI,NotificationTable.COLUMN_GAME + "=" + gameId,null);
    }

    private int getStatus() {
        if (game.isJoined()) {
            return GameModel.STATUS_SCHEDULED;
        } else return GameModel.STATUS_NEW;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Fragment fragment = new MyNewProfileFragment();

        Bundle bundle = new Bundle();
        bundle.putString("bungieId", entryList.get(position-1).getMembershipId());
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
        if (now.getTimeInMillis() > eventCalendar.getTimeInMillis() && gS == GameModel.STATUS_SCHEDULED){
            dialogThread();
        } else {
            if (entryList.size()==0){
                Log.w(TAG, "entryList size = 0");
                getMembers(game.getGameId());
            } else {
                Log.w(TAG, "entryList size > 0");
                onEntriesLoaded(entryList, false);
            }
        }

    }

    private void getMembers(int gameId) {
        callback.getGameEntries(gameId);
    }

    @Override
    public void onEntriesLoaded(List<MemberModel> entryList, boolean isUpdateNeeded){
        if (entryList != null){
            Log.w(TAG, "entryList size: " + entryList.size());
            this.entryList = (ArrayList<MemberModel>) entryList;

            int status;
            if (game.isJoined()){
                status = GameModel.STATUS_SCHEDULED;
            } else status = GameModel.STATUS_NEW;

            if (isUpdateNeeded) { callback.updateGameEntries(status, game.getGameId(), entryList.size()); }
            String sg = entryList.size() + " " + getContext().getResources().getString(R.string.of) + " " + maxGuardians;
            guardians.setText(sg);
            setAdapter(this.entryList, maxGuardians);
            if (footerView != null){
                this.getListView().addFooterView(footerView);
            }
        } else {
            Log.w(TAG, "entryList is null");
        }
    }

    @Override
    public void onMemberLoaded(MemberModel member, boolean isUpdateNeeded) {

    }

    @Override
    public void onMembersUpdated() {
        if (detailAdapter != null){
            detailAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("entryList",entryList);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.w(TAG, "DetailEventFragment attached!");
        callback = (ToActivityListener) getActivity();
        callback.setFragmentType(DrawerActivity.FRAGMENT_TYPE_WITHOUT_BACKSTACK);
        callback.registerUserDataListener(this);
        setHasOptionsMenu(true);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.w(TAG, "DetailEventFragment destroyed!");
        callback.deleteUserDataListener(this);
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
        Log.w(TAG, "DetailEventFragment detached!");
        callback.deleteUserDataListener(this);
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

    private void setAdapter(List<MemberModel> entries, int max) {
        setListAdapter(null);
        detailAdapter = new DetailEventAdapter(getContext(),entries, max);
        setListAdapter(detailAdapter);
    }

    private void setAlarmNotification(Calendar notifyTime, String gameTime, int gameId, String title, String typeName, int typeIcon) {

        int firstId = 0;
        int secondId = 0;

        if (notifyTime.getTimeInMillis() == eventCalendar.getTimeInMillis()){
            ContentValues values = new ContentValues();
            values.put(NotificationTable.COLUMN_GAME, gameId);
            values.put(NotificationTable.COLUMN_EVENT, title);
            values.put(NotificationTable.COLUMN_TYPE, typeName);
            values.put(NotificationTable.COLUMN_ICON, typeIcon);
            values.put(NotificationTable.COLUMN_TIME, DateUtils.calendarToString(notifyTime));
            values.put(NotificationTable.COLUMN_GAME_TIME, gameTime);

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
                values.put(NotificationTable.COLUMN_GAME_TIME, gameTime);

                Uri uri = getContext().getContentResolver().insert(DataProvider.NOTIFICATION_URI, values);
                if (uri != null) firstId = Integer.parseInt(uri.getLastPathSegment());

                if (firstId != 0) {
                    values.clear();
                    values.put(NotificationTable.COLUMN_GAME, gameId);
                    values.put(NotificationTable.COLUMN_EVENT, title);
                    values.put(NotificationTable.COLUMN_TYPE, typeName);
                    values.put(NotificationTable.COLUMN_ICON, typeIcon);
                    values.put(NotificationTable.COLUMN_TIME, DateUtils.calendarToString(eventCalendar));
                    values.put(NotificationTable.COLUMN_GAME_TIME, gameTime);

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
                values.put(NotificationTable.COLUMN_GAME_TIME, gameTime);

                Uri uri = getContext().getContentResolver().insert(DataProvider.NOTIFICATION_URI, values);
                if (uri != null) secondId = Integer.parseInt(uri.getLastPathSegment());
                values.clear();
            }
        }

        callback.registerAlarmTask(notifyTime, firstId, eventCalendar, secondId);

    }

    @Override
    public void onPositiveClick(String input, int type) {
        switch (type){
            case MyAlertDialog.JOIN_DIALOG:
                joinGame();
                break;
            case MyAlertDialog.LEAVE_DIALOG:
                leaveGame();
                break;
            case MyAlertDialog.DELETE_DIALOG:
                deleteGame();
                break;
            case MyAlertDialog.ALERT_DIALOG:
                updateGame();
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
    public void onItemSelected(String type, String entry, int value) {

    }

    @Override
    public void onMultiItemSelected(boolean[] items) {

    }

    @Override
    public void onUserDataLoaded() {

    }

    @Override
    public void onGamesLoaded(List<GameModel> gameList) {

    }
}