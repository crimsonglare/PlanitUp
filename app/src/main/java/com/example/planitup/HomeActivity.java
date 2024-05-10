package com.example.planitup;

import android.content.Intent;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.planitup.CreateRoomActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

// import statements

public class HomeActivity extends AppCompatActivity {

    private RecyclerView roomRecyclerView;
    private List<String> roomNames;
    private List<String> roomIds;
    private RoomAdapter adapter; // Define adapter as a class field

    private FirebaseAuth mAuth;
    private DatabaseReference roomsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mAuth = FirebaseAuth.getInstance();
        roomsRef = FirebaseDatabase.getInstance().getReference().child("Rooms");

        roomRecyclerView = findViewById(R.id.roomRecyclerView);
        roomRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        roomNames = new ArrayList<>();
        roomIds = new ArrayList<>();
        adapter = new RoomAdapter(roomNames); // Initialize the adapter

        roomRecyclerView.setAdapter(adapter);

        // Set click listeners for icon buttons
        ImageView createRoomIcon = findViewById(R.id.createRoomIcon);
        createRoomIcon.setOnClickListener(v -> createRoom());

        ImageView refreshIcon = findViewById(R.id.refreshIcon);
        refreshIcon.setOnClickListener(v -> loadRooms());

        loadRooms();
    }

    private void loadRooms() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            // User not logged in, redirect to SignInActivity
            startActivity(new Intent(HomeActivity.this, SignInActivity.class));
            finish();
            return;
        }

        String userId = currentUser.getUid();
        roomsRef.orderByChild("members/" + userId).equalTo(true).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                roomNames.clear();
                roomIds.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String roomName = snapshot.child("roomName").getValue(String.class);
                    String roomId = snapshot.getKey();
                    roomNames.add(roomName);
                    roomIds.add(roomId);
                }
                // Notify the adapter that the data set has changed
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
                Toast.makeText(HomeActivity.this, "Failed to load rooms: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void createRoom() {
        // Start CreateRoomActivity
        startActivity(new Intent(HomeActivity.this, CreateRoomActivity.class));
    }

    // RoomAdapter class for RecyclerView
    // RoomAdapter class for RecyclerView
    private class RoomAdapter extends RecyclerView.Adapter<RoomAdapter.ViewHolder> {

        private List<String> roomNames;

        public RoomAdapter(List<String> roomNames) {
            this.roomNames = roomNames;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_room, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            String roomName = roomNames.get(position);
            holder.roomNameTextView.setText(roomName);

            // Set click listener for the item
            holder.itemView.setOnClickListener(v -> {
                String roomId = roomIds.get(position); // Assuming roomIds list is also updated
                // Start RoomActivity with the selected room ID
                Intent intent = new Intent(HomeActivity.this, RoomActivity.class);
                intent.putExtra("roomId", roomId);
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return roomNames.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            public TextView roomNameTextView;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                roomNameTextView = itemView.findViewById(R.id.roomNameTextView);
            }
        }
    }

}
