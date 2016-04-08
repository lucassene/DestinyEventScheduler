package com.destiny.event.scheduler.dialogs;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.DatePicker;

import com.destiny.event.scheduler.interfaces.FromDialogListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class MyDatePickerDialog extends DialogFragment implements DatePickerDialog.OnDateSetListener {

    FromDialogListener listener;
    String fragment;



    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Calendar c = Calendar.getInstance();
        int mYear = c.get(Calendar.YEAR);
        int mMonth = c.get(Calendar.MONTH);
        int mDay = c.get(Calendar.DAY_OF_MONTH);

        Bundle bundle = getArguments();
        if (bundle != null){
            fragment = bundle.getString("tag");
            listener = (FromDialogListener) getFragmentManager().findFragmentByTag(fragment);
        }

        return new DatePickerDialog(getActivity(), this, mYear, mMonth, mDay);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        Calendar c = Calendar.getInstance();
        c.set(year, monthOfYear, dayOfMonth);

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", getResources().getConfiguration().locale);
        String finalDate = sdf.format(c.getTime());

        listener.onDateSent(finalDate);
    }
}
