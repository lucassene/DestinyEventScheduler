package com.app.the.bunker.interfaces;


import com.app.the.bunker.models.MultiChoiceItemModel;

import java.util.Calendar;
import java.util.List;

public interface FromDialogListener {

    int GUARDIAN_TYPE = 1;
    int LIGHT_TYPE = 2;

    void onPositiveClick(String input, int type);
    void onDateSent(Calendar date);
    void onTimeSent(int hour, int minute);
    void onLogoff();
    void onItemSelected(String type, String entry, int value);
    void onMultiItemSelected(boolean[] items);
    void onListChecked(List<MultiChoiceItemModel> list);
}
