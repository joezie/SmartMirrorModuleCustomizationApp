package com.example.smartmirrormodulecustomizationapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

public class UserActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);


        Button logoutBtn = (Button)findViewById(R.id.logoutBtn_user);
        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent startIntent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(startIntent);
            }
        });

        int buttonNumArr[] = {1, 21, 22, 23, 3, 4, 5, 61, 62, 63, 7};
        for (int buttonNum : buttonNumArr) {
            String buttonName = "pos" + buttonNum + "Btn_user";
            ImageButton addModuleBtn = (ImageButton) findViewById(getResources().getIdentifier(buttonName, "id", this.getPackageName()));
            addModuleBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent startIntent = new Intent(getApplicationContext(), AddModuleActivity.class);
                    startActivity(startIntent);
                }
            });
        }
    }
}
