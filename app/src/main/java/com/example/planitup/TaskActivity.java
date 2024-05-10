package com.example.planitup;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class TaskActivity extends AppCompatActivity {

    private RecyclerView subtasksRecyclerView;
    private List<SubtaskItem> subtaskItems;
    private SubtaskAdapter adapter;

    private FirebaseAuth mAuth;
    private DatabaseReference roomsRef;

    private ImageView reload;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);

        mAuth = FirebaseAuth.getInstance();
        roomsRef = FirebaseDatabase.getInstance().getReference().child("Rooms");

        subtasksRecyclerView = findViewById(R.id.subtasksRecyclerView);
        reload = findViewById(R.id.reloadButton);
        subtasksRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        subtaskItems = new ArrayList<>();
        adapter = new SubtaskAdapter(subtaskItems, new SubtaskAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(String roomId, String backlogId, String subtaskId) {
                // Handle item click here
                Intent intent = new Intent(TaskActivity.this, SubtasksActivity.class);
                intent.putExtra("roomId", roomId);
                intent.putExtra("backlogId", backlogId);
                intent.putExtra("subtaskId", subtaskId);
                startActivity(intent);
            }
        });
        subtasksRecyclerView.setAdapter(adapter);

        loadSubtasks();

        reload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadSubtasks();
            }
        });
    }

    private void loadSubtasks() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(TaskActivity.this, SignInActivity.class));
            finish();
            return;
        }

        String userId = currentUser.getUid();
        Query query = roomsRef.orderByChild("members/" + userId).equalTo(true);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                subtaskItems.clear();
                for (DataSnapshot roomSnapshot : dataSnapshot.getChildren()) {
                    String roomId = roomSnapshot.getKey();
                    String roomName = roomSnapshot.child("roomName").getValue(String.class);
                    for (DataSnapshot backlogSnapshot : roomSnapshot.child("backlogs").getChildren()) {
                        String backlogId = backlogSnapshot.getKey();
                        String backlogName = backlogSnapshot.child("name").getValue(String.class);
                        for (DataSnapshot subtaskSnapshot : backlogSnapshot.child("subtasks").getChildren()) {
                            String subtaskId = subtaskSnapshot.getKey();
                            String subtaskName = subtaskSnapshot.child("name").getValue(String.class);
                            boolean completed = subtaskSnapshot.child("completed").getValue(Boolean.class);
                            if (!completed) {
                                subtaskItems.add(new SubtaskItem(subtaskName, backlogName, roomName, roomId, backlogId, subtaskId));
                            }
                        }
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(TaskActivity.this, "Failed to load subtasks: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private static class SubtaskItem {
        private String subtaskName;
        private String backlogName;
        private String roomName;
        private String roomId;
        private String backlogId;
        private String subtaskId;

        public SubtaskItem(String subtaskName, String backlogName, String roomName, String roomId, String backlogId, String subtaskId) {
            this.subtaskName = subtaskName;
            this.backlogName = backlogName;
            this.roomName = roomName;
            this.roomId = roomId;
            this.backlogId = backlogId;
            this.subtaskId = subtaskId;
        }

        public String getSubtaskName() {
            return subtaskName;
        }

        public String getBacklogName() {
            return backlogName;
        }

        public String getRoomName() {
            return roomName;
        }

        public String getRoomId() {
            return roomId;
        }

        public String getBacklogId() {
            return backlogId;
        }

        public String getSubtaskId() {
            return subtaskId;
        }
    }

    private static class SubtaskAdapter extends RecyclerView.Adapter<SubtaskAdapter.ViewHolder> {
        private List<SubtaskItem> subtaskItems;
        private OnItemClickListener listener;

        public SubtaskAdapter(List<SubtaskItem> subtaskItems, OnItemClickListener listener) {
            this.subtaskItems = subtaskItems;
            this.listener = listener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.task_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            SubtaskItem subtaskItem = subtaskItems.get(position);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(subtaskItem.getRoomId(), subtaskItem.getBacklogId(), subtaskItem.getSubtaskId());
                }
            });
            holder.subtaskNameTextView.setText(subtaskItem.getSubtaskName());
            holder.backlogRoomTextView.setText(subtaskItem.getBacklogName() + " in " + subtaskItem.getRoomName());
        }

        @Override
        public int getItemCount() {
            return subtaskItems.size();
        }

        public static class ViewHolder extends RecyclerView.ViewHolder {
            public TextView subtaskNameTextView;
            public TextView backlogRoomTextView;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                subtaskNameTextView = itemView.findViewById(R.id.subtaskNameTextView);
                backlogRoomTextView = itemView.findViewById(R.id.backlogRoomTextView);
            }
        }

        public interface OnItemClickListener {
            void onItemClick(String roomId, String backlogId, String subtaskId);
        }
    }
}
