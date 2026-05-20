package com.example.babylove.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CalendarView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.babylove.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class CalendarActivity extends AppCompatActivity {

    private CalendarView calendarView;
    private TextView tvSelectedDate, tvDayInfo;
    private FirebaseFirestore db;
    private String familyId;
    private long selectedDateMillis;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        db = FirebaseFirestore.getInstance();
        familyId = getSharedPreferences("BabyLovePrefs", MODE_PRIVATE)
                .getString("familyId", "default");

        MaterialToolbar toolbar = findViewById(R.id.toolbarCalendar);
        toolbar.setNavigationOnClickListener(v -> finish());

        calendarView = findViewById(R.id.calendarView);
        tvSelectedDate = findViewById(R.id.tvSelectedDate);
        tvDayInfo = findViewById(R.id.tvDayInfo);

        selectedDateMillis = System.currentTimeMillis();
        updateSelectedDateText(selectedDateMillis);

        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.set(year, month, dayOfMonth, 0, 0, 0);
            cal.set(java.util.Calendar.MILLISECOND, 0);
            selectedDateMillis = cal.getTimeInMillis();
            updateSelectedDateText(selectedDateMillis);
            checkLogsForDate(selectedDateMillis);
        });

        
        findViewById(R.id.btnViewDay).setOnClickListener(v -> {
            Intent intent = new Intent(this, DayDetailActivity.class);
            intent.putExtra("dateMillis", selectedDateMillis);
            startActivity(intent);
        });
    }

    private void updateSelectedDateText(long millis) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy, EEEE", new Locale("tr"));
        tvSelectedDate.setText(sdf.format(new Date(millis)));
    }

    private void checkLogsForDate(long startMillis) {
        long endMillis = startMillis + (24 * 60 * 60 * 1000);
        db.collection("families").document(familyId).collection("logs")
                .whereGreaterThanOrEqualTo("timestamp", startMillis)
                .whereLessThan("timestamp", endMillis)
                .get()
                .addOnSuccessListener(snapshots -> {
                    int count = snapshots.size();
                    if (count > 0) {
                        tvDayInfo.setText("Bu günde " + count + " kayıt var 📋");
                        tvDayInfo.setVisibility(View.VISIBLE);
                    } else {
                        tvDayInfo.setText("Bu günde kayıt yok");
                        tvDayInfo.setVisibility(View.VISIBLE);
                    }
                });
    }
}
