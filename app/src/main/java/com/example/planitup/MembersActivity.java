package com.example.planitup;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MembersActivity extends AppCompatActivity {

    private ListView membersListView;
    private List<String> memberUsernames;
    private DatabaseReference roomMembersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_members);

        String roomId = getIntent().getStringExtra("roomId");
        roomMembersRef = FirebaseDatabase.getInstance().getReference().child("Rooms").child(roomId).child("members");

        membersListView = findViewById(R.id.membersListView);
        memberUsernames = new ArrayList<>();

        // Set up adapter for member usernames
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, memberUsernames);
        membersListView.setAdapter(adapter);

        loadMembers();
    }

    private void loadMembers() {
        roomMembersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                memberUsernames.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String memberId = snapshot.getKey();
                    DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(memberId).child("username");
                    userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            String username = dataSnapshot.getValue(String.class);
                            if (username != null) {
                                memberUsernames.add(username);
                                ((ArrayAdapter<String>) membersListView.getAdapter()).notifyDataSetChanged();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Toast.makeText(MembersActivity.this, "Failed to load member username: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MembersActivity.this, "Failed to load members: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
