package com.example.smartmirrormodulecustomizationapp;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

/**
 *  Activity for Login page, where users can login with their username and password by
 *  clicking login button, or create a new account by clicking register button
 */
public class MainActivity extends AppCompatActivity {

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
            @Override
            public void onClick(View v) {
                // get username
                EditText userNameText = findViewById(R.id.userNameText_login);
                final String username = userNameText.getText().toString();

                // pass username to UserActivity
                Intent startIntent = new Intent(getApplicationContext(), UserActivity.class);
                startIntent.putExtra(getResources().getString(R.string.username), username);
                startActivity(startIntent);
            }
        });

    }
}
