package com.example.planitup;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class CreateRoomActivity extends AppCompatActivity {

    private EditText roomNameEditText, roomCodeEditText;
    private Button createRoomButton;

    private FirebaseAuth mAuth;
    private DatabaseReference roomsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_room);

        mAuth = FirebaseAuth.getInstance();
        roomsRef = FirebaseDatabase.getInstance().getReference().child("Rooms");

        roomNameEditText = findViewById(R.id.roomNameEditText);
        roomCodeEditText = findViewById(R.id.roomCodeEditText);
        createRoomButton = findViewById(R.id.createRoomButton);

        createRoomButton.setOnClickListener(v -> createRoom());
    }

    private void createRoom() {
        String roomName = roomNameEditText.getText().toString().trim();
        String roomCode = roomCodeEditText.getText().toString().trim();

        if (TextUtils.isEmpty(roomName)) {
            Toast.makeText(this, "Please enter a room name", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(roomCode)) {
            Toast.makeText(this, "Please enter a room code", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if the room with the same name and code already exists
        roomsRef.orderByChild("roomName").equalTo(roomName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Room existingRoom = snapshot.getValue(Room.class);
                    if (existingRoom != null && existingRoom.getRoomCode().equals(roomCode)) {
                        // Room with the same name and code already exists
                        Toast.makeText(CreateRoomActivity.this, "Room already exists", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                // Room does not exist, create a new room
                String userId = mAuth.getCurrentUser().getUid();
                String roomId = roomsRef.push().getKey();

                Room room = new Room(roomId, roomName, roomCode, userId);
                Map<String, Object> roomValues = room.toMap();

                roomsRef.child(roomId).setValue(roomValues)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                // Add the host's UID to the members list
                                roomsRef.child(roomId).child("members").child(userId).setValue(true);
                                Toast.makeText(CreateRoomActivity.this, "Room created successfully", Toast.LENGTH_SHORT).show();
                                finish();
                            } else {
                                Toast.makeText(CreateRoomActivity.this, "Failed to create room", Toast.LENGTH_SHORT).show();
                            }
                        });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(CreateRoomActivity.this, "Failed to check room existence: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }





}
