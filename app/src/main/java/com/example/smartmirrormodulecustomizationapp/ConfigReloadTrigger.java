package com.example.smartmirrormodulecustomizationapp;

import androidx.appcompat.app.AppCompatActivity;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ConfigReloadTrigger implements Runnable {

    // the user whose config would be generated
    private final String username;

    // connection to host on cloud server
    private final String hostUrl;
    private final int hostPort;

    ConfigReloadTrigger(final AppCompatActivity parentActivity, final String username) {

        this.username = username;

        hostUrl = parentActivity.getResources().getString(R.string.CLOUD_URL);
        hostPort = Integer.parseInt(parentActivity.getResources().getString(R.string.CLOUD_PORT));
        
    }

    /**
     * Send a trigger message to host with username
     */
    @Override
    public void run() {

        Socket socket;
        DataOutputStream outputToHost;
        try {

            socket = new Socket(hostUrl, hostPort);
            outputToHost = new DataOutputStream(socket.getOutputStream());

        }
        catch (IOException e) {

            e.printStackTrace();
            return;

        }

        final int usernameLen = username.length();
        final String lenStr = usernameLen + "\n";

        // send trigger message with username and its length to host
        try {

            outputToHost.writeBytes(lenStr);
            outputToHost.writeBytes(username);
            outputToHost.flush();

        } catch (IOException e) {

            e.printStackTrace();

        }

    }

}
