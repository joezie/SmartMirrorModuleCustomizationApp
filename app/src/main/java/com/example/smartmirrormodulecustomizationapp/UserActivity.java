package com.example.smartmirrormodulecustomizationapp;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *  Activity for Modules Overview page, where displays the modules at each region in the smart
 *  mirror, and users can also choose to edit modules by clicking corresponding buttons
 */
public class UserActivity extends AppCompatActivity {

    // username passed from MainActivity
    private String username;

    // stores enabled modules of each position (pos, list of enabled modules)
    private final Map<Integer, List<String>> posModulesMap = new LinkedHashMap<>();

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
        final TextView welcomeText = findViewById(R.id.welcomeText_user);
        welcomeText.setText(welcomeText.getText()
                .toString()
                .replaceAll(getResources()
                        .getString(R.string.username), username));

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
        int[] posArr = {1, 21, 22, 23, 3, 4, 5, 61, 62, 63, 7};
        for (final int buttonNum : posArr) {

            String buttonName = "pos" + buttonNum + "Btn_user";
            ImageButton addModuleBtn = findViewById(getResources()
                    .getIdentifier(buttonName, "id", this.getPackageName()));
            addModuleBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // pass button position to AddModuleActivity

                    Intent startIntent = new Intent(
                            getApplicationContext(), AddModuleActivity.class);
                    startIntent.putExtra(getResources().getString(R.string.pos), buttonNum);
                    startActivity(startIntent);
                }
            });
        }

        // set module status map by reading database
        setPosModulesMap(posArr);

        // set module overview of each position
        for (final Map.Entry<Integer, List<String>> posModulesEntry : posModulesMap.entrySet()) {

            final int pos = posModulesEntry.getKey();
            final List<String> modules = posModulesEntry.getValue();
            final String posName = "pos" + pos + "Text_user";
            final TextView overviewText = findViewById(getResources()
                    .getIdentifier(posName, "id", this.getPackageName()));

            if (modules.isEmpty()) {

                // set overview description to 'EMPTY' in absence of modules
                overviewText.setText(getResources().getString(R.string.EMPTY));

            } else {

                // otherwise, set overview description to enabled module names
                final StringBuilder overviewEditor = new StringBuilder();
                final int moduleCount = modules.size();
                for (int i = 0; i < moduleCount - 1; ++i)
                    overviewEditor.append(modules.get(i)).append(";");
                overviewEditor.append(modules.get(moduleCount - 1));
                overviewText.setText(overviewEditor.toString());

            }

        }

    }

    /**
     * Run a thread to query enabling status of each module from database
     * to set position modules map
     */
    private void setPosModulesMap(final int[] posArr) {

        // insert of each position into posModulesMap
        for (final int pos : posArr)
            posModulesMap.put(pos, new LinkedList<String>());

        // query database and store in posModulesMap
        DatabaseHandler handler = new DatabaseHandler(
                this, getResources().getString(R.string.read_pos_modules),
                posModulesMap, username);
        Thread thread = new Thread(handler);
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
