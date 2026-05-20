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
import com.example.babylove.models.WardrobeItem;

import java.util.ArrayList;
import java.util.List;


public class RecommendationAdapter extends RecyclerView.Adapter<RecommendationAdapter.ViewHolder> {

    private List<WardrobeItem> items = new ArrayList<>();

    public void setItems(List<WardrobeItem> items) {
        this.items = items != null ? items : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recommendation, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WardrobeItem item = items.get(position);
        holder.tvName.setText(item.getName() != null ? item.getName() : "Kıyafet");
        holder.tvTags.setText(item.getTagsAsString());

        if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(item.getImageUrl())
                    .centerCrop()
                    .placeholder(R.drawable.ic_clothes_placeholder)
                    .into(holder.ivItem);
        } else {
            holder.ivItem.setImageResource(R.drawable.ic_clothes_placeholder);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivItem;
        TextView tvName, tvTags;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivItem = itemView.findViewById(R.id.ivRecommendationImage);
            tvName = itemView.findViewById(R.id.tvRecommendationName);
            tvTags = itemView.findViewById(R.id.tvRecommendationTags);
        }
    }
}
