package com.app.the.bunker.dialogs;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.widget.TimePicker;

import com.app.the.bunker.interfaces.FromDialogListener;

import java.util.Calendar;

public class MyTimePickerDialog extends DialogFragment implements TimePickerDialog.OnTimeSetListener {

    FromDialogListener listener;
    String fragment;

    int mHour;
    int mMinute;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Calendar c = Calendar.getInstance();
        mHour = c.get(Calendar.HOUR_OF_DAY);
        mMinute = c.get(Calendar.MINUTE);

        Bundle bundle = getArguments();
        if (bundle != null){
            fragment = bundle.getString("tag");
            listener = (FromDialogListener) getFragmentManager().findFragmentByTag(fragment);
        }

        return new TimePickerDialog(getActivity(),this,mHour,mMinute,true);

    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        listener.onTimeSent(hourOfDay, minute);
    }
}
