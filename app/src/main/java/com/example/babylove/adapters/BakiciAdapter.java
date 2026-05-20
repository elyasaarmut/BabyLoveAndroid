package com.example.babylove.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.babylove.R;
import com.example.babylove.models.User;

import java.util.ArrayList;
import java.util.List;

public class BakiciAdapter extends RecyclerView.Adapter<BakiciAdapter.BakiciViewHolder> {

    private List<User> bakiciList = new ArrayList<>();
    private OnBakiciSelectedListener listener;

    public interface OnBakiciSelectedListener {
        void onBakiciSelected(User bakici);
    }

    public void setBakiciList(List<User> list) {
        this.bakiciList = list;
        notifyDataSetChanged();
    }

    public void setOnBakiciSelectedListener(OnBakiciSelectedListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public BakiciViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bakici, parent, false);
        return new BakiciViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BakiciViewHolder holder, int position) {
        User bakici = bakiciList.get(position);
        holder.tvBakiciName.setText(bakici.getUid());

        String city = bakici.getCity() != null ? bakici.getCity() : "Belirtilmemiş";
        String gender = bakici.getGender() != null ? bakici.getGender() : "Belirtilmemiş";
        String age = bakici.getAge() > 0 ? bakici.getAge() + " Yaş" : "-";
        String exp = bakici.getExperienceYears() > 0 ? bakici.getExperienceYears() + " Yıl Deneyim" : "Deneyim Belirtilmemiş";

        holder.tvBakiciDetails.setText(city + " • " + gender + " • " + age + " • " + exp);
        
        String hours = bakici.getWorkingHours() != null ? "Çalışma: " + bakici.getWorkingHours() : "Çalışma saati belirtilmemiş";
        holder.tvWorkingHours.setText(hours);

        if (bakici.getProfileImageUrl() != null && !bakici.getProfileImageUrl().isEmpty()) {
            holder.ivBakiciProfile.setPadding(0, 0, 0, 0);
            Glide.with(holder.itemView.getContext())
                    .load(bakici.getProfileImageUrl())
                    .circleCrop()
                    .into(holder.ivBakiciProfile);
        } else {
            holder.ivBakiciProfile.setPadding(12, 12, 12, 12);
            holder.ivBakiciProfile.setImageResource(android.R.drawable.ic_menu_myplaces);
        }

        if (bakici.getActiveFamilyCount() >= 2) {
            holder.tvStatus.setText("Dolu (" + bakici.getActiveFamilyCount() + "/2)");
            holder.tvStatus.setTextColor(android.graphics.Color.RED);
            holder.btnSec.setEnabled(false);
            holder.btnSec.setText("Dolu");
        } else {
            holder.tvStatus.setText("Müsait (" + bakici.getActiveFamilyCount() + "/2)");
            holder.tvStatus.setTextColor(android.graphics.Color.parseColor("#81C784")); 
            holder.btnSec.setEnabled(true);
            holder.btnSec.setText("Seç");
        }

        holder.btnSec.setOnClickListener(v -> {
            if (listener != null) {
                listener.onBakiciSelected(bakici);
            }
        });
    }

    @Override
    public int getItemCount() {
        return bakiciList.size();
    }

    static class BakiciViewHolder extends RecyclerView.ViewHolder {
        TextView tvBakiciName, tvBakiciDetails, tvWorkingHours, tvStatus;
        android.widget.ImageView ivBakiciProfile;
        Button btnSec;

        public BakiciViewHolder(@NonNull View itemView) {
            super(itemView);
            tvBakiciName = itemView.findViewById(R.id.tvBakiciName);
            tvBakiciDetails = itemView.findViewById(R.id.tvBakiciDetails);
            tvWorkingHours = itemView.findViewById(R.id.tvWorkingHours);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            ivBakiciProfile = itemView.findViewById(R.id.ivBakiciProfile);
            btnSec = itemView.findViewById(R.id.btnSec);
        }
    }
}
