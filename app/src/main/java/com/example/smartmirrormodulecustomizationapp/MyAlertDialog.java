package com.example.smartmirrormodulecustomizationapp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDialogFragment;

import java.util.Objects;

public class MyAlertDialog extends AppCompatDialogFragment {

    // title of the dialog window (default: ERROR)
    private final String title;

    // message to be shown on dialog window
    private final String message;

    // whether or not to refresh page on clicking ok button
    private final boolean refreshOnClick;

    // parent activity that calls this alert window
    private final AppCompatActivity parentActivity;

    // default module if refreshing parent activity
    private final String defaultModule;

    /**
     * Constructor for NOT refresh-on-click window
     */
    MyAlertDialog(final String title, final String message, final boolean refreshOnClick) {

        this.title = title;
        this.message = message;
        this.parentActivity = null;
        this.refreshOnClick = refreshOnClick;
        this.defaultModule = null;

    }

    /**
     * Constructor for refresh-on-click window
     */
    MyAlertDialog(final String title, final String message, final boolean refreshOnClick,
                  final AppCompatActivity parentActivity, final String defaultModule) {

        this.title = title;
        this.message = message;
        this.parentActivity = parentActivity;
        this.refreshOnClick = refreshOnClick;
        this.defaultModule = defaultModule;

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

                        if (refreshOnClick) {

                            // refresh parent activity with selected module as default module
                            assert parentActivity != null;
                            parentActivity.finish();
                            parentActivity.startActivity(parentActivity
                                    .getIntent()
                                    .addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                                    .putExtra(parentActivity
                                            .getResources()
                                            .getString(R.string.default_module), defaultModule));

                        }
                    }
                });
        return builder.create();

    }

}
