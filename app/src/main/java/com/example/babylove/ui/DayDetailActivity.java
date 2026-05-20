package com.example.babylove.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.babylove.R;
import com.example.babylove.adapters.LogEntryAdapter;
import com.example.babylove.models.LogEntry;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class DayDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_day_detail);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String familyId = getSharedPreferences("BabyLovePrefs", MODE_PRIVATE)
                .getString("familyId", "default");

        long dateMillis = getIntent().getLongExtra("dateMillis", System.currentTimeMillis());
        long startMillis = getStartOfDay(dateMillis);
        long endMillis = startMillis + (24 * 60 * 60 * 1000);

        MaterialToolbar toolbar = findViewById(R.id.toolbarDayDetail);
        SimpleDateFormat titleFmt = new SimpleDateFormat("dd MMMM yyyy", new Locale("tr"));
        toolbar.setTitle(titleFmt.format(new Date(startMillis)));
        toolbar.setNavigationOnClickListener(v -> finish());

        RecyclerView rv = findViewById(R.id.rvDayLogs);
        TextView tvEmpty = findViewById(R.id.tvDayEmpty);
        rv.setLayoutManager(new LinearLayoutManager(this));
        LogEntryAdapter adapter = new LogEntryAdapter();
        rv.setAdapter(adapter);

        db.collection("families").document(familyId).collection("logs")
                .whereGreaterThanOrEqualTo("timestamp", startMillis)
                .whereLessThan("timestamp", endMillis)
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snapshots -> {
                    List<LogEntry> entries = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshots) {
                        entries.add(doc.toObject(LogEntry.class));
                    }
                    adapter.setEntries(entries);
                    tvEmpty.setVisibility(entries.isEmpty() ? View.VISIBLE : View.GONE);
                    rv.setVisibility(entries.isEmpty() ? View.GONE : View.VISIBLE);
                });
    }

    private long getStartOfDay(long millis) {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.setTimeInMillis(millis);
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
        cal.set(java.util.Calendar.MINUTE, 0);
        cal.set(java.util.Calendar.SECOND, 0);
        cal.set(java.util.Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }
}
