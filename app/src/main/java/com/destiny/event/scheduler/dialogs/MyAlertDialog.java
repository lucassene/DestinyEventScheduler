package com.destiny.event.scheduler.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.destiny.event.scheduler.R;
import com.destiny.event.scheduler.fragments.DetailEventFragment;
import com.destiny.event.scheduler.interfaces.FromDialogListener;

public class MyAlertDialog extends DialogFragment {

    private static final String TAG = "MyAlertDialog";

    private Dialog dialog;

    private FromDialogListener listener;
    private FromDialogListener fragmentListener;

    int dialogType;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.alert_dialog_layout, null);

        dialogBuilder.setView(v);

        TextView title = (TextView) v.findViewById(R.id.alert_title);
        TextView message = (TextView) v.findViewById(R.id.alert_message);
        Button btnLeave = (Button) v.findViewById(R.id.btn_leave);
        Button btnNevermind = (Button) v.findViewById(R.id.btn_nevermind);

        Bundle bundle = getArguments();

        if (bundle == null){
            title.setText(getResources().getString(R.string.leaving));
            message.setText(getResources().getString(R.string.oblivion));
        } else {
            title.setText(bundle.getString("title"));
            message.setText(bundle.getString("msg"));
            btnLeave.setText(bundle.getString("posButton"));
        }

        dialogType = bundle.getInt("type");

        listener = (FromDialogListener) getActivity();

        fragmentListener = (FromDialogListener) getFragmentManager().findFragmentById(R.id.content_frame);
        Log.w(TAG, "Fragment Listener: " + fragmentListener);

        btnLeave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (dialogType){
                    case 0:
                        listener.onLogoff();
                        break;
                    case DetailEventFragment.JOIN_DIALOG:
                        fragmentListener.onPositiveClick(null, dialogType);
                        break;
                    case DetailEventFragment.DELETE_DIALOG:
                        fragmentListener.onPositiveClick(null, dialogType);
                        break;
                    case DetailEventFragment.LEAVE_DIALOG:
                        fragmentListener.onPositiveClick(null, dialogType);
                        break;
                }
                dialog.dismiss();
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
