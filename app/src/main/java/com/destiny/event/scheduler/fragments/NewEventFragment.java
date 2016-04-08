package com.destiny.event.scheduler.fragments;

import android.content.Context;
import android.database.Cursor;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.destiny.event.scheduler.R;
import com.destiny.event.scheduler.data.EventTable;
import com.destiny.event.scheduler.data.EventTypeTable;
import com.destiny.event.scheduler.dialogs.MyDatePickerDialog;
import com.destiny.event.scheduler.dialogs.MyTimePickerDialog;
import com.destiny.event.scheduler.dialogs.SimpleInputDialog;
import com.destiny.event.scheduler.interfaces.FromActivityListener;
import com.destiny.event.scheduler.interfaces.FromDialogListener;
import com.destiny.event.scheduler.interfaces.ToActivityListener;
import com.destiny.event.scheduler.provider.DataProvider;


public class NewEventFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, FromDialogListener, FromActivityListener {

    private static final int URL_LOADER_TYPE = 10;
    private static final int URL_LOADER_GAME = 20;

    private String selectedType;
    private String selectedGame;

    private ToActivityListener callback;

    private ImageView iconType;
    private TextView textType;
    private RelativeLayout typeLayout;

    private ImageView iconGame;
    private TextView textGame;
    private RelativeLayout gameLayout;

    private TextView guardianMaxText;
    private SeekBar guardianBar;
    private int maxGuardian;

    private TextView lightText;
    private SeekBar lightBar;
    private int minLight;

    private EditText dateText;
    private EditText timeText;

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
                selectedGame = "20";
                break;
            case "4":
                selectedGame = "25";
                break;
            case "5":
                selectedGame = "29";
                break;
            case "6":
                selectedGame = "35";
                break;
            case "7":
                selectedGame = "37";
                break;
            case "8":
                selectedGame = "50";
                break;
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        setHasOptionsMenu(true);
        callback = (ToActivityListener) getActivity();
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

        guardianMaxText = (TextView) v.findViewById(R.id.guardian_total_text);
        guardianBar = (SeekBar) v.findViewById(R.id.guardian_bar);

        lightText = (TextView) v.findViewById(R.id.light_min_text);
        lightBar = (SeekBar) v.findViewById(R.id.light_bar);

        dateText = (EditText) v.findViewById(R.id.date_text);
        timeText = (EditText) v.findViewById(R.id.time_text);

        guardianBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                guardianMaxText.setText(String.valueOf(guardianBar.getProgress()+1));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
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

        guardianMaxText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString("tag", "new");
                bundle.putString("title", getResources().getString(R.string.max_guardians_label));
                bundle.putString("yes", getResources().getString(R.string.save));
                bundle.putString("no", getResources().getString(R.string.cancel));
                bundle.putInt("max", maxGuardian);
                bundle.putInt("min", 1);
                bundle.putInt("type", FromDialogListener.GUARDIAN_TYPE);
                String hint = getResources().getString(R.string.values_between1) + maxGuardian;
                bundle.putString("hint", hint);
                dialog = new SimpleInputDialog();
                dialog.setArguments(bundle);
                dialog.show(getFragmentManager(), "dialog");
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
                bundle.putInt("max", 320);
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
                maxGuardian = data.getInt(data.getColumnIndexOrThrow(EventTable.COLUMN_GUARDIANS));
                guardianBar.setMax(maxGuardian - 1);
                guardianMaxText.setText("1");
                minLight = data.getInt(data.getColumnIndexOrThrow(EventTable.COLUMN_LIGHT));
                lightBar.setMax(320 - minLight);
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
            case FromDialogListener.GUARDIAN_TYPE:
                guardianMaxText.setText(input);
                guardianBar.setProgress(value-1);
                break;
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

    public void createNewEvent(View view) {
        String date = dateText.getText().toString();
        String time = timeText.getText().toString();
        int minGuardian = Integer.parseInt(guardianMaxText.getText().toString());
        int minLight = Integer.parseInt(lightText.getText().toString());

    }
}