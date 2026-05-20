package com.example.babylove.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.babylove.R;
import com.example.babylove.models.LogEntry;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class GrowthChartActivity extends AppCompatActivity {

    private LineChart growthLineChart;
    private TextView tvGrowthData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_growth_chart);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String familyId = getSharedPreferences("BabyLovePrefs", MODE_PRIVATE)
                .getString("familyId", "default");

        MaterialToolbar toolbar = findViewById(R.id.toolbarGrowth);
        toolbar.setNavigationOnClickListener(v -> finish());

        growthLineChart = findViewById(R.id.growthLineChart);
        tvGrowthData = findViewById(R.id.tvGrowthData);

        setupChart();

        db.collection("families").document(familyId).collection("logs")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(snapshots -> {
                    List<LogEntry> logs = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshots) {
                        LogEntry entry = doc.toObject(LogEntry.class);
                        if (entry.getHeight() != null || entry.getWeight() != null) {
                            logs.add(entry);
                        }
                    }

                    if (logs.isEmpty()) {
                        growthLineChart.setVisibility(View.GONE);
                        tvGrowthData.setText("Henüz boy/kilo verisi yok.\nBakıcı günlük kaydına boy ve kilo bilgisi ekledikten sonra burada interaktif bir gelişim grafiği oluşacaktır.");
                    } else {
                        growthLineChart.setVisibility(View.VISIBLE);
                        displayChartAndList(logs);
                    }
                });
    }

    private void setupChart() {
        growthLineChart.getDescription().setEnabled(false);
        growthLineChart.setTouchEnabled(true);
        growthLineChart.setDragEnabled(true);
        growthLineChart.setScaleEnabled(true);
        growthLineChart.setPinchZoom(true);
        growthLineChart.setDrawGridBackground(false);

        
        XAxis xAxis = growthLineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setTextColor(Color.parseColor("#8E8E8E"));
        xAxis.setTextSize(10f);

        
        YAxis leftAxis = growthLineChart.getAxisLeft();
        leftAxis.setTextColor(Color.parseColor("#D4789E")); 
        leftAxis.setTextSize(11f);
        leftAxis.setDrawGridLines(true);
        leftAxis.setGridColor(Color.parseColor("#E8E8E8"));

        
        YAxis rightAxis = growthLineChart.getAxisRight();
        rightAxis.setTextColor(Color.parseColor("#5BA8C8")); 
        rightAxis.setTextSize(11f);
        rightAxis.setDrawGridLines(false);

        
        Legend l = growthLineChart.getLegend();
        l.setForm(Legend.LegendForm.LINE);
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);
        l.setTextSize(12f);
    }

    private void displayChartAndList(List<LogEntry> logs) {
        List<Entry> heightEntries = new ArrayList<>();
        List<Entry> weightEntries = new ArrayList<>();
        List<String> formattedDates = new ArrayList<>();
        StringBuilder textLog = new StringBuilder();
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", new Locale("tr"));

        int dataIndex = 0;
        for (LogEntry entry : logs) {
            String dateStr = sdf.format(new Date(entry.getTimestamp()));
            formattedDates.add(dateStr);

            boolean hasData = false;
            if (entry.getHeight() != null) {
                heightEntries.add(new Entry(dataIndex, entry.getHeight().floatValue()));
                hasData = true;
            }
            if (entry.getWeight() != null) {
                weightEntries.add(new Entry(dataIndex, entry.getWeight().floatValue()));
                hasData = true;
            }

            if (hasData) {
                textLog.append("📅 ").append(dateStr);
                if (entry.getHeight() != null) {
                    textLog.append("   📏 Boy: ").append(entry.getHeight()).append(" cm");
                }
                if (entry.getWeight() != null) {
                    textLog.append("   ⚖️ Kilo: ").append(entry.getWeight()).append(" kg");
                }
                textLog.append("\n\n");
                dataIndex++;
            }
        }

        
        growthLineChart.getXAxis().setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int index = Math.round(value);
                if (index >= 0 && index < formattedDates.size()) {
                    return formattedDates.get(index);
                }
                return "";
            }
        });

        
        LineDataSet heightDataSet = new LineDataSet(heightEntries, "Boy (cm)");
        heightDataSet.setColor(Color.parseColor("#E8A0BF"));
        heightDataSet.setCircleColor(Color.parseColor("#D4789E"));
        heightDataSet.setLineWidth(3f);
        heightDataSet.setCircleRadius(5f);
        heightDataSet.setDrawCircleHole(true);
        heightDataSet.setValueTextSize(9f);
        heightDataSet.setValueTextColor(Color.parseColor("#3D3D3D"));
        heightDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);

        
        LineDataSet weightDataSet = new LineDataSet(weightEntries, "Kilo (kg)");
        weightDataSet.setColor(Color.parseColor("#7EC8E3"));
        weightDataSet.setCircleColor(Color.parseColor("#5BA8C8"));
        weightDataSet.setLineWidth(3f);
        weightDataSet.setCircleRadius(5f);
        weightDataSet.setDrawCircleHole(true);
        weightDataSet.setValueTextSize(9f);
        weightDataSet.setValueTextColor(Color.parseColor("#3D3D3D"));
        weightDataSet.setAxisDependency(YAxis.AxisDependency.RIGHT);

        List<ILineDataSet> dataSets = new ArrayList<>();
        if (!heightEntries.isEmpty()) dataSets.add(heightDataSet);
        if (!weightEntries.isEmpty()) dataSets.add(weightDataSet);

        LineData lineData = new LineData(dataSets);
        growthLineChart.setData(lineData);
        growthLineChart.invalidate(); 

        tvGrowthData.setText(textLog.toString().trim());
    }
}
