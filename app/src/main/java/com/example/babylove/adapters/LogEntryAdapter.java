package com.example.babylove.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.babylove.R;
import com.example.babylove.models.LogEntry;

import java.util.ArrayList;
import java.util.List;


public class LogEntryAdapter extends RecyclerView.Adapter<LogEntryAdapter.ViewHolder> {

    private List<LogEntry> entries = new ArrayList<>();

    public void setEntries(List<LogEntry> entries) {
        this.entries = entries != null ? entries : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_log_entry, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LogEntry entry = entries.get(position);

        
        String title = (entry.getTitle() != null && !entry.getTitle().isEmpty())
                ? entry.getTitle() : "Günlük Kaydı";
        holder.tvTitle.setText(title);

        
        holder.tvDate.setText(entry.getFormattedDate());

        
        if (entry.getNotes() != null && !entry.getNotes().isEmpty()) {
            holder.tvNotes.setVisibility(View.VISIBLE);
            holder.tvNotes.setText(entry.getNotes());
        } else {
            holder.tvNotes.setVisibility(View.GONE);
        }

        
        StringBuilder stats = new StringBuilder();
        if (entry.getHeight() != null) stats.append("📏 ").append(entry.getHeight()).append(" cm  ");
        if (entry.getWeight() != null) stats.append("⚖️ ").append(entry.getWeight()).append(" kg");
        if (stats.length() > 0) {
            holder.tvStats.setVisibility(View.VISIBLE);
            holder.tvStats.setText(stats.toString().trim());
        } else {
            holder.tvStats.setVisibility(View.GONE);
        }

        
        if (entry.hasMilestones()) {
            holder.tvMilestones.setVisibility(View.VISIBLE);
            holder.tvMilestones.setText("🏆 " + String.join("  ", entry.getMilestoneTags()));
        } else {
            holder.tvMilestones.setVisibility(View.GONE);
        }

        
        if (entry.hasImages()) {
            holder.ivLogImage.setVisibility(View.VISIBLE);
            Glide.with(holder.itemView.getContext())
                    .load(entry.getImageUrls().get(0))
                    .centerCrop()
                    .placeholder(R.drawable.ic_photo_placeholder)
                    .into(holder.ivLogImage);
        } else {
            holder.ivLogImage.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return entries.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDate, tvNotes, tvStats, tvMilestones;
        ImageView ivLogImage;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvLogTitle);
            tvDate = itemView.findViewById(R.id.tvLogDate);
            tvNotes = itemView.findViewById(R.id.tvLogNotes);
            tvStats = itemView.findViewById(R.id.tvLogStats);
            tvMilestones = itemView.findViewById(R.id.tvLogMilestones);
            ivLogImage = itemView.findViewById(R.id.ivLogImage);
        }
    }
}
