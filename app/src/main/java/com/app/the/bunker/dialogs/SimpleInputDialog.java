package com.app.the.bunker.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.app.the.bunker.R;
import com.app.the.bunker.interfaces.FromDialogListener;
import com.app.the.bunker.utils.StringUtils;

public class SimpleInputDialog extends DialogFragment {

    private String fragment;
    private String title;
    private String posButton;
    private String negButton;
    private EditText input;
    private FromDialogListener listener;
    private String hint;
    private int max;
    private int min;
    private int type;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity(), R.style.DestinyApp_AlertDialog);

        Bundle bundle = getArguments();
        if (bundle != null){
            fragment = bundle.getString("tag");
            title = bundle.getString("title");
            posButton = bundle.getString("yes");
            negButton = bundle.getString("no");
            hint = bundle.getString("hint");
            max = bundle.getInt("max");
            min = bundle.getInt("min");
            type = bundle.getInt("type");
        }

        listener = (FromDialogListener) getFragmentManager().findFragmentByTag(fragment);

        dialog.setTitle(title);
        dialog.setMessage(getString(R.string.enter_value));

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.input_dialog_layout, null);

        dialog.setView(view);

        input = (EditText) view.findViewById(R.id.input);
        input.setHint(hint);

        dialog.setPositiveButton(posButton, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                switch (type){
                    case FromDialogListener.GUARDIAN_TYPE:
                        sendResult(FromDialogListener.GUARDIAN_TYPE);
                        break;
                    case FromDialogListener.LIGHT_TYPE:
                        sendResult(FromDialogListener.LIGHT_TYPE);
                        break;
                }
                dialog.dismiss();
            }
        });

        dialog.setNegativeButton(negButton, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        return dialog.create();
    }

    public void sendResult(int type){
        if (StringUtils.isEmptyOrWhiteSpaces(input.getText().toString())){
            listener.onPositiveClick("",type);
        } else {
            int text = Integer.parseInt(input.getText().toString());
            if (text < min) {
                listener.onPositiveClick(String.valueOf(min),type);
            } else {
                if (text > max){
                    listener.onPositiveClick(String.valueOf(max),type);
                } else {
                    listener.onPositiveClick(input.getText().toString(),type);
                }
            }
        }

    }
}
