package com.example.babylove.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.babylove.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FamilyAdapter extends RecyclerView.Adapter<FamilyAdapter.FamilyViewHolder> {

    private List<Map<String, Object>> familyList = new ArrayList<>();
    private OnFamilySelectedListener listener;

    public interface OnFamilySelectedListener {
        void onFamilySelected(Map<String, Object> assignment);
    }

    public void setFamilyList(List<Map<String, Object>> list) {
        this.familyList = list;
        notifyDataSetChanged();
    }

    public void setOnFamilySelectedListener(OnFamilySelectedListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public FamilyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_family, parent, false);
        return new FamilyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FamilyViewHolder holder, int position) {
        Map<String, Object> assignment = familyList.get(position);
        
        String familyId = (String) assignment.get("familyId");

        holder.tvFamilyName.setText("Aile: " + familyId);

        holder.btnGiris.setOnClickListener(v -> {
            if (listener != null) {
                listener.onFamilySelected(assignment);
            }
        });
    }

    @Override
    public int getItemCount() {
        return familyList.size();
    }

    static class FamilyViewHolder extends RecyclerView.ViewHolder {
        TextView tvFamilyName;
        Button btnGiris;

        public FamilyViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFamilyName = itemView.findViewById(R.id.tvFamilyName);
            btnGiris = itemView.findViewById(R.id.btnGiris);
        }
    }
}
