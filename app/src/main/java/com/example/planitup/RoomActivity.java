package com.example.planitup;

import android.content.Intent;
import android.os.Bundle;

import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.google.firebase.database.ValueEventListener;

public class RoomActivity extends AppCompatActivity {

    private String roomId;
    private FirebaseAuth mAuth;
    private DatabaseReference roomRef;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);

        roomId = getIntent().getStringExtra("roomId");

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        roomRef = FirebaseDatabase.getInstance().getReference().child("Rooms").child(roomId);

        CardView deleteRoomButton = findViewById(R.id.deleteRoomButton);
        deleteRoomButton.setOnClickListener(v -> deleteRoom());

        CardView chatButton = findViewById(R.id.chatButton);
        chatButton.setOnClickListener(v -> openChatActivity());

        CardView backlogButton = findViewById(R.id.backlogButton);
        backlogButton.setOnClickListener(v -> openBacklogActivity());

        CardView membersButton = findViewById(R.id.membersButton);
        membersButton.setOnClickListener(v -> openMembersActivity());




    }

    private void deleteRoom() {
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if the current user is the host
        roomRef.child("hostId").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String hostId = dataSnapshot.getValue(String.class);
                if (currentUser.getUid().equals(hostId)) {
                    // Current user is the host, delete the room
                    roomRef.removeValue().addOnSuccessListener(aVoid -> {
                        Toast.makeText(RoomActivity.this, "Room deleted", Toast.LENGTH_SHORT).show();
                        finish();
                    }).addOnFailureListener(e -> {
                        Toast.makeText(RoomActivity.this, "Failed to delete room", Toast.LENGTH_SHORT).show();
                    });
                } else {
                    // Current user is not the host, remove user from members list
                    roomRef.child("members").child(currentUser.getUid()).removeValue()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(RoomActivity.this, "You left the room", Toast.LENGTH_SHORT).show();
                                finish();
                            }).addOnFailureListener(e -> {
                                Toast.makeText(RoomActivity.this, "Failed to leave room", Toast.LENGTH_SHORT).show();
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(RoomActivity.this, "Failed to check host status: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openChatActivity() {
        Intent intent = new Intent(RoomActivity.this, ChatActivity.class);
        intent.putExtra("roomId", roomId);
        startActivity(intent);
    }

    private void openBacklogActivity() {
        Intent intent = new Intent(RoomActivity.this, BacklogActivity.class);
        intent.putExtra("roomId", roomId);
        startActivity(intent);
    }

    private void openMembersActivity(){
        Intent intent = new Intent(RoomActivity.this, MembersActivity.class);
        intent.putExtra("roomId", roomId);
        startActivity(intent);

    }



}
