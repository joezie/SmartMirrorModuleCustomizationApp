package com.example.smartmirrormodulecustomizationapp;

import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class PhotoCollectionTrigger implements Runnable {

    // parent activity that calls this trigger
    private final AppCompatActivity parentActivity;

    // the user to be collected face photos
    private final String username;

    // a flag indicating if the photo collection is triggered successfully
    private boolean triggerSuccessFlag;

    // connection to host on Raspberry Pi
    private final String hostUrl;
    private final int hostPort;

    // refresh header for trigger message
    private final int photoCollectionHeader;

    PhotoCollectionTrigger(final AppCompatActivity parentActivity, final String username) {

        this.parentActivity = parentActivity;
        this.username = username;
        triggerSuccessFlag = false;

        hostUrl = parentActivity.getResources().getString(R.string.PI_URL);
        hostPort = Integer.parseInt(parentActivity.getResources().getString(R.string.PI_PORT));

        photoCollectionHeader = Integer.parseInt(parentActivity.getResources().getString(
                R.string.PHOTO_COLLECTION));

    }

    /**
     * Send a trigger message to host with username, and receive confirmation message from host
     */
    @Override
    public void run() {

        Socket socket;
        DataInputStream inputFromHost;
        DataOutputStream outputToHost;
        try {

            socket = new Socket(hostUrl, hostPort);
            inputFromHost = new DataInputStream(socket.getInputStream());
            outputToHost = new DataOutputStream(socket.getOutputStream());

        }
        catch (IOException e) {

            triggerSuccessFlag = false;
            e.printStackTrace();
            return;

        }

        // send trigger message with PHOTO_COLLECTION header, username, and its length to host
        try {

            outputToHost.writeInt(photoCollectionHeader);
            outputToHost.writeInt(username.length());
            outputToHost.writeBytes(username);
            outputToHost.flush();

        } catch (IOException e) {

            e.printStackTrace();
            return;

        }

        // update progress text view
        parentActivity.runOnUiThread(new Runnable() {

            @Override
            public void run() {

                final TextView progressText = parentActivity.findViewById(
                        R.id.progressText_collection);
                progressText.setText(parentActivity.getResources().getString(
                        R.string.collecting_and_running));

            }
        });

        // wait for confirmation message from host
        try {

            triggerSuccessFlag = inputFromHost.readBoolean();

        } catch (IOException e) {

            e.printStackTrace();

        }

        // update progress text view and trigger alert dialog
        parentActivity.runOnUiThread(new Runnable() {

            @Override
            public void run() {

                final TextView progressText = parentActivity.findViewById(
                        R.id.progressText_collection);

                if (triggerSuccessFlag) {

                    final MyAlertDialog alertDialog = new MyAlertDialog(
                            parentActivity.getResources().getString(R.string.SUCCESS),
                            parentActivity.getResources().getString(
                                    R.string.register_success_message));
                    alertDialog.show(parentActivity.getSupportFragmentManager(),
                            parentActivity.getResources().getString(R.string.register_success_tag));
                    progressText.setText(parentActivity.getResources().getString(
                            R.string.collection_success));

                } else {

                    final MyAlertDialog alertDialog = new MyAlertDialog(
                            parentActivity.getResources().getString(R.string.ERROR),
                            parentActivity.getResources().getString(
                                    R.string.register_failed_message));
                    alertDialog.show(parentActivity.getSupportFragmentManager(),
                            parentActivity.getResources().getString(R.string.register_failed_tag));
                    progressText.setText(parentActivity.getResources().getString(
                            R.string.collection_failed));

                }

            }
        });

    }

}
