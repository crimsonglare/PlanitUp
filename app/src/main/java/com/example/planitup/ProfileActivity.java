package com.example.planitup;



import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.bumptech.glide.Glide;

public class ProfileActivity extends AppCompatActivity {

    private TextView usernameTextView, emailTextView,Name;
    private EditText usernameEditText;
    private Button updateUsernameButton, uploadImageButton;
    private ImageView profileImageView;

    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;
    private StorageReference profileImagesRef;
    private FirebaseUser currentUser;

    private ImageView signOutButton;


    private static final int PICK_IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        profileImagesRef = FirebaseStorage.getInstance().getReference().child("profile_images");


        emailTextView = findViewById(R.id.emailTextView);
        usernameEditText = findViewById(R.id.usernameEditText);
        updateUsernameButton = findViewById(R.id.updateUsernameButton);
        uploadImageButton = findViewById(R.id.uploadImageButton);
        profileImageView = findViewById(R.id.profileImageView);
        signOutButton = findViewById(R.id.signOutButton);
        Name = findViewById(R.id.name);

        currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            // Load the username from the Users node
            usersRef.child(currentUser.getUid()).child("username").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String username = dataSnapshot.getValue(String.class);

                    Name.setText(username);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle error
                    Toast.makeText(ProfileActivity.this, "Failed to load username", Toast.LENGTH_SHORT).show();
                }
            });

            emailTextView.setText(currentUser.getEmail());
        }

        updateUsernameButton.setOnClickListener(v -> updateUsername());
        uploadImageButton.setOnClickListener(v -> uploadImage());

        // Load profile image if exists
        StorageReference userImageRef = profileImagesRef.child(currentUser.getUid() + ".jpg");
        userImageRef.getDownloadUrl().addOnSuccessListener(uri -> {
            // Load the image using Glide
            Glide.with(this)
                    .load(uri)
                    .into(profileImageView);
        }).addOnFailureListener(e -> {
            // Failed to load image
            // Handle error or set a default image
        });

        signOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                startActivity(new Intent(ProfileActivity.this, SignInActivity.class));
                finish();
                Toast.makeText(ProfileActivity.this, "Signed off",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }




    private void updateUsername() {
        String newUsername = usernameEditText.getText().toString().trim();
        if (!newUsername.isEmpty()) {
            currentUser.updateProfile(new UserProfileChangeRequest.Builder()
                    .setDisplayName(newUsername)
                    .build()).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(ProfileActivity.this, "Username updated successfully", Toast.LENGTH_SHORT).show();
                    usernameEditText.setText("");
                    Name.setText(newUsername);

                    // Update username in the Realtime Database
                    usersRef.child(currentUser.getUid()).child("username").setValue(newUsername);
                } else {
                    Toast.makeText(ProfileActivity.this, "Failed to update username", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void uploadImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            StorageReference imageRef = profileImagesRef.child(currentUser.getUid() + ".jpg");
            imageRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        // Image uploaded successfully, update the image view
                        profileImageView.setImageURI(imageUri);

                        // Save the image URL to the Realtime Database under the user's node
                        imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            usersRef.child(currentUser.getUid()).child("profileImageUrl").setValue(uri.toString());
                        });
                    })
                    .addOnFailureListener(e -> {
                        // Handle unsuccessful uploads
                        Toast.makeText(ProfileActivity.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                    });
        }
    }

}




