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

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        String title = "";

        Bundle bundle = getArguments();

        if (bundle != null){
            title = bundle.getString("title");
            selectedItem = bundle.getInt("selectedItem");
            listener = (FromDialogListener) getFragmentManager().findFragmentByTag(bundle.getString("fragTag"));
        }

        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity(), R.style.DestinyApp_AlertDialog)
                .setTitle(title)
                .setSingleChoiceItems(R.array.pref_time_list_entries, selectedItem, this)
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
        String[] entries = getResources().getStringArray(R.array.pref_time_list_entries);
        String[] values = getResources().getStringArray(R.array.pref_time_list_values);
        if (listener != null ){
            selectedItem = which;
            listener.onItemSelected(entries[which],Integer.parseInt(values[which]));
        } else {
            Log.w(TAG, "Listener não foi encontrado!");
        }
        dialog.dismiss();
    }
}
