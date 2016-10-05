package com.app.the.bunker.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ListView;

import com.app.the.bunker.R;
import com.app.the.bunker.adapters.MultiChoiceAdapter;
import com.app.the.bunker.interfaces.FromDialogListener;
import com.app.the.bunker.models.MultiChoiceItemModel;

import java.util.ArrayList;
import java.util.List;

public class MultiChoiceDialog extends DialogFragment implements DialogInterface.OnMultiChoiceClickListener {

    private static final String TAG = "MultiChoiceDialog";

    private FromDialogListener listener;
    MultiChoiceAdapter mAdapter;

    @SuppressWarnings("unchecked")
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        String title = "";

        Bundle bundle = getArguments();
        List<MultiChoiceItemModel> itemList = new ArrayList<>();

        if (bundle != null){
            title = bundle.getString("title");

            itemList = (List<MultiChoiceItemModel>) bundle.getSerializable("itemList");
            listener = (FromDialogListener) getFragmentManager().findFragmentByTag(bundle.getString("fragTag"));
        }

        mAdapter = new MultiChoiceAdapter(getContext(),itemList);

        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity(), R.style.DestinyApp_AlertDialog)
                .setTitle(title)
                .setAdapter(mAdapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mAdapter.toogleItemChecked(which);
                    }
                })
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
                            listener.onListChecked(mAdapter.getItemList());
                        } else Log.w(TAG, "Listener não foi encontrado!");
                        dialog.dismiss();
                    }
                });

        AlertDialog alertDialog = dialog.show();
        ListView listView = alertDialog.getListView();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mAdapter.toogleItemChecked(position);
                CheckBox cBox = (CheckBox) view.findViewById(R.id.checkbox);
                cBox.toggle();
            }
        });

        return alertDialog;
    }

    @Override
    public void onClick(DialogInterface dialog, int which, boolean isChecked) {

    }
}
