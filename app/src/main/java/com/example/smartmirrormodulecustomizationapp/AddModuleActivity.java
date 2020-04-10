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

    // mapping between module name and database table name (module name, table name)
    private final Map<String, String> moduleTableMap = new LinkedHashMap<>();

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

        // set username
        if (getIntent().hasExtra(getResources().getString(R.string.username)) &&
                getIntent().hasExtra(getResources().getString(R.string.pos))) {
            username = Objects.requireNonNull(getIntent().getExtras())
                    .getString(getResources().getString(R.string.username));
            position = getIntent().getExtras().getInt(getResources().getString(R.string.pos));
        }

        // set moduleTableMap
        initModuleTableMap();

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


                // check if this module is enabled by this user
                Boolean moduleStatus = moduleStatusMap.get(selectedModule);
                if (moduleStatus != null && moduleStatus) {

                    final Map<String, Object> configMap = moduleConfigMap.get(selectedModule);

                    if (configMap != null && configMap.size() > 0) {

                        configListView.setAdapter(new HeteroItemAdapter(configMap));
                        return;

                    }

                }

                // display empty config list
                configListView.setAdapter(new HeteroItemAdapter(
                        new LinkedHashMap<String, Object>()
                ));

            }

            /**
             * Display empty config list when nothing is selected
             *
             * @param parent Dummy argument
             */
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

                final RecyclerView configListView = findViewById(R.id.moduleConfigList_module);
                configListView.setAdapter(new HeteroItemAdapter(
                        new LinkedHashMap<String, Object>()
                ));

            }

        });

        // set layout manager of config list
        RecyclerView configListView = findViewById(R.id.moduleConfigList_module);
/*
        // FOR TEST ONLY BEGIN
        itemMap.put("A Boolean", true);
        itemMap.put("An Integer", 233);
        itemMap.put("A String", "skr~");
        // FOR TEST ONLY END
*/
        // configListView.setAdapter(new HeteroItemAdapter(itemMap));
        configListView.setLayoutManager(new LinearLayoutManager(this));



        // set buttons action
        Button backBtn = findViewById(R.id.backBtn_module);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent startIntent = new Intent(getApplicationContext(), UserActivity.class);
                startActivity(startIntent);
            }
        });



    }

    /**
     * Set moduleTableMap
     */
    private void initModuleTableMap() {
        final String[] moduleNames = getResources().getStringArray(R.array.moduleNames);
        final String[] tableNames = getResources().getStringArray(R.array.tableNames);
        for (int i = 0; i < moduleNames.length; ++i)
            moduleTableMap.put(moduleNames[i], tableNames[i]);
    }

    /**
     * Run a thread to query module configuration data from database and set moduleConfigMap
     */
    private void setModuleConfigMapAndModuleStatusMap() {
        GetData retrieveData = new GetData();
        Thread thread = new Thread(retrieveData);
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    private class GetData implements Runnable {

        // Prepare for database connection
        final String db_driver = getResources().getString(R.string.JDBC_DRIVER);
        final String db_url = getResources().getString(R.string.JDBC_PREFIX) +
                getResources().getString(R.string.DATABASE_URL) + "/" +
                getResources().getString(R.string.DATABASE_NAME);
        final String db_username = getResources().getString(R.string.DATABASE_USERNAME);
        final String db_password = getResources().getString(R.string.DATABASE_PASSWORD);

        /**
         *  Query configuration data of each module and store in moduleConfigMap;
         *  query enabling status of each module and store in moduleStatusMap
         */
        @Override
        public void run() {

            Connection conn = null;
            Statement stmt = null;

            try {

                // conduct query to database
                Class.forName(db_driver);
                conn = DriverManager.getConnection(db_url, db_username, db_password);
                stmt = conn.createStatement();


                // query enabling status of each module and store in moduleStatusMap
                final String userModuleTable = getResources()
                        .getString(R.string.user_module_table);
                @SuppressLint("DefaultLocale")
                final String userModuleQuery = String.format(
                        "SELECT * FROM %s WHERE username=\"%s\"", userModuleTable, username
                );
                final ResultSet userModuleResult = stmt.executeQuery(userModuleQuery);
                final ResultSetMetaData userModuleQueryMeta = userModuleResult.getMetaData();

                // store status of each module
                if (userModuleResult.next()) {

                    final int columnCount = userModuleQueryMeta.getColumnCount();

                    // skip column 1 (username) & 2 (password)
                    for (int colNum = 3; colNum <= columnCount; ++colNum) {

                        // get column name and configuration value
                        final String colName = userModuleQueryMeta.getColumnName(colNum);
                        final Boolean enabled = userModuleResult.getBoolean(colNum);

                        // put (module name, status) in moduleStatusMap
                        moduleStatusMap.put(colName, enabled);

                    } // END of inner for block

                } else {

                    throw new IllegalStateException(
                            "User " + username + " unregistered in database");

                } // END of if-else block

                // close result set
                userModuleResult.close();


                // query configuration data for each module and store in moduleConfigMap
                for (final Map.Entry<String, String> moduleTableEntry
                        : moduleTableMap.entrySet()) {

                    final String moduleName = moduleTableEntry.getKey();
                    final String tableName = moduleTableEntry.getValue();

                    // firstly check if this module is enabled
                    Boolean moduleStatus = moduleStatusMap.get(moduleName);
                    if (moduleStatus == null || !moduleStatus)
                        continue;

                    @SuppressLint("DefaultLocale")
                    final String query = String.format(
                            "SELECT * FROM %s WHERE username=\"%s\"", tableName, username
                    );
                    final ResultSet rs = stmt.executeQuery(query);
                    final ResultSetMetaData rsMata = rs.getMetaData();

                    // store data from each column
                    if (rs.next()) {

                        // check if this module is in this position
                        final int modulePos = rs.getInt(getResources().getString(R.string.position));
                        if (modulePos != position) {
                            // update this module as disabled status

                            moduleStatusMap.put(moduleName, false);
                            continue;
                        }

                        final int columnCount = rsMata.getColumnCount();
                        final Map<String, Object> configMap = new LinkedHashMap<>();

                        // skip column 1 (username) & 2 (position)
                        for (int colNum = 3; colNum <= columnCount; ++colNum) {

                            // get column name, column type, and configuration value
                            final String colName = rsMata.getColumnName(colNum);
                            final String colType = rsMata.getColumnTypeName(colNum);
                            Object value;

                            // get value according to column type and column name
                            switch (colType) {
                                // VARCHAR -> String
                                case "VARCHAR":
                                    value = rs.getString(colName);
                                    break;

                                // INT -> Integer
                                case "INT":
                                    value = rs.getInt(colName);
                                    break;

                                // TINYINT -> Boolean
                                case "TINYINT":
                                    value = rs.getBoolean(colName);
                                    break;

                                default:
                                    throw new IllegalStateException(
                                            "Unexpected Column Type: " + colType);
                            }

                            // put (colName, value) in configMap
                            configMap.put(colName, value);

                        } // END of inner for block

                        moduleConfigMap.put(moduleName, configMap);

                    } else {

                        throw new IllegalStateException(
                                "Module " + moduleName + " shown enabled in table "
                                + userModuleTable + " but corresponding item of user "
                                + username + " not found in table " + tableName);

                    } // END of if-else block

                    // close result set
                    rs.close();

                } // END of outer for block

                // close connection to database
                stmt.close();
                conn.close();

            } catch (Exception e) {

                e.printStackTrace();

            } finally {

                // check again to close connection completely

                try {
                    if (stmt != null) {
                        stmt.close();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                try {
                    if (conn != null) {
                        conn.close();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }

            }
        }

    } // End of GetData

} // END of AddModuleActivity
