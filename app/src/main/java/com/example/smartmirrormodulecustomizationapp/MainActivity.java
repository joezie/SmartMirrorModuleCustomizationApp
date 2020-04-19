package com.example.smartmirrormodulecustomizationapp;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.LinkedList;

/**
 *  Activity for Login page, where users can login with their username and password by
 *  clicking login button, or create a new account by clicking register button
 */
public class MainActivity extends AppCompatActivity {

    // flags indicating whether or not username existed and password matched
    private boolean usernameExisted = false;
    private boolean passwordMatched = false;

    /**
     * set layout and button action
     *
     * @param savedInstanceState The instance state of the activity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // set layout
        setContentView(R.layout.activity_main);

        // set buttons action
        Button registerBtn = findViewById(R.id.registerBtn_login);
        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent startIntent = new Intent(getApplicationContext(), RegisterActivity.class);
                startActivity(startIntent);
            }
        });

        Button loginBtn = findViewById(R.id.loginButton_login);
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ApplySharedPref")
            @Override
            public void onClick(View v) {

                // get username and password
                EditText userNameText = findViewById(R.id.userNameText_login);
                EditText passwordText = findViewById(R.id.passwordText_login);
                final String username = userNameText.getText().toString();
                final String password = passwordText.getText().toString();

                // match username and password with database record
                checkUsernameAndMatchPassword(username, password);

                // window alert when username not existed
                if (!usernameExisted) {

                    MyAlertDialog alertDialog = new MyAlertDialog(
                            getResources().getString(R.string.username_not_existed_message));
                    alertDialog.show(getSupportFragmentManager(),
                            getResources().getString(R.string.username_not_existed_tag));
                    return;

                }

                // window alert when password not matched
                if (!passwordMatched) {

                    MyAlertDialog alertDialog = new MyAlertDialog(
                            getResources().getString(R.string.password_not_matched_message));
                    alertDialog.show(getSupportFragmentManager(),
                            getResources().getString(R.string.password_not_matched_tag));
                    return;

                }

                // set username as globally accessible preference
                getSharedPreferences(getResources()
                        .getString(R.string.user_preference), MODE_PRIVATE)
                        .edit()
                        .putString(getResources().getString(R.string.username), username)
                        .commit();

                Intent startIntent = new Intent(getApplicationContext(), UserActivity.class);
                startActivity(startIntent);

            }
        });

    }

    /**
     * Run a thread to check existence of username and match password of this user in database
     *
     * @param username the username to be checked existence
     * @param password the password to be matched
     */
    private void checkUsernameAndMatchPassword(final String username, final String password) {

        // query database
        DatabaseHandler handler = new DatabaseHandler(
                this, getResources().getString(R.string.match_username_and_password),
                username, password);
        Thread thread = new Thread(handler);
        thread.start();
        try {
            thread.join();

            // set flags
            usernameExisted = handler.isUsernameExisted();
            passwordMatched = handler.isPasswordMatched();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
