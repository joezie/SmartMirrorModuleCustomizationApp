package com.example.smartmirrormodulecustomizationapp;

import android.annotation.SuppressLint;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.Map;

class DatabaseHandler implements Runnable {

    // the parent activity that calls this database handler
    private AppCompatActivity parentActivity;

    // the current user
    private final String username;

    // the selected module
    private String moduleName;

    // button position passed from UserActivity
    private int position;
    
    // preparation for database connection
    private final String db_driver;
    private final String db_url;
    private final String db_username;
    private final String db_password;

    // operation types
    private final String optype_read;
    private final String optype_update;
    private final String optype_add;
    private final String optype_remove;
    private final String operationType;

    // passed by parent activity: stores configuration data of each module (field name, config)
    private Map<String, Map<String, Object>> moduleConfigMap;

    // passed by parent activity: stores status of each module (module name, status)
    private Map<String, Boolean> moduleStatusMap;

    // mapping between module name and database table name (module name, table name)
    private Map<String, String> moduleTableMap;


    /**
     * Private common constructor to set common members and constant strings
     *
     * @param parentActivity the parent activity that calls this database handler
     * @param operationType  the database operation type
     * @param username       the current user
     * @param position       the position of parent activity
     */
    private DatabaseHandler(final AppCompatActivity parentActivity, final String operationType,
                    final String username, final int position) {
        this.parentActivity = parentActivity;
        this.operationType = operationType;
        this.username = username;
        this.position = position;

        // preparation for database connection
        db_driver = parentActivity.getResources().getString(R.string.JDBC_DRIVER);
        db_url = parentActivity.getResources().getString(R.string.JDBC_PREFIX) +
                parentActivity.getResources().getString(R.string.DATABASE_URL) + "/" +
                parentActivity.getResources().getString(R.string.DATABASE_NAME);
        db_username = parentActivity.getResources().getString(R.string.DATABASE_USERNAME);
        db_password = parentActivity.getResources().getString(R.string.DATABASE_PASSWORD);

        // operation types
        optype_read = parentActivity.getResources().getString(R.string.read);
        optype_update = parentActivity.getResources().getString(R.string.update);
        optype_add = parentActivity.getResources().getString(R.string.add);
        optype_remove = parentActivity.getResources().getString(R.string.remove);

        // set moduleTableMap
        moduleTableMap = new LinkedHashMap<>();
        initModuleTableMap();
    }

    /**
     * Constructor used for read operation
     *
     * @param parentActivity  the parent activity that calls this database handler
     * @param operationType   the database operation type
     * @param username        the current user
     * @param position        the position of parent activity
     * @param moduleConfigMap an empty module config map passed by parent activity
     * @param moduleStatusMap an empty module status map passed by parent activity
     */
    DatabaseHandler(final AppCompatActivity parentActivity, final String operationType,
                    final String username, final int position,
                    final Map<String, Map<String, Object>> moduleConfigMap,
                    final Map<String, Boolean> moduleStatusMap) {

        // set common members and constant strings
        this(parentActivity, operationType, username, position);

        // set module config and status maps
        this.moduleConfigMap = moduleConfigMap;
        this.moduleStatusMap = moduleStatusMap;

        // set moduleName as undefined
        moduleName = parentActivity.getResources().getString(R.string.undefined);

    }

    /**
     * Constructor used for add, remove, and update operation
     *
     * @param parentActivity the parent activity that calls this database handler
     * @param operationType  the database operation type
     * @param username       the current user
     * @param position       the position of parent activity
     * @param moduleName     the name of elected module
     */
    DatabaseHandler(final AppCompatActivity parentActivity, final String operationType,
                    final String username, final int position, final String moduleName) {

        // set common members and constant strings
        this(parentActivity, operationType, username, position);

        // set selected module
        this.moduleName = moduleName;

        // set maps to null
        this.moduleConfigMap = null;
        this.moduleStatusMap = null;

    }



    /**
     *  Query configuration data of each module and store in moduleConfigMap;
     *  query enabling status of each module and store in moduleStatusMap
     */
    @Override
    public void run() {

        Connection conn = null;
        Statement stmt = null;

        try {

            // establish connection to database
            Class.forName(db_driver);
            conn = DriverManager.getConnection(db_url, db_username, db_password);
            stmt = conn.createStatement();


            // TODO: add, remove, and update operations

            // conduct database operation accoding to operation type
            if (operationType.equals(optype_read))
                readModuleStatusAndConfig(stmt);
            else if (operationType.equals(optype_add))
                addModule(stmt);


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

    /**
     * Query module configuration data from database to set module
     * config and status maps
     *
     * @param stmt       Used for database connection
     * @throws Exception If cannot get data because user is not registered in this table
     */
    private void readModuleStatusAndConfig(final Statement stmt) throws Exception {

        // query enabling status of each module and store in moduleStatusMap
        final String userModuleTable = parentActivity.getResources()
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
                final int modulePos = rs.getInt(parentActivity.getResources().getString(R.string.position));
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
    }

    /**
     * Add a default item to corresponding module table with given username and position;
     * Set corresponding module as enabled in user module table and refresh parent activity
     * with selected module as default module on success
     *
     * @param stmt       Used for database connection
     * @throws Exception If cannot add an item to database for new module
     */
    private void addModule(final Statement stmt) throws Exception {

        final String moduleTable = moduleTableMap.get(moduleName);
        @SuppressLint("DefaultLocale") final String addModuleOperation =
                String.format("INSERT INTO %s (username, position) VALUES (\"%s\", %d)",
                        moduleTable, username, position);

        if (stmt.executeUpdate(addModuleOperation) != 0) {

            // on success, set corresponding module as enabled in user module table
            final String userModuleTable = parentActivity.getResources()
                    .getString(R.string.user_module_table);
            @SuppressLint("DefaultLocale") final String updateModuleStatusOperation =
                    String.format("UPDATE %s SET %s = 1 WHERE (username = \"%s\")",
                            userModuleTable, moduleName, username);
            if (stmt.executeUpdate(updateModuleStatusOperation) == 0) {

                // throws exception on failure
                throw new IllegalStateException(
                        "Failed to set module " + moduleName + " as enabled in database");

            }

            // refresh parent activity with selected module as default module
            parentActivity.finish();
            parentActivity.startActivity(parentActivity
                    .getIntent()
                    .addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                    .putExtra(parentActivity
                            .getResources()
                            .getString(R.string.default_module), moduleName));

        } else {

            // throws exception on failure
            throw new IllegalStateException(
                    "Add new module " + moduleName + " to database failed");

        }
    }



        /**
         * Set moduleTableMap
         */
    private void initModuleTableMap() {

        final String[] moduleNames = parentActivity.getResources().getStringArray(R.array.moduleNames);
        final String[] tableNames = parentActivity.getResources().getStringArray(R.array.tableNames);
        for (int i = 0; i < moduleNames.length; ++i)
            moduleTableMap.put(moduleNames[i], tableNames[i]);

    }

} // End of DatabaseHandler
