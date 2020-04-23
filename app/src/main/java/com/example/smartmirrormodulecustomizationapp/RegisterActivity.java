package com.example.smartmirrormodulecustomizationapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

/**
 *  Activity for Register page, where users can input username and password for account registration
 */
public class RegisterActivity extends AppCompatActivity {

    /**
     * set layout and buttons action
     *
     * @param savedInstanceState The instance state of the activity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // set layout
        setContentView(R.layout.activity_register);

        // set buttons action
        Button cancelBtn = findViewById(R.id.cancelBtn_register);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent startIntent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(startIntent);
            }
        });

        Button registerBtn = findViewById(R.id.registerBtn_register);
        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // read username and password from user input
                EditText userNameText = findViewById(R.id.userNameText_register);
                EditText passwordText = findViewById(R.id.passwordText_register);
                EditText confirmedPasswordText = findViewById(R.id.confirmPasswordText_register);
                final String username = userNameText.getText().toString();
                final String password = passwordText.getText().toString();
                final String confirmedPassword = confirmedPasswordText.getText().toString();

                // window alert when any of the fields are empty
                if (username.isEmpty() || password.isEmpty() || confirmedPassword.isEmpty()) {

                    MyAlertDialog alertDialog = new MyAlertDialog(
                            getResources().getString(R.string.ERROR),
                            getResources().getString(R.string.fields_empty_message));
                    alertDialog.show(getSupportFragmentManager(),
                            getResources().getString(R.string.fields_empty_tag));
                    return;

                }

                // window alert when username already existed
                if (checkUsername(username)) {

                    MyAlertDialog alertDialog = new MyAlertDialog(
                            getResources().getString(R.string.ERROR),
                            getResources().getString(R.string.username_already_existed_message));
                    alertDialog.show(getSupportFragmentManager(),
                            getResources().getString(R.string.username_already_existed_tag));
                    return;

                }

                // window alert when password not equal to confirmed password
                if (!password.equals(confirmedPassword)) {

                    MyAlertDialog alertDialog = new MyAlertDialog(
                            getResources().getString(R.string.ERROR),
                            getResources().getString(R.string.two_passwords_not_euqal_message));
                    alertDialog.show(getSupportFragmentManager(),
                            getResources().getString(R.string.two_passwords_not_euqal_tag));
                    return;

                }

                // create a new item for this user in user module table
                addUser(username, password);

                // pass username to photo collection activity
                Intent startIntent = new Intent(getApplicationContext(),
                        PhotoCollectionActivity.class);
                startIntent.putExtra(getResources().getString(R.string.username), username);
                startActivity(startIntent);
            }
        });

    }

    /**
     * Run a thread to check existence of username in database
     *
     * @param username The username to be checked existence
     * @return         Whether or not this username is already existed
     */
    private boolean checkUsername(final String username) {

        boolean usernameExisted = false;

        // query database
        DatabaseHandler handler = new DatabaseHandler(
                this, getResources().getString(R.string.check_username),
                username);
        Thread thread = new Thread(handler);
        thread.start();
        try {
            thread.join();

            // set flags
            usernameExisted = handler.isUsernameExisted();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return usernameExisted;

    }

    /**
     * Run a thread to add the user in database
     *
     * @param username The given username to be stored
     * @param password The given password to be stored
     */
    private void addUser(final String username, final String password) {

        DatabaseHandler handler = new DatabaseHandler(
                this, getResources().getString(R.string.add_user),
                username, password);
        Thread thread = new Thread(handler);
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

}
