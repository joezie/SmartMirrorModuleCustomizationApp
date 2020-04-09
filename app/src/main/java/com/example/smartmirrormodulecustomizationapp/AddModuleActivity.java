package com.example.smartmirrormodulecustomizationapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *  Activity for Add Module page, where users can choose the target module through a spinner,
 *  edit configurations of the corresponding module
 */
public class AddModuleActivity extends AppCompatActivity {

    // username passed from UserActivity
    private String username;

    // stores configuration data (field name, config)
    private final Map<String, Object> itemMap = new LinkedHashMap<>();

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
        if (getIntent().hasExtra(getResources().getString(R.string.username))) {
            username = getIntent().getExtras().getString(getResources().getString(R.string.username));
        }

        // set spinner action
        Spinner moduleSpinner = findViewById(R.id.moduleSpinner_module);
        ArrayAdapter<String> moduleArrayAdapter = new ArrayAdapter<>(
                AddModuleActivity.this, android.R.layout.simple_list_item_1,
                getResources().getStringArray(R.array.moduleNames));
        moduleArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        moduleSpinner.setAdapter(moduleArrayAdapter);
/*
        moduleSpinner.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });
*/
        // bind adapter and layout manager to config list
        RecyclerView configListView = findViewById(R.id.moduleConfigList_module);

        // FOR TEST ONLY BEGIN
        itemMap.put("A Boolean", true);
        itemMap.put("An Integer", 233);
        itemMap.put("A String", "skr~");
        // FOR TEST ONLY END

        configListView.setAdapter(new HeteroItemAdapter(itemMap));
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
/*
    private class GetData extends AsyncTask<String, String, String> {

        // Prepare for database connection
        final String db_driver = getResources().getString(R.string.JDBC_DRIVER);
        final String db_url = getResources().getString(R.string.JDBC_PREFIX) +
                getResources().getString(R.string.DATABASE_URL) + "/" +
                getResources().getString(R.string.DATABASE_NAME);
        final String db_username = getResources().getString(R.string.DATABASE_USERNAME);
        final String db_password = getResources().getString(R.string.DATABASE_PASSWORD);


        @Override
        protected void onPreExecute() {
            progressTextView.setText("Connecting to database...");
        }


        @Override
        protected String doInBackground(String... strings) {

            Connection conn = null;
            Statement stmt = null;

            try {
                Class.forName(db_driver);
                conn = DriverManager.getConnection(db_url, db_username, db_password);

                stmt = conn.createStatement();
                String sql = "SELECT * FROM fruits";
                ResultSet rs = stmt.executeQuery(sql);

                while (rs.next()) {
                    String name = rs.getString("name");
                    double price = rs.getDouble("price");

                    fruitsMap.put(name, price);
                }

                rs.close();
                stmt.close();
                conn.close();

            } catch (Exception e) {
                e.printStackTrace();
            }
            finally {

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

            return null;
        }

        @Override
        protected void onPostExecute(String msg) {

            progressTextView.setText(this.msg);

            if (fruitsMap.size() > 0) {

                itemAdapter = new ItemAdapter(thisContext, fruitsMap);
                myListView.setAdapter(itemAdapter);
            }
        }

    } // End of GetData
*/
} // END of AddModuleActivity
