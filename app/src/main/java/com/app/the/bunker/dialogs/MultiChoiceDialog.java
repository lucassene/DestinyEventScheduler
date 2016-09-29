package com.app.the.bunker.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.app.the.bunker.R;
import com.app.the.bunker.interfaces.FromDialogListener;

public class MultiChoiceDialog extends DialogFragment implements DialogInterface.OnMultiChoiceClickListener {

    private static final String TAG = "MultiChoiceDialog";

    private FromDialogListener listener;

    private boolean[] checkedItems;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        String title = "";

        Bundle bundle = getArguments();

        checkedItems = new boolean[] {false, false, false, false, false, false, false, false};

        if (bundle != null){
            title = bundle.getString("title");
            checkedItems = bundle.getBooleanArray("selectedItems");
            listener = (FromDialogListener) getFragmentManager().findFragmentByTag(bundle.getString("fragTag"));
        }

        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity(), R.style.DestinyApp_AlertDialog)
                .setTitle(title)
                .setMultiChoiceItems(R.array.event_types, checkedItems, this)
                .setNegativeButton(getResources().getString(R.string.nevermind), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton(getResources().getString(R.string.save), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (listener != null){
                            listener.onMultiItemSelected(checkedItems);
                        } else Log.w(TAG, "Listener não foi encontrado!");
                        dialog.dismiss();
                    }
                });

        return dialog.create();
    }

    @Override
    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
        checkedItems[which] = isChecked;
        //Log.w(TAG, "Event (" + eventIdList[which] + ") " + eventList[which] + " foi marcado como " + isChecked);
    }
}
