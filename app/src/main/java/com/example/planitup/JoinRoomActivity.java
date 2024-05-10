// JoinRoomActivity.java
package com.example.planitup;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class JoinRoomActivity extends AppCompatActivity {

    private EditText roomNameEditText, roomCodeEditText;
    private Button joinRoomButton;

    private FirebaseAuth mAuth;
    private DatabaseReference roomsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_room);

        mAuth = FirebaseAuth.getInstance();
        roomsRef = FirebaseDatabase.getInstance().getReference().child("Rooms");

        roomNameEditText = findViewById(R.id.roomNameEditText);
        roomCodeEditText = findViewById(R.id.roomCodeEditText);
        joinRoomButton = findViewById(R.id.joinRoomButton);

        joinRoomButton.setOnClickListener(v -> joinRoom());
    }

    private void joinRoom() {
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

        roomsRef.orderByChild("roomName").equalTo(roomName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Room room = Room.fromSnapshot(snapshot);
                    if (room.getRoomCode().equals(roomCode)) {
                        // Room found
                        String userId = mAuth.getCurrentUser().getUid();
                        if (room.getHostId().equals(userId)) {
                            // User is the host
                            Toast.makeText(JoinRoomActivity.this, "You are the host of this room", Toast.LENGTH_SHORT).show();
                            return;
                        }


                        roomsRef.child(room.getRoomId()).child("members").child(userId).setValue(true)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(JoinRoomActivity.this, "Joined room: " + room.getRoomName(), Toast.LENGTH_SHORT).show();
                                        finish();
                                    } else {
                                        Toast.makeText(JoinRoomActivity.this, "Failed to join room", Toast.LENGTH_SHORT).show();
                                    }
                                });
                        return;
                    }
                }
                // Room not found
                Toast.makeText(JoinRoomActivity.this, "Room not found or invalid room code", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(JoinRoomActivity.this, "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}
