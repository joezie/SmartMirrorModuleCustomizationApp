package com.example.smartmirrormodulecustomizationapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.Objects;

/**
 *  Activity for Photo Collection page, where users can take photos of their face by clicking button
 */
public class PhotoCollectionActivity extends AppCompatActivity {

    // username passed from RegisterActivity
    private String username;

    /**
     * set layout and button action
     *
     * @param savedInstanceState The instance state of the activity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // set layout
        setContentView(R.layout.activity_photo_collection);

        // set username
        if (getIntent().hasExtra(getResources().getString(R.string.username))) {

            username = Objects.requireNonNull(getIntent().getExtras())
                    .getString(getResources().getString(R.string.username));

        } else {

            throw new IllegalStateException("Username information missing");

        }

        // set go to mirror reminder text
        final TextView reminderText = findViewById(R.id.goToMirrorText_collection);
        reminderText.setText(reminderText.getText()
                .toString()
                .replaceAll(getResources()
                        .getString(R.string.username), username));

        // set button action
        final ImageButton takePhotoBtn = findViewById(R.id.takePhotoBtn_collection);
        takePhotoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                triggerPhotoCollection();

            }
        });

        final Button backBtn = findViewById(R.id.backBtn_collection);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent startIntent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(startIntent);

            }
        });
    }

    /**
     * Run a thread to send a trigger message to host with username
     */
    private void triggerPhotoCollection() {

        final PhotoCollectionTrigger trigger = new PhotoCollectionTrigger(
                this, username);
        final Thread thread = new Thread(trigger);
        thread.start();

    }

}
