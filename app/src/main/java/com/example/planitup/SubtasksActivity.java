package com.example.planitup;

import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
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

public class SubtasksActivity extends AppCompatActivity {

    private String roomId, backlogId;
    private DatabaseReference backlogRef, subtasksRef;
    private FirebaseUser currentUser;
    private RecyclerView recyclerView;
    private SubtasksAdapter adapter;
    private List<Subtask> subtaskList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subtasks);

        roomId = getIntent().getStringExtra("roomId");
        backlogId = getIntent().getStringExtra("backlogId");

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        backlogRef = FirebaseDatabase.getInstance().getReference().child("Rooms").child(roomId).child("backlogs").child(backlogId);
        subtasksRef = backlogRef.child("subtasks");

        EditText subtaskEditText = findViewById(R.id.subtaskEditText);
        ImageView addSubtaskButton = findViewById(R.id.addSubtaskButton);
        recyclerView = findViewById(R.id.recyclerView);

        subtaskList = new ArrayList<>();
        adapter = new SubtasksAdapter(subtaskList);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        addSubtaskButton.setOnClickListener(v -> addSubtask(subtaskEditText.getText().toString().trim()));

        // Listen for new subtasks
        subtasksRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                String subtaskId = dataSnapshot.getKey();
                String subtaskName = dataSnapshot.child("name").getValue(String.class);
                boolean completed = dataSnapshot.child("completed").getValue(Boolean.class);
                Subtask subtask = new Subtask(subtaskId, subtaskName, completed);
                subtaskList.add(subtask);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                // Handle subtask changes if needed
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                // Handle removed subtasks if needed
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                // Handle moved subtasks if needed
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(SubtasksActivity.this, "Failed to load subtasks: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addSubtask(String subtaskName) {
        if (TextUtils.isEmpty(subtaskName)) {
            Toast.makeText(this, "Subtask name cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }
        DatabaseReference roomRef = FirebaseDatabase.getInstance().getReference().child("Rooms").child(roomId);

        // Check if the current user is the host
        roomRef.child("hostId").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String hostId = dataSnapshot.getValue(String.class);
                if (currentUser.getUid().equals(hostId)) {
                    // Current user is the host, allow adding subtask
                    String subtaskId = subtasksRef.push().getKey();

                    if (subtaskId == null) {
                        Toast.makeText(SubtasksActivity.this, "Failed to create subtask", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Map<String, Object> subtaskMap = new HashMap<>();
                    subtaskMap.put("name", subtaskName);
                    subtaskMap.put("completed", false);
                    subtasksRef.child(subtaskId).setValue(subtaskMap);

                    // Clear the EditText
                    EditText subtaskEditText = findViewById(R.id.subtaskEditText);
                    subtaskEditText.setText("");
                } else {
                    // Current user is not the host, show a toast
                    Toast.makeText(SubtasksActivity.this, "Only the host can add subtasks", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(SubtasksActivity.this, "Failed to check host status: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void editSubtask(String subtaskId, String currentSubtaskName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Subtask");

        final EditText input = new EditText(this);
        input.setText(currentSubtaskName);
        builder.setView(input);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String updatedSubtaskName = input.getText().toString();
            if (!TextUtils.isEmpty(updatedSubtaskName)) {
                subtasksRef.child(subtaskId).child("name").setValue(updatedSubtaskName)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(SubtasksActivity.this, "Subtask updated", Toast.LENGTH_SHORT).show();
                            refreshSubtasksList();
                        })
                        .addOnFailureListener(e -> Toast.makeText(SubtasksActivity.this, "Failed to update subtask", Toast.LENGTH_SHORT).show());
            } else {
                Toast.makeText(SubtasksActivity.this, "Subtask name cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void deleteSubtask(String subtaskId) {
        subtasksRef.child(subtaskId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                boolean completed = dataSnapshot.child("completed").getValue(Boolean.class);
                if (completed) {
                    // Subtask is already completed, show a toast message
                    Toast.makeText(SubtasksActivity.this, "Already completed the subtask", Toast.LENGTH_SHORT).show();
                } else {
                    // Mark the subtask as completed
                    subtasksRef.child(subtaskId).child("completed").setValue(true)
                            .addOnSuccessListener(aVoid -> {
                                // Subtask status updated successfully
                                Toast.makeText(SubtasksActivity.this, "Subtask completed", Toast.LENGTH_SHORT).show();
                                // Refresh the subtasks list
                                refreshSubtasksList();
                            })
                            .addOnFailureListener(e -> {
                                // Failed to update subtask status
                                Toast.makeText(SubtasksActivity.this, "Failed to complete subtask", Toast.LENGTH_SHORT).show();
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(SubtasksActivity.this, "Failed to delete subtask: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void refreshSubtasksList() {
        subtaskList.clear();
        subtasksRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String subtaskId = snapshot.getKey();
                    String subtaskName = snapshot.child("name").getValue(String.class);
                    boolean completed = snapshot.child("completed").getValue(Boolean.class);
                    Subtask subtask = new Subtask(subtaskId, subtaskName, completed);
                    subtaskList.add(subtask);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(SubtasksActivity.this, "Failed to refresh subtasks: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private class SubtasksAdapter extends RecyclerView.Adapter<SubtasksAdapter.ViewHolder> {

        private List<Subtask> subtaskList;

        public SubtasksAdapter(List<Subtask> subtaskList) {
            this.subtaskList = subtaskList;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_subtask, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Subtask subtask = subtaskList.get(position);
            holder.subtaskName.setText(subtask.getName());

            // Apply strike-through and transparency if the subtask is completed
            if (subtask.isCompleted()) {
                holder.subtaskName.setPaintFlags(holder.subtaskName.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                holder.subtaskName.setAlpha(0.5f);
            } else {
                // Remove strike-through and reset transparency
                holder.subtaskName.setPaintFlags(holder.subtaskName.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                holder.subtaskName.setAlpha(1.0f);
            }

            holder.editButton.setOnClickListener(v -> editSubtask(subtask.getId(), subtask.getName()));
            holder.deleteButton.setOnClickListener(v -> deleteSubtask(subtask.getId()));
        }


        @Override
        public int getItemCount() {
            return subtaskList.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public TextView subtaskName;
            public ImageView editButton;
            public ImageView deleteButton;

            public ViewHolder(View itemView) {
                super(itemView);
                subtaskName = itemView.findViewById(R.id.subtaskName);
                editButton = itemView.findViewById(R.id.editButton);
                deleteButton = itemView.findViewById(R.id.deleteButton);
            }
        }
    }

    private static class Subtask {
        private String id;
        private String name;
        private boolean completed;

        public Subtask(String id, String name, boolean completed) {
            this.id = id;
            this.name = name;
            this.completed = completed;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public boolean isCompleted() {
            return completed;
        }
    }
}
