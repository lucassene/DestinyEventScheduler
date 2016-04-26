package com.destiny.event.scheduler.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.destiny.event.scheduler.R;
import com.destiny.event.scheduler.interfaces.FromDialogListener;

public class MyAlertDialog extends DialogFragment {

    private Dialog dialog;

    private FromDialogListener listener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.alert_dialog_layout, null);

        dialogBuilder.setView(v);

        TextView title = (TextView) v.findViewById(R.id.alert_title);
        TextView message = (TextView) v.findViewById(R.id.alert_message);
        Button btnLeave = (Button) v.findViewById(R.id.btn_leave);
        Button btnNevermind = (Button) v.findViewById(R.id.btn_nevermind);

        title.setText(getResources().getString(R.string.leaving));
        message.setText(getResources().getString(R.string.oblivion));

        listener = (FromDialogListener) getActivity();

        btnLeave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onLogoff();
            }
        });

        btnNevermind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog = dialogBuilder.create();

        return dialog;

    }
}
