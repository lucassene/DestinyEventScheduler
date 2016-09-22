package com.destiny.event.scheduler.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.destiny.event.scheduler.R;
import com.destiny.event.scheduler.activities.DrawerActivity;
import com.destiny.event.scheduler.data.EventTable;
import com.destiny.event.scheduler.data.EventTypeTable;
import com.destiny.event.scheduler.dialogs.MyDatePickerDialog;
import com.destiny.event.scheduler.dialogs.MyTimePickerDialog;
import com.destiny.event.scheduler.dialogs.SimpleInputDialog;
import com.destiny.event.scheduler.interfaces.FromActivityListener;
import com.destiny.event.scheduler.interfaces.FromDialogListener;
import com.destiny.event.scheduler.interfaces.ToActivityListener;
import com.destiny.event.scheduler.models.GameModel;
import com.destiny.event.scheduler.provider.DataProvider;
import com.destiny.event.scheduler.services.ServerService;
import com.destiny.event.scheduler.utils.NetworkUtils;
import com.destiny.event.scheduler.utils.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class NewEventFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, FromDialogListener, FromActivityListener {

    private static final String TAG = "NewEventFragment";

    private static final int LOADER_TYPE = 10;
    private static final int LOADER_EVENT = 20;
    private static final int LOADER_EVENT_WITH_TYPE = 99;

    private static final int MAX_LIGHT = 400;

    private int selectedType;
    private int selectedEvent;

    private GameModel game;

    private ToActivityListener callback;

    private ImageView iconType;
    private TextView textType;
    private ImageView iconGame;
    private TextView textGame;
    private TextView lightText;
    private SeekBar lightBar;
    private int minLight;
    private EditText dateText;
    private EditText timeText;
    private Button createButton;
    private EditText commentText;
    private TextView chartCount;
    private int chars = 60;

    private SimpleInputDialog dialog;

    private Calendar insertedDate;
    private int minimumIntTime;

    private boolean hasDate = false;
    private boolean hasTime = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        game = new GameModel();

        if (savedInstanceState != null){
            selectedEvent = savedInstanceState.getInt("id");
            selectedType = savedInstanceState.getInt("Type");
        }

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("id", selectedEvent);
        outState.putInt("Type", selectedType);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        callback.setFragmentType(DrawerActivity.FRAGMENT_TYPE_WITHOUT_BACKSTACK);
        if(!dateText.getText().toString().equals("")){
            hasDate = true;
        }
        if (!timeText.getText().toString().equals("")){
            hasTime = true;
        }
        checkIfIsOk();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        setHasOptionsMenu(true);
        callback = (ToActivityListener) getActivity();
        callback.setFragmentType(DrawerActivity.FRAGMENT_TYPE_WITHOUT_BACKSTACK);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.new_event_layout, container, false);

        iconType = (ImageView) v.findViewById(R.id.type_icon);
        textType = (TextView) v.findViewById(R.id.type_text);
        RelativeLayout typeLayout = (RelativeLayout) v.findViewById(R.id.type_list);

        iconGame = (ImageView) v.findViewById(R.id.game_icon);
        textGame = (TextView) v.findViewById(R.id.game_text);
        RelativeLayout gameLayout = (RelativeLayout) v.findViewById(R.id.game_list);

        lightText = (TextView) v.findViewById(R.id.light_min_text);
        lightBar = (SeekBar) v.findViewById(R.id.light_bar);

        dateText = (EditText) v.findViewById(R.id.date_text);
        timeText = (EditText) v.findViewById(R.id.time_text);

        commentText = (EditText) v.findViewById(R.id.comment_text);
        commentText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                chars =  60 - commentText.getText().length();
                chartCount.setText(String.valueOf(chars));
                if (commentText.getText().length() > 60){
                    createButton.setEnabled(false);
                } else {
                    if (hasDate && hasTime){
                        createButton.setEnabled(true);
                    }
                }
            }
        });
        chartCount = (TextView) v.findViewById(R.id.char_count);

        createButton = (Button) v.findViewById(R.id.btn_gravar);
        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (NetworkUtils.checkConnection(getContext())){
                    createNewEvent();
                } else {
                    Toast.makeText(getContext(), getString(R.string.check_connection), Toast.LENGTH_SHORT).show();
                }
            }
        });


        lightBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                lightText.setText(String.valueOf(lightBar.getProgress() + minLight));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        typeLayout.setOnClickListener(new View.OnClickListener() {
            Fragment fragment = new GenericListFragment();

            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString("title", getContext().getResources().getString(R.string.choose_type));
                bundle.putInt("selected", selectedType);
                bundle.putString("table", EventTypeTable.TABLE_NAME);
                bundle.putString("tag",getTag());
                callback.loadNewFragment(fragment, bundle, "type");
            }
        });

        gameLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callEventList();
            }
        });

        lightText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString("tag","new");
                bundle.putString("title", getResources().getString(R.string.min_light_label));
                bundle.putString("yes", getResources().getString(R.string.save));
                bundle.putString("no", getResources().getString(R.string.cancel));
                bundle.putInt("max", MAX_LIGHT);
                bundle.putInt("min", minLight);
                bundle.putInt("type", FromDialogListener.LIGHT_TYPE);
                String hint = getResources().getString(R.string.value_between2) + minLight + getResources().getString(R.string.and320);
                bundle.putString("hint", hint);
                dialog = new SimpleInputDialog();
                dialog.setArguments(bundle);
                dialog.show(getFragmentManager(), "dialog");
            }
        });

        dateText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment dateDialog = new MyDatePickerDialog();
                Bundle bundle = new Bundle();
                bundle.putString("tag","new");
                dateDialog.setArguments(bundle);
                dateDialog.show(getFragmentManager(), "datePicker");
            }
        });

        timeText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment timeDialog = new MyTimePickerDialog();
                Bundle bundle = new Bundle();
                bundle.putString("tag","new");
                timeDialog.setArguments(bundle);
                timeDialog.show(getFragmentManager(), "timePicker");
            }
        });

        if (selectedType == 0) selectedType = 2;
        initLoader(LOADER_TYPE);

        return v;
    }

    private void callEventList(){
        //Log.w(TAG, "Method callEventList() called!");
        Fragment fragment = new GenericListFragment();
        Bundle bundle = new Bundle();
        bundle.putString("title", getContext().getResources().getString(R.string.choose_game));
        bundle.putString("table", EventTable.TABLE_NAME);
        bundle.putInt("selected", selectedEvent);
        bundle.putInt("type", selectedType);
        bundle.putString("tag", getTag());
        callback.loadNewFragment(fragment, bundle, "game");
    }

    private void initLoader(int loaderType){
        callback.onLoadingData();
        Log.w(TAG, "Inicializing Loader " + loaderType);
        if (getLoaderManager().getLoader(loaderType) != null){
            getLoaderManager().destroyLoader(loaderType);
        }
        getLoaderManager().restartLoader(loaderType, null, this);
    }

    private boolean checkIfIsOk(){
        if (hasDate && hasTime && commentText.getText().length() < 60){
            createButton.setEnabled(true);
            createButton.setText(R.string.new_event_button);
            return true;
        } else {
            createButton.setEnabled(false);
            createButton.setText(R.string.waiting_data);
            return false;
        }
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.clear();
        callback.setToolbarTitle(getString(R.string.new_event_title));
        getActivity().getMenuInflater().inflate(R.menu.home_menu, menu);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] selectionArgs;
        callback.onLoadingData();
        switch (id){
            case LOADER_TYPE:
                selectionArgs = new String[] {String.valueOf(selectedType)};
                return new CursorLoader(
                        getContext(),
                        DataProvider.EVENT_TYPE_URI,
                        EventTypeTable.ALL_COLUMNS,
                        EventTypeTable.COLUMN_ID + "=?",
                        selectionArgs,
                        null
                );
            case LOADER_EVENT:
                selectionArgs = new String[] {String.valueOf(selectedEvent)};
                return new CursorLoader(
                        getContext(),
                        DataProvider.EVENT_URI,
                        EventTable.ALL_COLUMNS,
                        EventTable.COLUMN_ID + "=?",
                        selectionArgs,
                        null
                );
            case LOADER_EVENT_WITH_TYPE:
                selectionArgs = new String[] {String.valueOf(selectedType)};
                return new CursorLoader(
                        getContext(),
                        DataProvider.EVENT_URI,
                        EventTable.ALL_COLUMNS,
                        EventTable.COLUMN_TYPE + "=?",
                        selectionArgs,
                        StringUtils.getLanguageString(getContext())
                );
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null && data.moveToFirst()){
            switch (loader.getId()){
                case LOADER_TYPE:
                    setViewIcon(iconType,getContext().getResources().getIdentifier(data.getString(data.getColumnIndexOrThrow(EventTypeTable.COLUMN_ICON)),"drawable",getContext().getPackageName()));
                    textType.setText(EventTypeTable.getName(getContext(), data));
                    game.setTypeName(textType.getText().toString());
                    selectedType = data.getInt(data.getColumnIndexOrThrow(EventTypeTable.COLUMN_ID));
                    if (selectedEvent == 0){
                        initLoader(LOADER_EVENT_WITH_TYPE);
                    } else initLoader(LOADER_EVENT);
                    break;
                case LOADER_EVENT:
                case LOADER_EVENT_WITH_TYPE:
                    selectedEvent = data.getInt(data.getColumnIndexOrThrow(EventTable.COLUMN_ID));
                    prepareEventViews(data);
                    break;
            }
        }
        callback.onDataLoaded();
    }

    private void prepareEventViews(Cursor data){
        minLight = data.getInt(data.getColumnIndexOrThrow(EventTable.COLUMN_LIGHT));
        lightBar.setMax(MAX_LIGHT - minLight);
        lightText.setText(String.valueOf(minLight));
        String iconId = data.getString(data.getColumnIndexOrThrow(EventTable.COLUMN_ICON));
        setViewIcon(iconGame,getContext().getResources().getIdentifier(iconId,"drawable",getContext().getPackageName()));
        textGame.setText(EventTable.getName(getContext(),data));
        game.setEventIcon(data.getString(data.getColumnIndexOrThrow(EventTable.COLUMN_ICON)));
        game.setEventName(textGame.getText().toString());
        game.setMaxGuardians(data.getInt(data.getColumnIndexOrThrow(EventTable.COLUMN_GUARDIANS)));
    }

    private void setViewIcon(ImageView view, int resId){
        if (resId != 0){
            view.setImageResource(resId);
        } else {
            Log.w(TAG, "Drawable resource not found.");
            view.setImageResource(R.drawable.ic_missing);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.w("NewEvent Loader: ", "O Loader entrou no método onLoaderReset");
    }

    @Override
    public void onPositiveClick(String input, int type) {
        switch (type){
            case FromDialogListener.LIGHT_TYPE:
                lightText.setText(input);
                int min = Integer.parseInt(input) - minLight;
                lightBar.setProgress(min);
                break;
        }
    }

    @Override
    public void onDateSent(Calendar date) {
        insertedDate = date;
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", getResources().getConfiguration().locale);
        String finalDate = sdf.format(date.getTime());
        dateText.setText(finalDate);
        hasDate = true;
        if (!checkIfIsOk()){
            DialogFragment timeDialog = new MyTimePickerDialog();
            Bundle bundle = new Bundle();
            bundle.putString("tag","new");
            timeDialog.setArguments(bundle);
            timeDialog.show(getFragmentManager(), "timePicker");
        }
    }

    @Override
    public void onTimeSent(int hour, int minute) {

        String date = dateText.getText().toString();

        if (date.isEmpty()){
            insertedDate = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", getResources().getConfiguration().locale);
            String finalDate = sdf.format(insertedDate.getTime());
            dateText.setText(finalDate);
            insertedDate.set(Calendar.HOUR_OF_DAY, hour);
            insertedDate.set(Calendar.MINUTE, minute);
            insertedDate.set(Calendar.SECOND, 0);
        } else {
            insertedDate.set(Calendar.HOUR_OF_DAY, hour);
            insertedDate.set(Calendar.MINUTE, minute);
            insertedDate.set(Calendar.SECOND, 0);
        }

        String hourOfTheDay = String.valueOf(hour);
        String min = String.valueOf(minute);
        if (hourOfTheDay.length() == 1) hourOfTheDay = "0" + hourOfTheDay;
        if (min.length() == 1) min = "0" + min;
        String time = hourOfTheDay + " : " + min;
        timeText.setText(time);
        hasTime = true;
        hasDate = true;
        checkIfIsOk();

    }

    @Override
    public void onLogoff() {}

    @Override
    public void onItemSelected(String type, String entry, int value) {}

    @Override
    public void onMultiItemSelected(boolean[] items) {}

    @Override
    public void onEventTypeSent(int id) {
        selectedType = id;
        selectedEvent = 0;
        getLoaderManager().destroyLoader(LOADER_EVENT);
        getLoaderManager().destroyLoader(LOADER_EVENT_WITH_TYPE);
        //initLoader(LOADER_TYPE);
        //initLoader(LOADER_EVENT_WITH_TYPE);
        callEventList();
    }

    @Override
    public void onEventGameSent(int id) {
        Log.w(TAG, "onEventGameSent called");
        selectedEvent = id;
        getLoaderManager().destroyLoader(LOADER_EVENT);
        getLoaderManager().destroyLoader(LOADER_EVENT_WITH_TYPE);
        //initLoader(LOADER_EVENT);
        //initLoader(LOADER_TYPE);
    }

    @Override
    public void onOrderBySet(String orderBy) { }

    private void createNewEvent() {

        String date = dateText.getText().toString();
        String time = timeText.getText().toString();

        if(!date.isEmpty() || !time.isEmpty()){
            Calendar now = Calendar.getInstance();

            Calendar minimumTime = Calendar.getInstance();
            Calendar notifyTime = getNotifyTime();
            minimumTime.setTime(notifyTime.getTime());
            minimumTime.add(Calendar.MINUTE, -10);

            String gameTime = getBungieTime(date, time);

            String bungieId = callback.getBungieId();
            String userName = callback.getUserName();
            game.setCreatorName(userName);
            game.setCreatorId(bungieId);
            game.setEventId(selectedEvent);
            game.setTypeId(selectedType);
            game.setTime(gameTime);
            game.setInscriptions(1);
            game.setMinLight(lightBar.getProgress() + minLight);
            game.setStatus(GameModel.STATUS_NEW);
            if (commentText.getText().length() != 0){
                game.setComment(commentText.getText().toString());
            } else game.setComment("");
            game.setJoined(true);

            if (now.getTimeInMillis() <= minimumTime.getTimeInMillis()) {
                Bundle bundle = new Bundle();
                bundle.putInt(ServerService.REQUEST_TAG, ServerService.TYPE_CREATE_GAME);
                bundle.putSerializable(ServerService.GAME_TAG, game);
                callback.runServerService(bundle);
            } else Toast.makeText(getContext(), getResources().getString(R.string.match_must_created) + " " + minimumIntTime + " " + getResources().getString(R.string.minutes_advance), Toast.LENGTH_SHORT).show();

        } else Toast.makeText(getContext(), R.string.when_play, Toast.LENGTH_SHORT).show();

    }

    private Calendar getNotifyTime() {
        Calendar notifyTime = Calendar.getInstance();
        notifyTime.setTime(insertedDate.getTime());

        SharedPreferences sharedPrefs = getActivity().getSharedPreferences(DrawerActivity.SHARED_PREFS, Context.MODE_PRIVATE);
        int alarmTime = sharedPrefs.getInt(DrawerActivity.SCHEDULED_TIME_PREF, 0)*-1;
        notifyTime.add(Calendar.MINUTE,alarmTime);
        minimumIntTime = (alarmTime*-1) + 10;

        return notifyTime;
    }

    private String getBungieTime(String date, String time) {

        String year = date.substring(date.lastIndexOf("/")+1,date.length());
        String month = date.substring(date.indexOf("/")+1,date.lastIndexOf("/"));
        String day = date.substring(0,date.indexOf("/"));
        String hour = time.substring(0,time.indexOf(" "));
        String minute = time.substring(time.lastIndexOf(" ")+1, time.length());

        return year + "-" + month + "-" + day + "T" + hour + ":" + minute + ":00";

    }

    public void onServerResponse(int type){
        switch (type){
            case ServerService.TYPE_CREATE_GAME:
                createButton.setText(R.string.creating_button_msg);
                createButton.setEnabled(false);
                break;
            case ServerService.TYPE_NEW_EVENTS:
                createButton.setText(R.string.waiting_data);
                createButton.setEnabled(false);
                break;
            default:
                checkIfIsOk();
                break;
        }
    }

}