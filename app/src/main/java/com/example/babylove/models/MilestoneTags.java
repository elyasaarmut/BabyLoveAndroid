package com.example.babylove.models;

import android.graphics.Color;
import java.util.Arrays;
import java.util.List;


public class MilestoneTags {

    public static final List<String> PREDEFINED = Arrays.asList(
            "İlk Adım 🚶",
            "İlk Kelime 🗣️",
            "İlk Diş 🦷",
            "İlk Gülümseme 😄",
            "İlk Yemek 🍽️",
            "İlk Banyo 🛁",
            "İlk Uyku 😴",
            "İlk Park 🌳",
            "Aşı 💉",
            "Doktor Ziyareti 🏥",
            "Özel An ⭐"
    );

    private static final int[] COLORS = {
            Color.parseColor("#FFD3B6"),  
            Color.parseColor("#A8E6CF"),  
            Color.parseColor("#C3AED6"),  
            Color.parseColor("#FFF1C1"),  
            Color.parseColor("#FFCDD2"),  
            Color.parseColor("#BBDEFB"),  
            Color.parseColor("#F3C9DD"),  
            Color.parseColor("#B0E0F0"),  
            Color.parseColor("#81C784"),  
            Color.parseColor("#FFB74D"),  
            Color.parseColor("#E8A0BF")   
    };

    public static int getColorForIndex(int index) {
        return COLORS[index % COLORS.length];
    }
}
