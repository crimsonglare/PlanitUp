package com.example.planitup;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
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
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ChatActivity extends AppCompatActivity implements ChatAdapter.OnProfileClickListener {

    private RecyclerView chatRecyclerView;
    private EditText messageEditText;

    private FirebaseAuth mAuth;
    private DatabaseReference usersRef, chatRef;
    private ChatAdapter adapter;
    private ArrayList<ChatMessage> chatList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        chatRef = FirebaseDatabase.getInstance().getReference().child("Rooms").child(getIntent().getStringExtra("roomId")).child("chat");

        chatRecyclerView = findViewById(R.id.chatRecyclerView);
        messageEditText = findViewById(R.id.messageEditText);

        chatList = new ArrayList<>();
        adapter = new ChatAdapter(this, chatList);
        chatRecyclerView.setAdapter(adapter);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadChatMessages();
        adapter.setOnProfileClickListener(this);
        adapter.setOnMessageLongClickListener(new ChatAdapter.OnMessageLongClickListener() {
            @Override
            public void onMessageLongClick(ChatMessage chatMessage) {
                showEditDeleteDialog(chatMessage);
            }
        });

        // Set click listener for the send button
        findViewById(R.id.sendButton).setOnClickListener(view -> sendMessage());
    }

    private void loadChatMessages() {
        chatRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String s) {
                String messageId = dataSnapshot.getKey();
                String senderId = dataSnapshot.child("sender").getValue(String.class);
                String message = dataSnapshot.child("message").getValue(String.class);
                long timestamp = dataSnapshot.child("timestamp").getValue(Long.class); // Get timestamp
                Boolean editedValue = dataSnapshot.child("edited").getValue(Boolean.class);
                boolean edited = editedValue != null ? editedValue : false;


                usersRef.child(senderId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String username = snapshot.child("username").getValue(String.class);
                            String profileImageUrl = snapshot.child("profileImageUrl").getValue(String.class);

                            ChatMessage chatMessage = new ChatMessage(username, message, profileImageUrl, edited, timestamp, messageId);
                            chatMessage.setUsername(username); // Set the username
                            chatList.add(chatMessage);
                            Collections.sort(chatList); // Sort messages based on timestamp
                            adapter.notifyDataSetChanged();
                            chatRecyclerView.smoothScrollToPosition(chatList.size() - 1);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(ChatActivity.this, "Failed to load chat messages: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, String s) {
                // Handle changes to existing chat messages
                String messageId = dataSnapshot.getKey();
                String senderId = dataSnapshot.child("sender").getValue(String.class);
                String message = dataSnapshot.child("message").getValue(String.class);
                long timestamp = dataSnapshot.child("timestamp").getValue(Long.class);
                boolean edited = dataSnapshot.child("edited").getValue(Boolean.class);

                // Update the existing message in the chatList
                for (ChatMessage chatMessage : chatList) {
                    if (chatMessage.getMessageId().equals(messageId)) {
                        chatMessage.setSender(chatMessage.getUsername());
                        chatMessage.setMessage(message);
                        chatMessage.setEdited(edited);
                        chatMessage.setTimestamp(timestamp);
                        // Notify adapter of the change
                        adapter.notifyDataSetChanged();
                        break;
                    }
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                // Handle removal of chat messages if needed
                String messageId = dataSnapshot.getKey();
                for (ChatMessage chatMessage : chatList) {
                    if (chatMessage.getMessageId().equals(messageId)) {
                        chatList.remove(chatMessage);
                        adapter.notifyDataSetChanged();
                        break;
                    }
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, String s) {
                // Handle movement of chat messages if needed
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Your existing code for handling onCancelled
            }
        });
    }




    public void sendMessage() {
        String message = messageEditText.getText().toString().trim();
        if (!message.isEmpty()) {
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser != null) {
                String senderId = currentUser.getUid();
                long timestamp = System.currentTimeMillis(); // Generate timestamp
                Map<String, Object> chatMessage = new HashMap<>();
                chatMessage.put("sender", senderId);
                chatMessage.put("message", message);
                chatMessage.put("edited", false); // Set edited to false initially
                chatMessage.put("timestamp", timestamp);
                chatRef.push().setValue(chatMessage);
                messageEditText.setText("");
            } else {
                Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onProfileClick(String username, String profileImageUrl) {
        showFullProfileDialog(username, profileImageUrl);
    }

    private void showFullProfileDialog(String username, String profileImageUrl) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.activity_full_profile, null);

        ImageView profileImageView = dialogView.findViewById(R.id.fullProfileImageView);
        TextView usernameTextView = dialogView.findViewById(R.id.fullUsernameTextView);

        Picasso.get().load(profileImageUrl).into(profileImageView);
        usernameTextView.setText(username);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    // Method to show dialog for editing/deleting messages
    private void showEditDeleteDialog(ChatMessage chatMessage) {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Declare currentUsername as final
        final String[] currentUsername = {""}; // Initialize currentUsername

        // Retrieve current user's username from the database
        usersRef.child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    currentUsername[0] = snapshot.child("username").getValue(String.class);
                    String senderUsername = chatMessage.getSender();

                    if (currentUsername[0] != null && senderUsername != null && senderUsername.equals(currentUsername[0])) {
                        // Current user is the sender, show edit/delete dialog
                        AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
                        builder.setTitle("Edit/Delete Message");
                        builder.setItems(new CharSequence[]{"Edit", "Delete"}, (dialogInterface, i) -> {
                            if (i == 0) {
                                showEditMessageDialog(chatMessage);
                            } else if (i == 1) {
                                deleteMessage(chatMessage);
                            }
                        });
                        builder.create().show();
                    } else {
                        Log.d("DEBUG", "User is not the sender. Cannot edit/delete message.");
                    }
                } else {
                    Log.d("DEBUG", "Current user not found in database.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("DEBUG", "Failed to retrieve current user's username: " + error.getMessage());
            }
        });
    }




    private void showEditMessageDialog(ChatMessage chatMessage) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Message");
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_edit_message, null);
        EditText editText = view.findViewById(R.id.editMessageEditText);
        editText.setText(chatMessage.getMessage());
        builder.setView(view);
        builder.setPositiveButton("Save", (dialogInterface, i) -> {
            String newMessage = editText.getText().toString().trim();
            if (!newMessage.isEmpty()) {
                editMessage(chatMessage, newMessage);
            } else {
                Toast.makeText(this, "Message cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.create().show();
    }

    private void editMessage(ChatMessage chatMessage, String newMessage) {
        String messageId = chatMessage.getMessageId();
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("message", newMessage);
        updateData.put("edited", true);
        chatRef.child(messageId).updateChildren(updateData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Message edited successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to edit message: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void deleteMessage(ChatMessage chatMessage) {
        String messageId = chatMessage.getMessageId();
        chatRef.child(messageId).removeValue()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Message deleted successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to delete message: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
