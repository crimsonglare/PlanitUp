package com.example.planitup;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Check if user is signed in (non-null) and update UI accordingly
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // User is signed in, go to HomeActivity
            startActivity(new Intent(MainActivity.this, HamburgerActivity.class));
            finish(); // Prevent going back to MainActivity
        } else {
            // User is not signed in, go to SignInActivity
            startActivity(new Intent(MainActivity.this, SignInActivity.class));
            finish(); // Prevent going back to MainActivity
        }
    }
}
