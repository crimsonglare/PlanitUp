package com.example.planitup;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BacklogActivity extends AppCompatActivity {

    private String roomId;
    private DatabaseReference roomRef, backlogRef;
    private FirebaseUser currentUser;
    private String hostId;
    private RecyclerView recyclerView;
    private BacklogAdapter adapter;
    private List<Backlog> backlogList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_backlog);

        roomId = getIntent().getStringExtra("roomId");
        EditText backlogEditText = findViewById(R.id.backlogEditText);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        roomRef = FirebaseDatabase.getInstance().getReference().child("Rooms").child(roomId);
        backlogRef = roomRef.child("backlogs");

        roomRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                hostId = dataSnapshot.child("hostId").getValue(String.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(BacklogActivity.this, "Failed to load host information", Toast.LENGTH_SHORT).show();
            }
        });

        ImageView addBacklogButton = findViewById(R.id.addBacklogButton);
        addBacklogButton.setOnClickListener(v -> {
            if (currentUser != null && currentUser.getUid().equals(hostId)) {
                addBacklog();
            } else {
                Toast.makeText(BacklogActivity.this, "Only the host can add backlogs", Toast.LENGTH_SHORT).show();
            }
        });

        recyclerView = findViewById(R.id.backlogsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        backlogList = new ArrayList<>();
        adapter = new BacklogAdapter(backlogList);
        recyclerView.setAdapter(adapter);

        backlogRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                String backlogId = dataSnapshot.getKey();
                String backlogName = dataSnapshot.child("name").getValue(String.class);
                backlogList.add(new Backlog(backlogId, backlogName));
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                // Handle backlog item changes if needed
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                String backlogId = dataSnapshot.getKey();
                // Remove the deleted backlog from the list
                for (int i = 0; i < backlogList.size(); i++) {
                    if (backlogList.get(i).getId().equals(backlogId)) {
                        backlogList.remove(i);
                        break;
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                // Handle moved backlog items if needed
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(BacklogActivity.this, "Failed to load backlogs: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addBacklog() {
        EditText backlogEditText = findViewById(R.id.backlogEditText);
        String backlogName = backlogEditText.getText().toString().trim();

        if (TextUtils.isEmpty(backlogName)) {
            Toast.makeText(this, "Backlog name cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        String backlogId = backlogRef.push().getKey();

        if (backlogId == null) {
            Toast.makeText(this, "Failed to create backlog", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> backlogMap = new HashMap<>();
        backlogMap.put("name", backlogName);
        backlogMap.put("timestamp", ServerValue.TIMESTAMP);
        backlogRef.child(backlogId).setValue(backlogMap);

        backlogEditText.setText("");
    }

    private void openSubtasksActivity(String backlogId, String backlogName) {
        Intent intent = new Intent(BacklogActivity.this, SubtasksActivity.class);
        intent.putExtra("roomId", roomId);
        intent.putExtra("backlogId", backlogId);
        intent.putExtra("backlogName", backlogName);
        startActivity(intent);
    }

    private static class Backlog {
        private String id;
        private String name;

        public Backlog(String id, String name) {
            this.id = id;
            this.name = name;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }
    }

    private class BacklogAdapter extends RecyclerView.Adapter<BacklogAdapter.ViewHolder> {

        private List<Backlog> backlogList;

        public BacklogAdapter(List<Backlog> backlogList) {
            this.backlogList = backlogList;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_backlog, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Backlog backlog = backlogList.get(position);
            holder.backlogNameTextView.setText(backlog.getName());
            holder.itemView.setOnClickListener(v -> openSubtasksActivity(backlog.getId(), backlog.getName()));
            holder.deleteBacklogButton.setOnClickListener(v -> deleteBacklog(backlog.getId()));
        }

        @Override
        public int getItemCount() {
            return backlogList.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public TextView backlogNameTextView;
            public ImageButton deleteBacklogButton;

            public ViewHolder(View itemView) {
                super(itemView);
                backlogNameTextView = itemView.findViewById(R.id.backlogNameTextView);
                deleteBacklogButton = itemView.findViewById(R.id.deleteBacklogButton);
            }
        }
    }

    private void deleteBacklog(String backlogId) {
        if (!currentUser.getUid().equals(hostId)) {
            Toast.makeText(this, "Only the host can delete backlogs", Toast.LENGTH_SHORT).show();
            return;
        }

        backlogRef.child(backlogId).removeValue().addOnSuccessListener(aVoid -> {
            Toast.makeText(this, "Backlog deleted", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to delete backlog", Toast.LENGTH_SHORT).show();
        });
    }
}
