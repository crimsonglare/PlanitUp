package com.example.planitup;

public class ChatMessage implements Comparable<ChatMessage> {
    private String sender;
    private String message;
    private String profileImageUrl;

    private boolean edited;
    private String username;

    private long timestamp;
    private String messageId;

    private String senderId;

    public ChatMessage() {
        // Default constructor required for Firebase
    }

    public ChatMessage(String sender, String message, String profileImageUrl, boolean edited, long timestamp, String messageId) {
        this.sender = sender;
        this.message = message;
        this.profileImageUrl = profileImageUrl;
        this.edited = edited;
        this.timestamp = timestamp;
        this.messageId = messageId;
    }

    public String getSender() {
        return sender;
    }

    public String getMessage() {
        return message;
    }

    public String getUsername() {
        return username;
    }

    public boolean isEdited() {
        return edited;
    }
    public void setEdited(boolean edited) {
        this.edited = edited;
    }

    public String getMessageId() {
        return messageId;
    }




    public void setUsername(String username) {
        this.username = username;
    }


    public void setSender(String sender) {
        this.sender = sender;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }





    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    // Implement compareTo method if needed for sorting
    @Override
    public int compareTo(ChatMessage other) {
        // Implement this method according to your timestamp logic
        return Long.compare(this.timestamp, other.timestamp);

    }

}

