package com.destiny.event.scheduler.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.destiny.event.scheduler.R;
import com.destiny.event.scheduler.interfaces.FromDialogListener;

public class SingleChoiceDialog extends DialogFragment implements DialogInterface.OnClickListener{

    private static final String TAG = "SingleChoiceDialog";

    private FromDialogListener listener;

    private int selectedItem = 0;

    String[] entries;
    int[] values;

    String type;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        String title = "";
        Bundle bundle = getArguments();

        int array = 0;
        if (bundle != null){
            title = bundle.getString("title");
            selectedItem = bundle.getInt("selectedItem");
            listener = (FromDialogListener) getFragmentManager().findFragmentByTag(bundle.getString("fragTag"));
            type = bundle.getString("type");
            if (type.equals("scheduled")){
                array = R.array.pref_time_list_entries;
                entries = getResources().getStringArray(R.array.pref_time_list_entries);
                values = getResources().getIntArray(R.array.pref_time_list_values);
            } else if (bundle.getString("type").equals("new")){
                array = R.array.pref_new_time_list_entries;
                entries = getResources().getStringArray(R.array.pref_new_time_list_entries);
                values = getResources().getIntArray(R.array.pref_new_time_list_values);
            }
        }

        return buildDialog(title, array);
    }

    private AlertDialog buildDialog(String title, int array) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity(), R.style.DestinyApp_AlertDialog)
                .setTitle(title)
                .setSingleChoiceItems(array, selectedItem, this)
                .setNegativeButton(getResources().getString(R.string.nevermind), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        return dialog.create();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (listener != null ){
            selectedItem = which;
            listener.onItemSelected(type, entries[which],values[which]);
        } else {
            Log.w(TAG, "Listener não foi encontrado!");
        }
        dialog.dismiss();
    }
}
