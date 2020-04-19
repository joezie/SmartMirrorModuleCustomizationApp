package com.example.smartmirrormodulecustomizationapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import java.lang.String;
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

    // stores configuration data of each module (module name, (column name, value))
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

                        // set adapter based on whether or not this module has integer type field
                        if (hasIntField(configMap)) {

                            // read ranges of integer type fields from database and pass to adapter
                            configListView.setAdapter(new HeteroItemAdapter(
                                    configMap, getRangeMap(selectedModule)));

                        } else {

                            // without integer type field
                            configListView.setAdapter(new HeteroItemAdapter(configMap));

                        }

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

            // set if passed by parent activity

            final String defaultModuleName = Objects.requireNonNull(getIntent()
                    .getExtras())
                    .getString(getResources().getString(R.string.default_module));
            final int defaultModulePos = moduleArrayAdapter.getPosition(defaultModuleName);
            moduleSpinner.setSelection(defaultModulePos);

        } else {

            // otherwise, set default module as one of the enabled modules at this position

            for (final Map.Entry<String, Boolean> moduleStatusEntry : moduleStatusMap.entrySet()) {

                if (moduleStatusEntry.getValue()) {
                    final int defaultModulePos = moduleArrayAdapter.getPosition(
                            moduleStatusEntry.getKey());
                    moduleSpinner.setSelection(defaultModulePos);

                    break;
                }

            }

        }

        // set layout manager of config list
        final RecyclerView configListView = findViewById(R.id.moduleConfigList_module);
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

        // set actions for add, remove, and update buttons
        Button addBtn = findViewById(R.id.addBtn_module);
        addBtn.setOnClickListener(new View.OnClickListener() {

            /**
             * Generate a default item of this module for the user in the database;
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

        Button removeBtn = findViewById(R.id.removeBtn_module);
        removeBtn.setOnClickListener(new View.OnClickListener() {

            /**
             * Remove the given module for the user in the database;
             * reload the page with this module as default module
             *
             * @param v Dunmmy argument
             */
            @Override
            public void onClick(View v) {

                final String selectedModule = moduleSpinner.getSelectedItem().toString();
                removeModule(selectedModule);

            }

        });

        Button updateBtn = findViewById(R.id.updateBtn_module);
        updateBtn.setOnClickListener(new View.OnClickListener() {

            /**
             * Update config of this module for the user in the database;
             * reload the page with this module as default module
             *
             * @param v Dunmmy argument
             */
            @Override
            public void onClick(View v) {

                // read each field from user end according to type and update moduleConfigMap

                final String selectedModule = moduleSpinner.getSelectedItem().toString();
                Map<String, Object> configMap = moduleConfigMap.get(selectedModule);
                assert configMap != null;

                for (int pos = 0; pos < configListView.getChildCount(); ++pos) {

                    RecyclerView.ViewHolder viewHolder =
                            configListView.findViewHolderForAdapterPosition(pos);
                    if (viewHolder instanceof EdittextItemViewHolder) {

                        TextView fieldNameView =
                                ((EdittextItemViewHolder)viewHolder).getFieldName();
                        EditText textInputView =
                                ((EdittextItemViewHolder)viewHolder).getInputText();
                        configMap.put(fieldNameView.getText().toString(),
                                textInputView.getText().toString());

                    } else if (viewHolder instanceof SeekbarItemViewHolder) {

                        TextView fieldNameView =
                                ((SeekbarItemViewHolder)viewHolder).getFieldName();
                        TextView progressView =
                                ((SeekbarItemViewHolder)viewHolder).getProgress();
                        configMap.put(fieldNameView.getText().toString(),
                                Integer.valueOf(progressView.getText().toString()));

                    } else if (viewHolder instanceof SwitchItemViewHolder) {

                        TextView fieldNameView =
                                ((SwitchItemViewHolder)viewHolder).getFieldName();
                        Switch switchView =
                                ((SwitchItemViewHolder)viewHolder).getSwitchButton();
                        configMap.put(fieldNameView.getText().toString(),
                                switchView.isChecked());

                    }

                }

                // update in database
                updateModuleConfig(selectedModule);

            }

        });

    }

    /**
     * Run a thread to query module configuration data from database
     * to set module config and status maps
     */
    private void setModuleConfigMapAndModuleStatusMap() {

        DatabaseHandler handler = new DatabaseHandler(
                this, getResources().getString(R.string.read_status_and_config),
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
                this, getResources().getString(R.string.add_module),
                username, position, moduleName);
        Thread thread = new Thread(handler);
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    /**
     * Run a thread to remove the given module for the user in the database;
     * reload the page with this module as default module to set module config and status maps
     *
     * @param moduleName the module to be removed from database
     */
    private void removeModule(final String moduleName) {

        DatabaseHandler handler = new DatabaseHandler(
                this, getResources().getString(R.string.remove_module),
                username, position, moduleName);
        Thread thread = new Thread(handler);
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    /**
     * Run a thread to update config of this module for the user in the database;
     * reload the page with this module as default module to set module config and status maps
     *
     * @param moduleName the new module whose config are to be updated in database
     */
    private void updateModuleConfig(final String moduleName) {

        DatabaseHandler handler = new DatabaseHandler(
                this, getResources().getString(R.string.update_module_config),
                username, position, moduleName, moduleConfigMap);
        Thread thread = new Thread(handler);
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    /**
     * Run a thread to query ranges of integer fields from database to set range map
     *
     * @param moduleName the module whose integer type fields ranges would be query
     * @return           the range map containing (field name, (min, max))
     */
    private Map<String, Pair<Integer, Integer>> getRangeMap(final String moduleName) {

        Map<String, Pair<Integer, Integer>> rangeMap = new LinkedHashMap<>();

        DatabaseHandler handler = new DatabaseHandler(
                this, getResources().getString(R.string.read_range),
                moduleName, rangeMap);
        Thread thread = new Thread(handler);
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return rangeMap;

    }

    /**
     * Check if the given map has integer type fields
     *
     * @param m the map to be checked
     * @return  whether or not this map contains integer type fields
     */
    private boolean hasIntField(final Map<String, Object> m) {

        // set adapter based on whether or not this module has integer type field
        for (final Object value : m.values()) {
            if (value instanceof Integer) {
                return true;
            }
        }

        return false;
    }


} // END of AddModuleActivity
