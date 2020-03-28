package com.example.smartmirrormodulecustomizationapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

public class AddModuleActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_module);

        Spinner moduleSpinner = (Spinner)findViewById(R.id.moduleSpinner_module);
        ArrayAdapter<String> moduleArrayAdapter = new ArrayAdapter<String>(
                AddModuleActivity.this, android.R.layout.simple_list_item_1,
                getResources().getStringArray(R.array.moduleNames) );
        moduleArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        moduleSpinner.setAdapter(moduleArrayAdapter);

        Button backBtn = (Button)findViewById(R.id.backBtn_module);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent startIntent = new Intent(getApplicationContext(), UserActivity.class);
                startActivity(startIntent);
            }
        });
    }
}
