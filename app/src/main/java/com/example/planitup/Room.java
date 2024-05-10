package com.example.planitup;

import com.google.firebase.database.DataSnapshot;

import java.util.HashMap;
import java.util.Map;

public class Room {
    private String roomId;
    private String roomName;
    private String roomCode;
    private String hostId;

    public Room() {
        // Default constructor required for calls to DataSnapshot.getValue(Room.class)
    }

    public Room(String roomId, String roomName, String roomCode, String hostId) {
        this.roomId = roomId;
        this.roomName = roomName;
        this.roomCode = roomCode;
        this.hostId = hostId;
    }
    public String getRoomCode() {
        return roomCode;
    }
    public String getRoomName() {
        return roomName;
    }

    public String getRoomId() {
        return roomId;
    }

    public Object getHostId() {
        return hostId;
    }



    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("roomId", roomId);
        result.put("roomName", roomName);
        result.put("roomCode", roomCode);
        result.put("hostId", hostId);
        return result;
    }
    // Room.java
    public static Room fromSnapshot(DataSnapshot snapshot) {
        String roomId = snapshot.getKey();
        String roomName = snapshot.child("roomName").getValue(String.class);
        String roomCode = snapshot.child("roomCode").getValue(String.class);
        String hostId = snapshot.child("hostId").getValue(String.class);
        return new Room(roomId, roomName, roomCode, hostId);
    }



}