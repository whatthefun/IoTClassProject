package com.example.user.iotclassproject;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.EditText;

/**
 * Created by Yuan on 2017/6/21.
 */

public class MyDialog extends DialogFragment {

    private DialogListener mListener;

    public interface DialogListener {
        public void onDialogPositiveClick(String username);
    }

    @Override public Dialog onCreateDialog(Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_add, null);

        final EditText edtUsername = (EditText)view.findViewById(R.id.edtUsername);

        builder.setView(view)
            .setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
                @Override public void onClick(DialogInterface dialog, int which) {
                    mListener.onDialogPositiveClick(edtUsername.getText().toString());
                }
            })
            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override public void onClick(DialogInterface dialog, int which) {
                    MyDialog.this.getDialog().cancel();
                }
            });

        return builder.create();
    }
}
