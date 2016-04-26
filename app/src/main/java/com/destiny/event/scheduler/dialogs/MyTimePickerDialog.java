package com.destiny.event.scheduler.dialogs;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.TimePicker;

import com.destiny.event.scheduler.interfaces.FromDialogListener;

import java.util.Calendar;

public class MyTimePickerDialog extends DialogFragment implements TimePickerDialog.OnTimeSetListener {

    FromDialogListener listener;
    String fragment;

    int mHour;
    int mMinute;

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

        String hour = String.valueOf(hourOfDay);
        String min = String.valueOf(minute);

        if (hour.length() == 1) hour = "0" + hour;
        if (min.length() == 1) min = "0" + min;

        String time = hour + " : " + min;
        listener.onTimeSent(time);
    }
}
