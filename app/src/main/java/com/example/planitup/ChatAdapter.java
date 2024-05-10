package com.example.planitup;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

    private Context context;
    private ArrayList<ChatMessage> chatMessages;

    private OnProfileClickListener profileClickListener;
    private OnMessageLongClickListener messageLongClickListener;

    public ChatAdapter(Context context, ArrayList<ChatMessage> chatMessages) {
        this.context = context;
        this.chatMessages = chatMessages;
    }

    public interface OnProfileClickListener {
        void onProfileClick(String username, String profileImageUrl);
    }

    public interface OnMessageLongClickListener {
        void onMessageLongClick(ChatMessage chatMessage);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_chat_message, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChatMessage chatMessage = chatMessages.get(position);
        holder.usernameTextView.setText(chatMessage.getSender());
        holder.messageTextView.setText(chatMessage.getMessage());
        if (chatMessage.isEdited()) {
            holder.editedTextView.setVisibility(View.VISIBLE);
        } else {
            holder.editedTextView.setVisibility(View.GONE);
        }
        Picasso.get().load(chatMessage.getProfileImageUrl()).into(holder.profileImageView);

        holder.profileImageView.setOnClickListener(view -> {
            if (profileClickListener != null) {
                String username = chatMessage.getSender();
                String profileImageUrl = chatMessage.getProfileImageUrl();
                profileClickListener.onProfileClick(username, profileImageUrl);
            }
        });

        // Long click listener to show edit/delete dialog
        holder.itemView.setOnLongClickListener(view -> {
            if (messageLongClickListener != null) {
                messageLongClickListener.onMessageLongClick(chatMessage);
                return true;
            }
            return false;
        });
    }

    public void setOnProfileClickListener(OnProfileClickListener listener) {
        this.profileClickListener = listener;
    }

    public void setOnMessageLongClickListener(OnMessageLongClickListener listener) {
        this.messageLongClickListener = listener;
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView usernameTextView;
        TextView messageTextView;
        TextView editedTextView;
        ImageView profileImageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            usernameTextView = itemView.findViewById(R.id.usernameTextView);
            messageTextView = itemView.findViewById(R.id.messageTextView);
            editedTextView = itemView.findViewById(R.id.editedTextView);
            profileImageView = itemView.findViewById(R.id.profileImageView);
        }
    }
}
