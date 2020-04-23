package com.example.smartmirrormodulecustomizationapp;

import android.net.InetAddresses;

import androidx.appcompat.app.AppCompatActivity;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class PhotoCollectionTrigger implements Runnable {

    // the parent activity that calls this trigger
    private final AppCompatActivity parentActivity;

    // the user to be collected face photos
    private final String username;

    // a flag indicating if the photo collection is triggered successfully
    private boolean triggerSuccessFlag;

    // connection to host on Raspberry Pi
    private final String hostUrl;
    private final int hostPort;

    PhotoCollectionTrigger(final AppCompatActivity parentActivity, final String username) {

        this.parentActivity = parentActivity;
        this.username = username;
        triggerSuccessFlag = false;

        hostUrl = parentActivity.getResources().getString(R.string.PI_URL);
        hostPort = Integer.parseInt(parentActivity.getResources().getString(R.string.PI_PORT));


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

        // send trigger message with username and its length to host
        try {

            outputToHost.writeInt(username.length());
            outputToHost.writeBytes(username);
            outputToHost.flush();

        } catch (IOException e) {

            e.printStackTrace();
            return;

        }

        // wait for confirmation message from host
        try {

            triggerSuccessFlag = inputFromHost.readBoolean();

        } catch (IOException e) {

            e.printStackTrace();

        }

    }

    /**
     * Check if the photo collection is triggered successfully
     *
     * @return the trigger success status
     */
    boolean isTriggerSuccess() { return triggerSuccessFlag; }

}
