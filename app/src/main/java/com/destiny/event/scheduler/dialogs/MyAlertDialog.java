package com.destiny.event.scheduler.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.destiny.event.scheduler.R;
import com.destiny.event.scheduler.interfaces.FromDialogListener;

public class MyAlertDialog extends DialogFragment {

    private static final String TAG = "MyAlertDialog";

    public static final int LEAVE_DIALOG = 10;
    public static final int DELETE_DIALOG = 11;
    public static final int JOIN_DIALOG = 12;
    public static final int ALERT_DIALOG = 13;
    public static final int CONFIRM_DIALOG = 14;

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
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity(), R.style.DestinyApp_AlertDialog);

        String title = "";
        String message = "";
        String posButton = "";
        String negButton = "";

        Bundle bundle = getArguments();

        if (bundle != null) {

            switch (bundle.getInt("type")){
                case 0:
                    title = getResources().getString(R.string.leaving);
                    message = getResources().getString(R.string.oblivion);
                    posButton = getResources().getString(R.string.leave);
                    negButton = getResources().getString(R.string.cancel);
                    break;
                case ALERT_DIALOG:
                    title = (bundle.getString("title"));
                    message = (bundle.getString("msg"));
                    posButton = bundle.getString("posButton");
                    negButton = bundle.getString("negButton");
                    break;
                default:
                    title = (bundle.getString("title"));
                    message = (bundle.getString("msg"));
                    posButton = (bundle.getString("posButton"));
                    negButton = getResources().getString(R.string.cancel);
                    break;
            }

            dialogType = bundle.getInt("type");

        }

        listener = (FromDialogListener) getActivity();

        Fragment fragment = getFragmentManager().findFragmentById(R.id.content_frame);

        if (fragment instanceof FromDialogListener){
            fragmentListener = (FromDialogListener) getFragmentManager().findFragmentById(R.id.content_frame);
        }
        //Log.w(TAG, "Fragment Listener: " + fragmentListener);

        dialogBuilder.setTitle(title)
                .setMessage(message)
                .setPositiveButton(posButton, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (dialogType){
                            case 0:
                                listener.onLogoff();
                                break;
                            case JOIN_DIALOG:
                                fragmentListener.onPositiveClick(null, dialogType);
                                break;
                            case DELETE_DIALOG:
                                fragmentListener.onPositiveClick(null, dialogType);
                                break;
                            case LEAVE_DIALOG:
                                fragmentListener.onPositiveClick(null, dialogType);
                                break;
                            case ALERT_DIALOG:
                                listener.onPositiveClick(null, dialogType);
                                break;
                            case CONFIRM_DIALOG:
                                fragmentListener.onPositiveClick(null, dialogType);
                                break;
                        }
                        dialog.dismiss();
                    }
                });

                if (negButton != null && !negButton.isEmpty()){
                    dialogBuilder.setNegativeButton(negButton, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                }

        dialog = dialogBuilder.create();

        return dialog;

    }

    @Override
    public void show(FragmentManager manager, String tag) {
        try {
            FragmentTransaction ft = manager.beginTransaction();
            ft.add(this, tag);
            ft.commit();
        } catch (IllegalStateException e){
            Log.e(TAG, "State loss Exception");
        }
    }


}
