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


public class WardrobeAdapter extends RecyclerView.Adapter<WardrobeAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(WardrobeItem item, int position);
    }

    private List<WardrobeItem> items = new ArrayList<>();
    private OnItemClickListener listener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<WardrobeItem> items) {
        this.items = items != null ? items : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void removeItem(int position) {
        if (position >= 0 && position < items.size()) {
            items.remove(position);
            notifyItemRemoved(position);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_wardrobe, parent, false);
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
                    .into(holder.ivClothes);
        } else {
            holder.ivClothes.setImageResource(R.drawable.ic_clothes_placeholder);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(item, holder.getAdapterPosition());
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivClothes;
        TextView tvName, tvTags;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivClothes = itemView.findViewById(R.id.ivClothes);
            tvName = itemView.findViewById(R.id.tvClothesName);
            tvTags = itemView.findViewById(R.id.tvClothesTags);
        }
    }
}
