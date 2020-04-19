package com.example.smartmirrormodulecustomizationapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.util.Pair;

import androidx.appcompat.app.AppCompatActivity;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

class DatabaseHandler implements Runnable {

    // the parent activity that calls this database handler
    private AppCompatActivity parentActivity;

    // the current user
    private final String username;

    // the selected module
    private String moduleName;

    // button position
    private int position;

    // user password to be matched
    private String loginPassword;

    // a flag indicating whether or not the login password is matched
    private boolean passwordMatched;

    // a flag indicating whether or not the username exists
    private boolean usernameExisted;
    
    // preparation for database connection
    private final String db_driver;
    private final String db_url;
    private final String db_username;
    private final String db_password;

    // operation types
    private final String optype_readStatusAndConfig;
    private final String optype_readStatus;
    private final String optype_readPosModules;
    private final String optype_readRange;
    private final String optype_matchUsernameAndPassword;
    private final String optype_checkUsername;
    private final String optype_addUser;
    private final String optype_updateModuleConfig;
    private final String optype_addModule;
    private final String optype_removeModule;
    private final String operationType;

    // passed by parent activity: stores configuration data of each module (field name, config)
    private Map<String, Map<String, Object>> moduleConfigMap;

    // passed by parent activity: stores status of each module (module name, status)
    private Map<String, Boolean> moduleStatusMap;

    // passed by parent activity: stores range of integer type field (field name, (min, max))
    private Map<String, Pair<Integer, Integer>> rangeMap;

    // passed by parent activity: stores enabled modules of each position (pos, enabled modules)
    private Map<Integer, List<String>> posModulesMap;

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
        optype_readStatusAndConfig = parentActivity.getResources()
                .getString(R.string.read_status_and_config);
        optype_readStatus = parentActivity.getResources()
                .getString(R.string.read_status);
        optype_readPosModules = parentActivity.getResources()
                .getString(R.string.read_pos_modules);
        optype_readRange = parentActivity.getResources()
                .getString(R.string.read_range);
        optype_matchUsernameAndPassword = parentActivity.getResources()
                .getString(R.string.match_username_and_password);
        optype_checkUsername = parentActivity.getResources()
                .getString(R.string.check_username);
        optype_updateModuleConfig = parentActivity.getResources()
                .getString(R.string.update_module_config);
        optype_addModule = parentActivity.getResources()
                .getString(R.string.add_module);
        optype_removeModule = parentActivity.getResources()
                .getString(R.string.remove_module);
        optype_addUser = parentActivity.getResources()
                .getString(R.string.add_user);

        // set moduleTableMap
        moduleTableMap = new LinkedHashMap<>();
        initModuleTableMap();

        // set all other maps to null by default
        moduleConfigMap = null;
        moduleStatusMap = null;
        posModulesMap = null;
        rangeMap = null;

        // set moduleName and login password as undefined by default
        moduleName = parentActivity.getResources().getString(R.string.undefined);
        loginPassword = parentActivity.getResources().getString(R.string.undefined);

        // set password matched and username existed as false by default
        passwordMatched = false;
        usernameExisted = false;

    }

    /**
     * Constructor used for read status and config operation
     *
     * @param parentActivity  the parent activity that calls this database handler
     * @param operationType   the database operation type
     * @param username        the current user
     * @param position        the position of parent activity
     * @param moduleConfigMap an empty module config map passed by parent activity
     * @param moduleStatusMap an empty module status map passed by parent activity
     */
    @SuppressLint("Assert")
    DatabaseHandler(final AppCompatActivity parentActivity, final String operationType,
                    final String username, final int position,
                    final Map<String, Map<String, Object>> moduleConfigMap,
                    final Map<String, Boolean> moduleStatusMap) {

        // set common members and constant strings
        this(parentActivity, operationType, username, position);

        // check operation type
        assert operationType.equals(optype_readStatusAndConfig);

        // set module config and status maps
        this.moduleConfigMap = moduleConfigMap;
        this.moduleStatusMap = moduleStatusMap;

    }

    /**
     * Constructor used for add and remove module operations
     *
     * @param parentActivity the parent activity that calls this database handler
     * @param operationType  the database operation type
     * @param username       the current user
     * @param position       the position of parent activity
     * @param moduleName     the name of selected module
     */
    @SuppressLint("Assert")
    DatabaseHandler(final AppCompatActivity parentActivity, final String operationType,
                    final String username, final int position, final String moduleName) {

        // set common members and constant strings
        this(parentActivity, operationType, username, position);

        // check operation type
        assert operationType.equals(optype_addModule) || operationType.equals(optype_removeModule);

        // set selected module
        this.moduleName = moduleName;

    }

    /**
     * Constructor used for update module config operation
     *
     * @param parentActivity  the parent activity that calls this database handler
     * @param operationType   the database operation type
     * @param username        the current user
     * @param position        the position of parent activity
     * @param moduleName      the name of selected module
     * @param moduleConfigMap the module config map passed by parent activity
     */
    @SuppressLint("Assert")
    DatabaseHandler(final AppCompatActivity parentActivity, final String operationType,
                    final String username, final int position, final String moduleName,
                    final Map<String, Map<String, Object>> moduleConfigMap) {

        // set common members and constant strings
        this(parentActivity, operationType, username, position);

        // check operation type
        assert operationType.equals(optype_updateModuleConfig);

        // set selected module
        this.moduleName = moduleName;

        // set module config map
        this.moduleConfigMap = moduleConfigMap;

    }

    /**
     * Constructor used for read range operation
     *
     * @param parentActivity  the parent activity that calls this database handler
     * @param operationType   the database operation type
     * @param moduleName      the name of selected module
     * @param rangeMap        the empty range map passed by parent activity
     */
    @SuppressLint("Assert")
    DatabaseHandler(final AppCompatActivity parentActivity, final String operationType,
                    final String moduleName, final Map<String, Pair<Integer, Integer>> rangeMap) {

        // set common members and constant strings
        this(parentActivity, operationType, null, -1);

        // check operation type
        assert operationType.equals(optype_readRange);

        // set selected module
        this.moduleName = moduleName;

        // set range map
        this.rangeMap = rangeMap;

    }

    /**
     * Constructor used for read position modules operation
     *
     * @param parentActivity  the parent activity that calls this database handler
     * @param operationType   the database operation type
     * @param posModulesMap   an empty position modules map passed by parent activity
     * @param username        the current user
     */
    @SuppressLint("Assert")
    DatabaseHandler(final AppCompatActivity parentActivity, final String operationType,
                    final Map<Integer, List<String>> posModulesMap, final String username) {

        // set common members and constant strings
        this(parentActivity, operationType, username, -1);

        // check operation type
        assert operationType.equals(optype_readPosModules);

        // set position modules and module status map
        this.posModulesMap = posModulesMap;
        moduleStatusMap = new LinkedHashMap<>();

    }

    /**
     * Constructor used for match username and password operation and add user operation
     *
     * @param parentActivity the parent activity that calls this database handler
     * @param operationType  the database operation type
     * @param username       the username to be checked
     * @param loginPassword  the login password to be matched
     */
    @SuppressLint("Assert")
    DatabaseHandler(final AppCompatActivity parentActivity, final String operationType,
                    final String username, final String loginPassword) {

        // set common members and constant strings
        this(parentActivity, operationType, username, -1);

        // check operation type
        assert operationType.equals(optype_matchUsernameAndPassword)
                || operationType.equals(optype_addUser);

        // set login password
        this.loginPassword = loginPassword;

    }

    /**
     * Constructor used for check username existence operation
     *
     * @param parentActivity the parent activity that calls this database handler
     * @param operationType  the database operation type
     * @param username       the username to be checked
     */
    @SuppressLint("Assert")
    DatabaseHandler(final AppCompatActivity parentActivity, final String operationType,
                    final String username) {

        // set common members and constant strings
        this(parentActivity, operationType, username, -1);

        // check operation type
        assert operationType.equals(optype_checkUsername);

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

            // conduct database operation according to operation type
            if (operationType.equals(optype_readStatusAndConfig)) {
                readModuleStatus(stmt);
                readModuleConfig(stmt);
            } else if (operationType.equals(optype_readStatus)) {
                readModuleStatus(stmt);
            } else if (operationType.equals(optype_readPosModules)) {
                readModuleStatus(stmt);
                readPositionModules(stmt);
            } else if (operationType.equals(optype_addModule)) {
                addModule(stmt);
            } else if (operationType.equals(optype_removeModule)) {
                removeModule(stmt);
            } else if (operationType.equals(optype_updateModuleConfig)) {
                updateModuleConfig(stmt);
            } else if (operationType.equals(optype_readRange)) {
                readRanges(stmt);
            } else if (operationType.equals(optype_matchUsernameAndPassword)) {
                checkUsernameExistence(stmt);
                matchPassword(stmt);
            } else if (operationType.equals(optype_checkUsername)) {
                checkUsernameExistence(stmt);
            } else if (operationType.equals(optype_addUser)) {
                addUser(stmt);
            } else {
                throw new IllegalStateException(
                        "Unsupported operation type " + operationType);
            }

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
     * Query enabling status of each module from database to set module status map
     *
     * @param stmt          Used for database connection
     * @throws SQLException If cannot get data because user is not registered in this table
     */
    private void readModuleStatus(final Statement stmt) throws SQLException {

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

    }

    /**
     * Query configuration data for each module from database to set module config map
     *
     * @param stmt          Used for database connection
     * @throws SQLException If module shown enabled in user module table but item of this
     *                      user not found in corresponding table
     */
    private void readModuleConfig(final Statement stmt) throws SQLException {

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
                final int modulePos = rs.getInt(parentActivity
                        .getResources().getString(R.string.position));
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

                final String userModuleTable = parentActivity.getResources()
                        .getString(R.string.user_module_table);
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
     * @param stmt          Used for database connection
     * @throws SQLException If cannot add an item to database for new module
     */
    private void addModule(final Statement stmt) throws SQLException {

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
     * Remove the item of given username and position from the corresponding module table;
     * Set corresponding module as disabled in user module table and refresh parent activity
     * with selected module as default module on success
     *
     * @param stmt          Used for database connection
     * @throws SQLException If cannot remove the item of the chosen module from database
     */
    private void removeModule(final Statement stmt) throws SQLException {

        final String moduleTable = moduleTableMap.get(moduleName);
        @SuppressLint("DefaultLocale") final String removeModuleOperation =
                String.format("DELETE FROM %s WHERE (username=\"%s\" AND position=%d)",
                        moduleTable, username, position);

        if (stmt.executeUpdate(removeModuleOperation) != 0) {

            // on success, set corresponding module as disabled in user module table
            final String userModuleTable = parentActivity.getResources()
                    .getString(R.string.user_module_table);
            @SuppressLint("DefaultLocale") final String updateModuleStatusOperation =
                    String.format("UPDATE %s SET %s = 0 WHERE (username = \"%s\")",
                            userModuleTable, moduleName, username);
            if (stmt.executeUpdate(updateModuleStatusOperation) == 0) {

                // throws exception on failure
                throw new IllegalStateException(
                        "Failed to set module " + moduleName + " as disabled in database");

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
                    "Remove module " + moduleName + " from database failed");

        }
    }

    /**
     * Update config of the item in corresponding module table with given username and position;
     * Refresh parent activity with selected module as default module on success
     *
     * @param stmt          Used for database connection
     * @throws SQLException If cannot update config of given module in database
     */
    private void updateModuleConfig(final Statement stmt) throws SQLException {

        final StringBuilder configSetter = new StringBuilder();
        final Map<String, Object> configMap = moduleConfigMap.get(moduleName);
        if (configMap == null)
            throw new IllegalStateException(
                "Config map of " + moduleName + " is null");

        // store config key-value pairs
        int configCnt = 0;
        final int configMapSize = configMap.size();
        for (final Map.Entry<String, Object> configEntry : configMap.entrySet()) {

            // transform config value based on its type
            final String configName= configEntry.getKey();
            final Object configValue= configEntry.getValue();
            String configValueString;
            if (configValue instanceof Boolean) {
                configValueString = (Boolean)configValue ? "1" : "0";
            } else if (configValue instanceof Integer || configValue instanceof String) {
                configValueString = configValue.toString();
            } else {
                throw new IllegalStateException(
                        "Unsupported config value type of " + configName);
            }

            configSetter.append("`")
                    .append(configName)
                    .append("`=\"")
                    .append(configValueString)
                    .append("\"");

            if (configCnt < configMapSize - 1)
                configSetter.append(", ");

            ++configCnt;

        }

        // conduct update operation
        final String moduleTable = moduleTableMap.get(moduleName);
        @SuppressLint("DefaultLocale") final String updateConfigOperation =
                String.format("UPDATE %s SET %s WHERE (username=\"%s\" AND position=%d)",
                        moduleTable, configSetter.toString(), username, position);

        if (stmt.executeUpdate(updateConfigOperation) != 0) {

            // on success, refresh parent activity with selected module as default module
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
                    "Update module config of " + moduleName + " in database failed");

        }
    }

    /**
     * Query integer type field range data from database to set range map
     *
     * @param stmt          Used for database connection
     * @throws SQLException If range map is empty after database query
     */
    private void readRanges(final Statement stmt) throws SQLException {

        final String rangeTable = parentActivity
                .getResources()
                .getString(R.string.range_table);
        final String moduleTableName = moduleTableMap.get(moduleName);

        // query ranges for integer type fields in this module
        @SuppressLint("DefaultLocale")
        final String query = String.format(
                "SELECT * FROM %s WHERE tableName=\"%s\"", rangeTable, moduleTableName
        );
        final ResultSet rs = stmt.executeQuery(query);

        // store ranges of each field
        while (rs.next()) {

            final String fieldName = rs.getString(parentActivity
                    .getResources()
                    .getString(R.string.fieldName));
            final int rangeMin = rs.getInt(parentActivity
                    .getResources()
                    .getString(R.string.min));
            final int rangeMax = rs.getInt(parentActivity
                    .getResources()
                    .getString(R.string.max));
            rangeMap.put(fieldName, new Pair<>(rangeMin, rangeMax));

        }

        // close result set
        rs.close();

        if (rangeMap.isEmpty()) {

            // throws exception if rangeMap is empty
            throw new IllegalStateException("Range map is empty but it shouldn't be");

        }

    }

    /**
     * Query enabled modules for each position from database to set position modules map
     *
     * @param stmt          Used for database connection
     * @throws SQLException If module shown enabled in user module table but item of this
     *                      user not found in corresponding table
     */
    private void readPositionModules(final Statement stmt) throws SQLException {

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

            // store module in entry of corresponding position
            if (rs.next()) {

                final int modulePos = rs.getInt(parentActivity
                        .getResources().getString(R.string.position));
                Objects.requireNonNull(posModulesMap.get(modulePos)).add(moduleName);

            } else {

                final String userModuleTable = parentActivity.getResources()
                        .getString(R.string.user_module_table);
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
     * Match password of this user in database
     *
     * @param stmt          Used for database connection
     * @throws SQLException If meet database connection error
     */
    private void matchPassword(final Statement stmt) throws SQLException {

        final String userModuleTable = parentActivity.getResources()
                .getString(R.string.user_module_table);
        final String matched = parentActivity.getResources().getString(R.string.matched);

        // match login password in database
        @SuppressLint("DefaultLocale")
        final String matchPasswordQuery = String.format(
                "SELECT count(*) AS %s FROM %s WHERE username=\"%s\" AND `password`=\"%s\"",
                matched, userModuleTable, username, loginPassword
        );
        final ResultSet matchPasswordResult = stmt.executeQuery(matchPasswordQuery);

        // set API accessible variable passwordMatched
        passwordMatched = matchPasswordResult.next() &&
                matchPasswordResult.getInt(matched) == 1;

        // close result set
        matchPasswordResult.close();

    }

    /**
     * Check existence of username in database
     *
     * @param stmt          Used for database connection
     * @throws SQLException If meet database connection error
     */
    private void checkUsernameExistence(final Statement stmt) throws SQLException {

        final String userModuleTable = parentActivity.getResources()
                .getString(R.string.user_module_table);
        final String existence = parentActivity.getResources().getString(R.string.existence);

        // first check existence of this user
        @SuppressLint("DefaultLocale")
        final String userExistenceQuery = String.format(
                "SELECT count(*) AS %s FROM %s WHERE username=\"%s\"",
                existence, userModuleTable, username
        );
        final ResultSet userExistenceResult = stmt.executeQuery(userExistenceQuery);

        // set API accessible variable usernameExisted
        usernameExisted = userExistenceResult.next() &&
                userExistenceResult.getInt(existence) == 1;

        // close result set
        userExistenceResult.close();

    }

    /**
     * Add an item with all modules disabled to user module table with given username and password
     *
     * @param stmt          Used for database connection
     * @throws SQLException If cannot add an item to database for new module
     */
    private void addUser(final Statement stmt) throws SQLException {

        final String userModuleTable = parentActivity.getResources()
                .getString(R.string.user_module_table);
        @SuppressLint("DefaultLocale") final String addUserOperation =
                String.format("INSERT INTO %s (`username`, `password`) VALUES (\"%s\", \"%s\")",
                        userModuleTable, username, loginPassword);

        if (stmt.executeUpdate(addUserOperation) == 0) {

            // throws exception on failure
            throw new IllegalStateException(
                    "Add new user " + username + " to database failed");

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

    /**
     * Get password match result
     *
     * @return Whether or not the login password is matched
     */
    boolean isPasswordMatched() { return passwordMatched; }

    /**
     * Get username existence check result
     *
     * @return Whether or not the username is existed
     */
    boolean isUsernameExisted() { return usernameExisted; }

} // End of DatabaseHandler
