package com.example.planitup;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.firebase.auth.FirebaseAuth;

public class HamburgerActivity extends AppCompatActivity {

    private CardView createRoomButton;

    private CardView joinRoomButton;

    private CardView currentroomsButton;

    private ImageView profileButton;

    private CardView taskButton;



    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hamburger);

        mAuth = FirebaseAuth.getInstance();

        createRoomButton = findViewById(R.id.createRoomButton);
        joinRoomButton = findViewById(R.id.joinRoomButton);

        currentroomsButton = findViewById(R.id.currentRoomButton);
        profileButton = findViewById(R.id.profileButton);


        createRoomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(HamburgerActivity.this, CreateRoomActivity.class));

            }
        });

        joinRoomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HamburgerActivity.this, JoinRoomActivity.class));
            }
        });

        currentroomsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HamburgerActivity.this, HomeActivity.class));
            }
        });

        profileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HamburgerActivity.this, ProfileActivity.class));
            }
        });

        taskButton = findViewById(R.id.taskButton);
        taskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HamburgerActivity.this, TaskActivity.class));
            }
        });



    }
}