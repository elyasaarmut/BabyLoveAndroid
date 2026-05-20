package com.example.babylove.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.babylove.R;
import com.example.babylove.models.Message;

import java.util.ArrayList;
import java.util.List;


public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;
    private static final int VIEW_TYPE_SYSTEM = 3;

    private final String currentRole; 
    private List<Message> messages = new ArrayList<>();

    public ChatAdapter(String currentRole) {
        this.currentRole = currentRole;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages != null ? messages : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void addMessage(Message message) {
        this.messages.add(message);
        notifyItemInserted(messages.size() - 1);
    }

    @Override
    public int getItemViewType(int position) {
        Message msg = messages.get(position);
        if (msg.getSenderId().equalsIgnoreCase("system")) {
            return VIEW_TYPE_SYSTEM;
        } else if (msg.getSenderId().equalsIgnoreCase(currentRole)) {
            return VIEW_TYPE_SENT;
        } else {
            return VIEW_TYPE_RECEIVED;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SYSTEM) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_chat_system, parent, false);
            return new SystemViewHolder(view);
        } else if (viewType == VIEW_TYPE_SENT) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_chat_sent, parent, false);
            return new SentViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_chat_received, parent, false);
            return new ReceivedViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message msg = messages.get(position);
        if (holder instanceof SystemViewHolder) {
            ((SystemViewHolder) holder).bind(msg);
        } else if (holder instanceof SentViewHolder) {
            ((SentViewHolder) holder).bind(msg);
        } else if (holder instanceof ReceivedViewHolder) {
            ((ReceivedViewHolder) holder).bind(msg);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class SystemViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessageSystem;

        SystemViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessageSystem = itemView.findViewById(R.id.tvMessageSystem);
        }

        void bind(Message msg) {
            tvMessageSystem.setText(msg.getText());
        }
    }

    static class SentViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage, tvTime;

        SentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvMessageSent);
            tvTime = itemView.findViewById(R.id.tvTimeSent);
        }

        void bind(Message msg) {
            tvMessage.setText(msg.getText());
            tvTime.setText(msg.getFormattedTime());
        }
    }

    static class ReceivedViewHolder extends RecyclerView.ViewHolder {
        TextView tvSenderName, tvMessage, tvTime;

        ReceivedViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSenderName = itemView.findViewById(R.id.tvSenderName);
            tvMessage = itemView.findViewById(R.id.tvMessageReceived);
            tvTime = itemView.findViewById(R.id.tvTimeReceived);
        }

        void bind(Message msg) {
            tvSenderName.setText(msg.getSenderName());
            tvMessage.setText(msg.getText());
            tvTime.setText(msg.getFormattedTime());
        }
    }
}
