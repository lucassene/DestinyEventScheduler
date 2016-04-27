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
import com.destiny.event.scheduler.dialogs.MyDatePickerDialog;
import com.destiny.event.scheduler.dialogs.MyTimePickerDialog;
import com.destiny.event.scheduler.dialogs.SimpleInputDialog;
import com.destiny.event.scheduler.interfaces.FromActivityListener;
import com.destiny.event.scheduler.interfaces.FromDialogListener;
import com.destiny.event.scheduler.interfaces.OnEventCreatedListener;
import com.destiny.event.scheduler.interfaces.ToActivityListener;
import com.destiny.event.scheduler.provider.DataProvider;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;


public class NewEventFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, FromDialogListener, FromActivityListener {

    private static final String TAG = "NewEventFragment";

    private static final int URL_LOADER_TYPE = 10;
    private static final int URL_LOADER_GAME = 20;

    private String selectedType;
    private String selectedGame;

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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        selectedType = "2";
        selectedGame = "7";

        Bundle bundle = getArguments();
        if (bundle != null){
            switch (bundle.getString("Table")){
                case EventTypeTable.TABLE_NAME:
                    selectedType = String.valueOf(bundle.getLong("id"));
                    checkGame(selectedType);
                    break;
                case EventTable.TABLE_NAME:
                    selectedGame = String.valueOf(bundle.getLong("id"));
                    selectedType = String.valueOf(bundle.getString("Type"));
            }
        }
    }

    private void checkGame(String selectedType) {
        switch (selectedType){
            case "1":
                selectedGame = "1";
                break;
            case "2":
                selectedGame = "4";
                break;
            case "3":
                selectedGame = "19";
                break;
            case "4":
                selectedGame = "24";
                break;
            case "5":
                selectedGame = "30";
                break;
            case "6":
                selectedGame = "36";
                break;
            case "7":
                selectedGame = "38";
                break;
            case "8":
                selectedGame = "52";
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
            Fragment fragment = new GenericListFragment();

            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString("title", getContext().getResources().getString(R.string.choose_game));
                bundle.putString("table", EventTable.TABLE_NAME);
                bundle.putString("selected", selectedGame);
                bundle.putString("type", selectedType);
                bundle.putString("tag", getTag());
                callback.loadNewFragment(fragment, bundle, "game");
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

    private void fillgameData() {
        getLoaderManager().initLoader(URL_LOADER_GAME, null, this);
    }

    private void fillTypeData() {
        getLoaderManager().initLoader(URL_LOADER_TYPE, null, this);
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
            case URL_LOADER_TYPE:
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
            case URL_LOADER_GAME:
                projection = EventTable.ALL_COLUMNS;
                selectionArgs = new String[] {selectedGame};
                return new CursorLoader(
                        getContext(),
                        DataProvider.EVENT_URI,
                        projection,
                        EventTable.COLUMN_ID + "=?",
                        selectionArgs,
                        null
                );
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        String iconId;
        String textId;
        int resId;

        data.moveToFirst();
        switch (loader.getId()){
            case URL_LOADER_TYPE:
                iconId = data.getString(data.getColumnIndexOrThrow(EventTypeTable.COLUMN_ICON));
                iconType.setImageResource(getContext().getResources().getIdentifier(iconId,"drawable",getContext().getPackageName()));
                textId = data.getString(data.getColumnIndexOrThrow(EventTypeTable.COLUMN_NAME));
                textType.setText(getContext().getResources().getIdentifier(textId,"string",getContext().getPackageName()));
                break;
            case URL_LOADER_GAME:
                minLight = data.getInt(data.getColumnIndexOrThrow(EventTable.COLUMN_LIGHT));
                lightBar.setMax(335 - minLight);
                lightText.setText(String.valueOf(minLight));
                iconId = data.getString(data.getColumnIndexOrThrow(EventTable.COLUMN_ICON));
                iconGame.setImageResource(getContext().getResources().getIdentifier(iconId,"drawable",getContext().getPackageName()));
                textId = data.getString(data.getColumnIndexOrThrow(EventTable.COLUMN_NAME));
                textGame.setText(getContext().getResources().getIdentifier(textId,"string",getContext().getPackageName()));
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.w("NewEvent Loader: ", "O Loader entrou no método onLoaderReset");
    }

    @Override
    public void onPositiveClick(String input, int type) {
        int value = Integer.parseInt(input);

        switch (type){
            case FromDialogListener.LIGHT_TYPE:
                lightText.setText(input);
                int min = Integer.parseInt(input) - minLight;
                lightBar.setProgress(min);
                break;
        }


    }

    @Override
    public void onDateSent(String date) {
        dateText.setText(date);
    }

    @Override
    public void onTimeSent(String time) {
        timeText.setText(time);
    }

    @Override
    public void onLogoff() {

    }


    @Override
    public void onEventTypeSent(String id) {
        selectedType = id;
        getLoaderManager().restartLoader(URL_LOADER_TYPE, null, this);
        checkGame(id);
        getLoaderManager().restartLoader(URL_LOADER_GAME, null, this);
    }

    @Override
    public void onEventGameSent(String id) {
        selectedGame = id;
        getLoaderManager().restartLoader(URL_LOADER_GAME, null, this);
    }


    public void createNewEvent() {
        String date = dateText.getText().toString();
        String time = timeText.getText().toString();

        if(!date.isEmpty() || !time.isEmpty()){

            String fullTime = getBungieTime(date, time);

            int minLight = Integer.parseInt(lightText.getText().toString());
            int insc = 1;
            String bungieId = callback.getBungieId();
            String userName = callback.getUserName();

            ContentValues gameValues = new ContentValues();
            gameValues.put(GameTable.COLUMN_CREATOR, bungieId);
            gameValues.put(GameTable.COLUMN_CREATOR_NAME,userName);
            gameValues.put(GameTable.COLUMN_EVENT_ID, selectedGame);
            gameValues.put(GameTable.COLUMN_TIME,fullTime);
            gameValues.put(GameTable.COLUMN_LIGHT, minLight);
            gameValues.put(GameTable.COLUMN_INSCRIPTIONS, insc);
            gameValues.put(GameTable.COLUMN_STATUS, GameTable.GAME_SCHEDULED);

            Uri result = getContext().getContentResolver().insert(DataProvider.GAME_URI, gameValues);
            String id = "";
            if (result != null) {
                id = result.getLastPathSegment();
            }

            String now = getCurrentTime();
            //Toast.makeText(getContext(), "Time: " + now, Toast.LENGTH_SHORT).show();

            ContentValues entryValues = new ContentValues();
            entryValues.put(EntryTable.COLUMN_MEMBERSHIP, bungieId);
            entryValues.put(EntryTable.COLUMN_GAME, id);
            entryValues.put(EntryTable.COLUMN_TIME, now);
            getContext().getContentResolver().insert(DataProvider.ENTRY_URI, entryValues);

            Toast.makeText(getContext(), "Success! You've created one brand new event!", Toast.LENGTH_SHORT).show();
            eventCallback.onEventCreated();

        } else{
            Toast.makeText(getContext(), "Hmmm, when do you wanna play?", Toast.LENGTH_SHORT).show();
        }

    }

    private String getCurrentTime() {
        Calendar c = GregorianCalendar.getInstance();
        String minute = String.valueOf(c.get(Calendar.MINUTE));
        String hour = String.valueOf(c.get(Calendar.HOUR));
        String day = String.valueOf(c.get(Calendar.DAY_OF_MONTH));
        String month = String.valueOf(c.get(Calendar.MONTH)+1);
        String year = String.valueOf(c.get(Calendar.YEAR));

        // SimpleDateFormat Class
        SimpleDateFormat sdfDateTime = new SimpleDateFormat("yyyy-MM-dd");
        String newdate =  sdfDateTime.format(new Date(System.currentTimeMillis()));

        SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm:ss");
        String newtime = sdfTime.format(new Date(System.currentTimeMillis()));

        String finalString = newdate + "T" + newtime;

        Log.w(TAG, "Time: " + finalString);

        return finalString ;
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