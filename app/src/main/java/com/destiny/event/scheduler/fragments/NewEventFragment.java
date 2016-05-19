package com.destiny.event.scheduler.fragments;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.destiny.event.scheduler.R;
import com.destiny.event.scheduler.data.EntryTable;
import com.destiny.event.scheduler.data.EventTable;
import com.destiny.event.scheduler.data.EventTypeTable;
import com.destiny.event.scheduler.data.GameTable;
import com.destiny.event.scheduler.data.NotificationTable;
import com.destiny.event.scheduler.dialogs.MyDatePickerDialog;
import com.destiny.event.scheduler.dialogs.MyTimePickerDialog;
import com.destiny.event.scheduler.dialogs.SimpleInputDialog;
import com.destiny.event.scheduler.interfaces.FromActivityListener;
import com.destiny.event.scheduler.interfaces.FromDialogListener;
import com.destiny.event.scheduler.interfaces.OnEventCreatedListener;
import com.destiny.event.scheduler.interfaces.ToActivityListener;
import com.destiny.event.scheduler.provider.DataProvider;
import com.destiny.event.scheduler.utils.DateUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;


public class NewEventFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, FromDialogListener, FromActivityListener {

    private static final String TAG = "NewEventFragment";

    private static final int LOADER_TYPE = 10;
    private static final int LOADER_EVENT = 20;
    private static final int LOADER_NOTIFICATION = 80;

    private String selectedType;
    private String selectedEvent;

    private String gameId;
    private String gameTime;
    private String eventTypeName;
    private int eventIcon;
    private String eventName;

    private ToActivityListener callback;
    private OnEventCreatedListener eventCallback;

    private ImageView iconType;
    private TextView textType;
    private RelativeLayout typeLayout;

    private ImageView iconGame;
    private TextView textGame;
    private RelativeLayout gameLayout;

    private TextView lightText;
    private SeekBar lightBar;
    private int minLight;

    private EditText dateText;
    private EditText timeText;

    private Button createButton;

    private SimpleInputDialog dialog;

    private Calendar insertedDate;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        selectedType = "2";
        selectedEvent = "7";

        Bundle bundle = getArguments();
        if (bundle != null){
            switch (bundle.getString("Table")){
                case EventTypeTable.TABLE_NAME:
                    selectedType = String.valueOf(bundle.getLong("id"));
                    callEventList();
                    checkGame(selectedType);
                    break;
                case EventTable.TABLE_NAME:
                    selectedEvent = String.valueOf(bundle.getLong("id"));
                    selectedType = String.valueOf(bundle.getString("Type"));
            }
        }

    }

    private void checkGame(String selectedType) {
        switch (selectedType){
            case "1":
                selectedEvent = "1";
                break;
            case "2":
                selectedEvent = "4";
                break;
            case "3":
                selectedEvent = "19";
                break;
            case "4":
                selectedEvent = "24";
                break;
            case "5":
                selectedEvent = "30";
                break;
            case "6":
                selectedEvent = "36";
                break;
            case "7":
                selectedEvent = "38";
                break;
            case "8":
                selectedEvent = "52";
                break;
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        setHasOptionsMenu(true);
        callback = (ToActivityListener) getActivity();
        eventCallback = (OnEventCreatedListener) getActivity();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.new_event_title);

        View v = inflater.inflate(R.layout.new_event_layout, container, false);

        iconType = (ImageView) v.findViewById(R.id.type_icon);
        textType = (TextView) v.findViewById(R.id.type_text);
        typeLayout = (RelativeLayout) v.findViewById(R.id.type_list);

        iconGame = (ImageView) v.findViewById(R.id.game_icon);
        textGame = (TextView) v.findViewById(R.id.game_text);
        gameLayout = (RelativeLayout) v.findViewById(R.id.game_list);

        lightText = (TextView) v.findViewById(R.id.light_min_text);
        lightBar = (SeekBar) v.findViewById(R.id.light_bar);

        dateText = (EditText) v.findViewById(R.id.date_text);
        timeText = (EditText) v.findViewById(R.id.time_text);

        createButton = (Button) v.findViewById(R.id.btn_gravar);

        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createNewEvent();
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
                bundle.putString("selected", selectedType);
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
                bundle.putInt("max", 335);
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


        fillTypeData();
        fillgameData();

        return v;
    }

    private void callEventList(){
        Log.w(TAG, "Method calEventList() called!");
        Fragment fragment = new GenericListFragment();
        Bundle bundle = new Bundle();
        bundle.putString("title", getContext().getResources().getString(R.string.choose_game));
        bundle.putString("table", EventTable.TABLE_NAME);
        bundle.putString("selected", selectedEvent);
        bundle.putString("type", selectedType);
        bundle.putString("tag", getTag());
        callback.loadNewFragment(fragment, bundle, "game");
    }

    private void fillgameData() {
        getLoaderManager().initLoader(LOADER_EVENT, null, this);
    }

    private void fillTypeData() {
        getLoaderManager().initLoader(LOADER_TYPE, null, this);
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

        String[] projection;
        String[] selectionArgs;

        switch (id){
            case LOADER_TYPE:
                projection = EventTypeTable.ALL_COLUMNS;
                selectionArgs = new String[] {selectedType};
                return new CursorLoader(
                        getContext(),
                        DataProvider.EVENT_TYPE_URI,
                        projection,
                        EventTypeTable.COLUMN_ID + "=?",
                        selectionArgs,
                        null
                );
            case LOADER_EVENT:
                projection = EventTable.ALL_COLUMNS;
                selectionArgs = new String[] {selectedEvent};
                return new CursorLoader(
                        getContext(),
                        DataProvider.EVENT_URI,
                        projection,
                        EventTable.COLUMN_ID + "=?",
                        selectionArgs,
                        null
                );
            case LOADER_NOTIFICATION:
                return new CursorLoader(
                        getContext(),
                        DataProvider.NOTIFICATION_URI,
                        NotificationTable.ALL_COLUMNS,
                        NotificationTable.COLUMN_GAME + "=" + gameId,
                        null,
                        null
                );
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        data.moveToFirst();
        switch (loader.getId()){
            case LOADER_TYPE:
                iconType.setImageResource(getContext().getResources().getIdentifier(data.getString(data.getColumnIndexOrThrow(EventTypeTable.COLUMN_ICON)),"drawable",getContext().getPackageName()));
                eventTypeName = getContext().getResources().getString(getContext().getResources().getIdentifier(data.getString(data.getColumnIndexOrThrow(EventTypeTable.COLUMN_NAME)),"string",getContext().getPackageName()));
                textType.setText(getContext().getResources().getIdentifier(data.getString(data.getColumnIndexOrThrow(EventTypeTable.COLUMN_NAME)),"string",getContext().getPackageName()));
                break;
            case LOADER_EVENT:
                minLight = data.getInt(data.getColumnIndexOrThrow(EventTable.COLUMN_LIGHT));
                lightBar.setMax(335 - minLight);
                lightText.setText(String.valueOf(minLight));
                String iconId = data.getString(data.getColumnIndexOrThrow(EventTable.COLUMN_ICON));
                eventIcon = getContext().getResources().getIdentifier(data.getString(data.getColumnIndexOrThrow(EventTable.COLUMN_ICON)),"drawable",getContext().getPackageName());
                iconGame.setImageResource(getContext().getResources().getIdentifier(iconId,"drawable",getContext().getPackageName()));
                eventName = getContext().getResources().getString(getContext().getResources().getIdentifier(data.getString(data.getColumnIndexOrThrow(EventTable.COLUMN_NAME)),"string",getContext().getPackageName()));
                textGame.setText(getContext().getResources().getIdentifier(data.getString(data.getColumnIndexOrThrow(EventTable.COLUMN_NAME)),"string",getContext().getPackageName()));
                break;
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

        Log.w(TAG, "Entrada de Date: " + insertedDate.get(Calendar.DAY_OF_MONTH) + "/" + insertedDate.get(Calendar.MONTH) + "/" + insertedDate.get(Calendar.YEAR));

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", getResources().getConfiguration().locale);
        String finalDate = sdf.format(date.getTime());
        dateText.setText(finalDate);
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

        Log.w(TAG, "Entrada de Time: " + insertedDate.get(Calendar.HOUR_OF_DAY) + ":" + insertedDate.get(Calendar.MINUTE) + ":00");

        String hourOfTheDay = String.valueOf(hour);
        String min = String.valueOf(minute);
        if (hourOfTheDay.length() == 1) hourOfTheDay = "0" + hourOfTheDay;
        if (min.length() == 1) min = "0" + min;
        String time = hourOfTheDay + " : " + min;
        timeText.setText(time);

    }

    @Override
    public void onLogoff() {

    }


    @Override
    public void onEventTypeSent(String id) {
        selectedType = id;
        callEventList();
        getLoaderManager().restartLoader(LOADER_TYPE, null, this);
        //checkGame(id);
        //getLoaderManager().restartLoader(LOADER_EVENT, null, this);
    }

    @Override
    public void onEventGameSent(String id) {
        selectedEvent = id;
        getLoaderManager().restartLoader(LOADER_EVENT, null, this);
    }

    @Override
    public void onOrderBySet(String orderBy) {

    }


    public void createNewEvent() {

        String date = dateText.getText().toString();
        String time = timeText.getText().toString();


        if(!date.isEmpty() || !time.isEmpty()){

            Calendar now = Calendar.getInstance();

            Calendar notifyTime = Calendar.getInstance();
            notifyTime.setTime(insertedDate.getTime());
            notifyTime.add(Calendar.MINUTE, -5);

            Calendar minimumTime = Calendar.getInstance();
            minimumTime.setTime(insertedDate.getTime());
            minimumTime.add(Calendar.MINUTE, -7);

            Log.w(TAG, "InsertedDate: " + insertedDate.get(Calendar.HOUR_OF_DAY) + ":" + insertedDate.get(Calendar.MINUTE) + ":00");
            Log.w(TAG, "NotifyTime: " + notifyTime.get(Calendar.HOUR_OF_DAY) + ":" + notifyTime.get(Calendar.MINUTE) + ":00");
            Log.w(TAG, "MinimumTime: " + minimumTime.get(Calendar.HOUR_OF_DAY) + ":" + minimumTime.get(Calendar.MINUTE) + ":00");

            if (now.getTimeInMillis() <= minimumTime.getTimeInMillis()){

                timeText.setText(time);

                gameTime = getBungieTime(date, time);

                int minLight = Integer.parseInt(lightText.getText().toString());
                int insc = 1;
                String bungieId = callback.getBungieId();
                String userName = callback.getUserName();

                ContentValues gameValues = new ContentValues();
                gameValues.put(GameTable.COLUMN_CREATOR, bungieId);
                gameValues.put(GameTable.COLUMN_CREATOR_NAME,userName);
                gameValues.put(GameTable.COLUMN_EVENT_ID, selectedEvent);
                gameValues.put(GameTable.COLUMN_TIME, gameTime);
                gameValues.put(GameTable.COLUMN_LIGHT, minLight);
                gameValues.put(GameTable.COLUMN_INSCRIPTIONS, 4);
                gameValues.put(GameTable.COLUMN_STATUS, GameTable.STATUS_SCHEDULED); //Inserir STATUS_NEW no Servidor

                Uri result = getContext().getContentResolver().insert(DataProvider.GAME_URI, gameValues);
                if (result != null) {
                    gameId = result.getLastPathSegment();
                }

                ContentValues entryValues = new ContentValues();
                entryValues.put(EntryTable.COLUMN_MEMBERSHIP, bungieId);
                entryValues.put(EntryTable.COLUMN_GAME, gameId);
                entryValues.put(EntryTable.COLUMN_TIME, DateUtils.getCurrentTime());
                getContext().getContentResolver().insert(DataProvider.ENTRY_URI, entryValues);

                createFakeEntries();

                setAlarmNotification(notifyTime, gameId, eventName, eventTypeName, eventIcon);

                Toast.makeText(getContext(), "Success! You've created one new match!", Toast.LENGTH_SHORT).show();

                eventCallback.onEventCreated();

            } else {
                Toast.makeText(getContext(), "A partida precisa ser criada com, no mínimo, 07 minutos de antecedência.", Toast.LENGTH_SHORT).show();
            }

        } else{
            Toast.makeText(getContext(), "Hmmm, when do you wanna play?", Toast.LENGTH_SHORT).show();
        }

    }

    //Inserindo entries falsas apenas para testes
    private void createFakeEntries() {

        ContentValues values = new ContentValues();
        values.put(EntryTable.COLUMN_MEMBERSHIP,"4611686018446566077");
        values.put(EntryTable.COLUMN_GAME, gameId);
        values.put(EntryTable.COLUMN_TIME,"2016-05-30T16:43:00");
        getContext().getContentResolver().insert(DataProvider.ENTRY_URI, values);
        values.clear();

        values = new ContentValues();
        values.put(EntryTable.COLUMN_MEMBERSHIP,"4611686018434509539");
        values.put(EntryTable.COLUMN_GAME, gameId);
        values.put(EntryTable.COLUMN_TIME,"2016-05-30T16:43:00");
        getContext().getContentResolver().insert(DataProvider.ENTRY_URI, values);
        values.clear();

        values = new ContentValues();
        values.put(EntryTable.COLUMN_MEMBERSHIP,"4611686018444413912");
        values.put(EntryTable.COLUMN_GAME, gameId);
        values.put(EntryTable.COLUMN_TIME,"2016-05-30T16:43:00");
        getContext().getContentResolver().insert(DataProvider.ENTRY_URI, values);
        values.clear();

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
                Log.w(TAG, "Notificação não foi inserida");
            } else {
                int id = Integer.parseInt(uri.getLastPathSegment());
                values.clear();
                callback.registerAlarmTask(notifyTime, id);
            }

    }

    private String getBungieTime(String date, String time) {

        String year = date.substring(date.lastIndexOf("/")+1,date.length());
        String month = date.substring(date.indexOf("/")+1,date.lastIndexOf("/"));
        String day = date.substring(0,date.indexOf("/"));
        String hour = time.substring(0,time.indexOf(" "));
        String minute = time.substring(time.lastIndexOf(" ")+1, time.length());

        //Log.w(TAG, "Bungie Time: " + year + "-" + month + "-" + day + "T" + time);
        return year + "-" + month + "-" + day + "T" + hour + ":" + minute + ":00";

    }
}