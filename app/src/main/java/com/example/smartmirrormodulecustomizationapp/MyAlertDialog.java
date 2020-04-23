package com.example.smartmirrormodulecustomizationapp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDialogFragment;

import java.util.Objects;

public class MyAlertDialog extends AppCompatDialogFragment {

    // title of the dialog window (default: ERROR)
    private final String title;

    // message to be shown on dialog window
    private final String message;

    /*
    MyAlertDialog(final String message) {
        this.message = message;
        this.title = Objects.requireNonNull(getActivity()).getResources()
                .getString(R.string.ERROR);
    }
    */

    MyAlertDialog(final String title, final String message) {
        this.title = title;
        this.message = message;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton(Objects.requireNonNull(getActivity())
                                .getResources().getString(R.string.OK),
                        new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        /* Do nothing */
                    }
                });
        return builder.create();

    }

}
