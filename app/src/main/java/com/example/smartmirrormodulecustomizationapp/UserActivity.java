package com.example.smartmirrormodulecustomizationapp;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

/**
 *  Activity for Modules Overview page, where displays the modules at each region in the smart
 *  mirror, and users can also choose to edit modules by clicking corresponding buttons
 */
public class UserActivity extends AppCompatActivity {

    // username passed from MainActivity
    private String username;

    /**
     * Set layout, username and actions of buttons
     *
     * @param savedInstanceState The instance state of the activity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // set layout
        setContentView(R.layout.activity_user);

        // set username
        username = getSharedPreferences(getResources()
                        .getString(R.string.user_preference), MODE_PRIVATE)
                        .getString(getResources().getString(R.string.username),
                                getResources().getString(R.string.undefined));

        // set welcome text
        TextView welcomeText = findViewById(R.id.welcomeText_user);
        welcomeText.setText("Welcome, " + username + "!");

        // set logout button action
        Button logoutBtn = findViewById(R.id.logoutBtn_user);
        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ApplySharedPref")
            @Override
            public void onClick(View v) {
                // TODO: real logout logic

                // reset username in preference
                getSharedPreferences(getResources()
                        .getString(R.string.user_preference), MODE_PRIVATE)
                        .edit()
                        .putString(getResources().getString(R.string.username),
                                getResources().getString(R.string.undefined))
                        .commit();

                // jump to login page
                Intent startIntent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(startIntent);
            }
        });

        // set module edit buttons action
        int[] buttonNumArr = {1, 21, 22, 23, 3, 4, 5, 61, 62, 63, 7};
        for (final int buttonNum : buttonNumArr) {
            String buttonName = "pos" + buttonNum + "Btn_user";
            ImageButton addModuleBtn = findViewById(getResources().getIdentifier(buttonName, "id", this.getPackageName()));
            addModuleBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // pass button position to AddModuleActivity

                    Intent startIntent = new Intent(getApplicationContext(), AddModuleActivity.class);
                    // startIntent.putExtra(getResources().getString(R.string.username), username);
                    startIntent.putExtra(getResources().getString(R.string.pos), buttonNum);
                    startActivity(startIntent);
                }
            });
        }
    }
}
