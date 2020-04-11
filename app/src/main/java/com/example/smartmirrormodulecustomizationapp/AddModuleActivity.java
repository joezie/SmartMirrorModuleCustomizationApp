package com.example.smartmirrormodulecustomizationapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import java.lang.String;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 *  Activity for Add Module page, where users can choose the target module through a spinner,
 *  edit configurations of the corresponding module
 */
public class AddModuleActivity extends AppCompatActivity {

    // username passed from UserActivity
    private String username;

    // button position passed from UserActivity
    private int position;

    // stores configuration data of each module (field name, config)
    private final Map<String, Map<String, Object>> moduleConfigMap = new LinkedHashMap<>();

    // stores status of each module (module name, status)
    private final Map<String, Boolean> moduleStatusMap = new LinkedHashMap<>();

    /**
     *  Set layout, username, and actions of spinner, config list, and buttons
     *
     * @param savedInstanceState The instance state of the activity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // set layout
        setContentView(R.layout.activity_add_module);

        // set position
        if (getIntent().hasExtra(getResources().getString(R.string.pos))) {

            position = Objects.requireNonNull(getIntent()
                    .getExtras())
                    .getInt(getResources().getString(R.string.pos));

        } else {

            throw new IllegalStateException("Button position information missing");

        }

        // set username
        username = getSharedPreferences(getResources()
                .getString(R.string.user_preference), MODE_PRIVATE)
                .getString(getResources().getString(R.string.username), null);
        if (username == null) {

            throw new IllegalStateException("Username information missing");

        }

        // set moduleConfigMap
        setModuleConfigMapAndModuleStatusMap();

        // set spinner action
        final Spinner moduleSpinner = findViewById(R.id.moduleSpinner_module);
        final ArrayAdapter<String> moduleArrayAdapter = new ArrayAdapter<>(
                AddModuleActivity.this, android.R.layout.simple_list_item_1,
                getResources().getStringArray(R.array.moduleNames));
        moduleArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        moduleSpinner.setAdapter(moduleArrayAdapter);

        moduleSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            /**
             * Display configuration data of the corresponding module
             *
             * @param parent   Dummy argument
             * @param view     Dummy argument
             * @param position Dummy argument
             * @param id       Dummy argument
             */
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                final String selectedModule = moduleSpinner.getSelectedItem().toString();
                final RecyclerView configListView = findViewById(R.id.moduleConfigList_module);
                final Button removeBtn = findViewById(R.id.removeBtn_module);
                final Button updateBtn = findViewById(R.id.updateBtn_module);
                final Button addBtn = findViewById(R.id.addBtn_module);

                // check if this module is enabled by this user
                final Boolean moduleStatus = moduleStatusMap.get(selectedModule);
                if (moduleStatus != null && moduleStatus) {

                    final Map<String, Object> configMap = moduleConfigMap.get(selectedModule);

                    if (configMap != null && configMap.size() > 0) {

                        configListView.setAdapter(new HeteroItemAdapter(configMap));

                        // show remove and update buttons and hide add button
                        removeBtn.setVisibility(View.VISIBLE);
                        updateBtn.setVisibility(View.VISIBLE);
                        addBtn.setVisibility(View.GONE);

                        return;

                    }

                }

                // show add button and hide remove and update buttons, and display
                // empty config list if this module is not enabled
                removeBtn.setVisibility(View.GONE);
                updateBtn.setVisibility(View.GONE);
                addBtn.setVisibility(View.VISIBLE);
                configListView.setAdapter(new HeteroItemAdapter(
                        new LinkedHashMap<String, Object>()
                ));

            }

            /**
             * Display empty config list and hide buttons except for back when nothing is selected
             *
             * @param parent Dummy argument
             */
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

                final RecyclerView configListView = findViewById(R.id.moduleConfigList_module);
                configListView.setAdapter(new HeteroItemAdapter(
                        new LinkedHashMap<String, Object>()
                ));

                final Button removeBtn = findViewById(R.id.removeBtn_module);
                final Button updateBtn = findViewById(R.id.updateBtn_module);
                final Button addBtn = findViewById(R.id.addBtn_module);
                removeBtn.setVisibility(View.GONE);
                updateBtn.setVisibility(View.GONE);
                addBtn.setVisibility(View.GONE);

            }

        });

        // set default module if available
        if (getIntent().hasExtra(getResources().getString(R.string.default_module))) {

            final String defaultModuleName = Objects.requireNonNull(getIntent()
                    .getExtras())
                    .getString(getResources().getString(R.string.default_module));
            final int defaultModulePos = moduleArrayAdapter.getPosition(defaultModuleName);
            moduleSpinner.setSelection(defaultModulePos);

        }

        // set layout manager of config list
        RecyclerView configListView = findViewById(R.id.moduleConfigList_module);
        configListView.setLayoutManager(new LinearLayoutManager(this));

        // set buttons action
        Button backBtn = findViewById(R.id.backBtn_module);
        backBtn.setOnClickListener(new View.OnClickListener() {

            /**
             * Jump back to UserActivity page
             *
             * @param v Dunmmy argument
             */
            @Override
            public void onClick(View v) {
                Intent startIntent = new Intent(getApplicationContext(), UserActivity.class);
                startActivity(startIntent);
            }

        });

        // TODO: set actions for add, remove, and update buttons

        Button addBtn = findViewById(R.id.addBtn_module);
        addBtn.setOnClickListener(new View.OnClickListener() {

            /**
             * TODO: Generate a default item of this module for the user in the database;
             * reload the page with this module as default module
             *
             * @param v Dunmmy argument
             */
            @Override
            public void onClick(View v) {

                final String selectedModule = moduleSpinner.getSelectedItem().toString();
                addNewModule(selectedModule);

            }

        });

    }

    /**
     * Run a thread to query module configuration data from database
     * to set module config and status maps
     */
    private void setModuleConfigMapAndModuleStatusMap() {

        DatabaseHandler handler = new DatabaseHandler(
                this, getResources().getString(R.string.read),
                username, position, moduleConfigMap, moduleStatusMap);
        Thread thread = new Thread(handler);
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    /**
     * Run a thread to generate a default item of this module for the user in the database;
     * reload the page with this module as default module to set module config and status maps
     *
     * @param moduleName the new module to be added to database
     */
    private void addNewModule(final String moduleName) {

        DatabaseHandler handler = new DatabaseHandler(
                this, getResources().getString(R.string.add),
                username, position, moduleName);
        Thread thread = new Thread(handler);
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }


} // END of AddModuleActivity
