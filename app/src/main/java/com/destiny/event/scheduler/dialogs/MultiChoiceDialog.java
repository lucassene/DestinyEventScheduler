package com.destiny.event.scheduler.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.destiny.event.scheduler.R;
import com.destiny.event.scheduler.interfaces.FromDialogListener;

public class MultiChoiceDialog extends DialogFragment implements DialogInterface.OnMultiChoiceClickListener {

    private static final String TAG = "MultiChoiceDialog";

    private FromDialogListener listener;

    private boolean[] checkedItems;
    private String[] eventList;
    private int[] eventIdList;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        String title = "";
        eventList = getResources().getStringArray(R.array.event_types);
        eventIdList = getResources().getIntArray(R.array.event_type_ids);

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
        Log.w(TAG, "Event (" + eventIdList[which] + ") " + eventList[which] + " foi marcado como " + isChecked);
        getCounts();
    }

    private void getCounts() {
        int trues = 0;
        int total = 0;
        int falses = 0;
        for (int i=0;i<checkedItems.length;i++){
            if (checkedItems[i]){
                trues ++;
            } else falses++;
            total++;
        }
        //Toast.makeText(getContext(), "Total: " + total + " True: " + trues + " False: " + falses, Toast.LENGTH_SHORT).show();
    }
}
