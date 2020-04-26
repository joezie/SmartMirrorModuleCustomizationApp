package com.example.smartmirrormodulecustomizationapp;

import androidx.appcompat.app.AppCompatActivity;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ConfigReloadTrigger implements Runnable {

    // the user whose config would be generated
    private final String username;

    // connection to host on cloud server
    private final String hostUrl;
    private final int hostPort;

    // connection to host on raspberry pi
    private final String piUrl;
    private final int piPort;

    // a flag indicating if the config reloading is triggered successfully
    private byte reloadSuccessFlag;

    // refresh header for trigger message
    private final int refreshHeader;
    private final int moduleUpdateHeader;

    // status for config reloading
    private final byte reloadFailedStatus;
    private final byte matchedUserStatus;

    ConfigReloadTrigger(final AppCompatActivity parentActivity, final String username) {

        this.username = username;

        hostUrl = parentActivity.getResources().getString(R.string.CLOUD_URL);
        hostPort = Integer.parseInt(parentActivity.getResources().getString(R.string.CLOUD_PORT));

        piUrl = parentActivity.getResources().getString(R.string.PI_URL);
        piPort = Integer.parseInt(parentActivity.getResources().getString(R.string.PI_PORT));

        refreshHeader = Integer.parseInt(parentActivity.getResources().getString(R.string.REFRESH));
        moduleUpdateHeader = Integer.parseInt(parentActivity.getResources().getString(
                R.string.MODULE_UPDATE));
        reloadFailedStatus = Byte.parseByte(parentActivity.getResources().getString(
                R.string.RELOAD_FAILED));
        matchedUserStatus = Byte.parseByte(parentActivity.getResources().getString(
                R.string.USER_MATCHED));

        reloadSuccessFlag = reloadFailedStatus;
        
    }

    /**
     * Send a message with username to cloud server host to trigger config reloading;
     * on success, send a message to raspberry pi host to trigger page refresh
     */
    @Override
    public void run() {

        Socket socketToCloud, socketToPi;
        DataInputStream inputFromCloud;
        DataOutputStream outputToCloud, outputToPi;

        // build connection with cloud server host
        try {

            socketToCloud = new Socket(hostUrl, hostPort);
            inputFromCloud = new DataInputStream(socketToCloud.getInputStream());
            outputToCloud = new DataOutputStream(socketToCloud.getOutputStream());

        }
        catch (IOException e) {

            e.printStackTrace();
            return;

        }

        final int usernameLen = username.length();
        final String lenStr = usernameLen + "\n", headerStr = moduleUpdateHeader + "\n";

        // send trigger message with MODULE_UPDATE header, username, and its length to host
        try {

            outputToCloud.writeBytes(headerStr);
            outputToCloud.writeBytes(lenStr);
            outputToCloud.writeBytes(username);
            outputToCloud.flush();

        } catch (IOException e) {

            e.printStackTrace();

        }

        //  wait for confirmation message from cloud server host
        try {

            reloadSuccessFlag = inputFromCloud.readByte();

        } catch (IOException e) {

            e.printStackTrace();
            reloadSuccessFlag = reloadFailedStatus;

        }

        // send refresh trigger message to raspberry pi host on success
        if (reloadSuccessFlag == matchedUserStatus) {

            // build connection with raspberry pi host
            try {

                socketToPi = new Socket(piUrl, piPort);
                outputToPi = new DataOutputStream(socketToPi.getOutputStream());

            }
            catch (IOException e) {

                e.printStackTrace();
                reloadSuccessFlag = reloadFailedStatus;
                return;

            }

            // send trigger message with header REFRESH to host
            try {

                outputToPi.writeInt(refreshHeader);
                outputToPi.flush();

            } catch (IOException e) {

                e.printStackTrace();
                reloadSuccessFlag = reloadFailedStatus;

            }

        }

    }

    /**
     * Check if the config reloading is triggered successfully
     *
     * @return the trigger success status
     */
    byte isReloadSuccess() { return reloadSuccessFlag; }

}
