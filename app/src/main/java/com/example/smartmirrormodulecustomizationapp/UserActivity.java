package com.example.smartmirrormodulecustomizationapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
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
        if (getIntent().hasExtra(getResources().getString(R.string.username))) {
            username = getIntent().getExtras().getString(getResources().getString(R.string.username));
            TextView welcomeText = findViewById(R.id.welcomeText_user);
            welcomeText.setText("Welcome, " + username + "!");
        }

        // set logout button action
        Button logoutBtn = findViewById(R.id.logoutBtn_user);
        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                    // pass username and button position to AddModuleActivity

                    Intent startIntent = new Intent(getApplicationContext(), AddModuleActivity.class);
                    startIntent.putExtra(getResources().getString(R.string.username), username);
                    startIntent.putExtra(getResources().getString(R.string.pos), buttonNum);
                    startActivity(startIntent);
                }
            });
        }
    }
}
