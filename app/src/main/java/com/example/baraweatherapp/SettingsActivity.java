package com.example.baraweatherapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

public class SettingsActivity extends AppCompatActivity {

    private SwitchCompat tempSwitch, windSwitch;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // XML connection
        tempSwitch = findViewById(R.id.tempSwitch);
        windSwitch = findViewById(R.id.windSwitch);

        // opens the settings file
        sharedPreferences = getSharedPreferences("WeatherPrefs", MODE_PRIVATE);

        // loads the defaults values (Celsius/KMH)
        tempSwitch.setChecked(sharedPreferences.getBoolean("USE_CELSIUS", true));
        windSwitch.setChecked(sharedPreferences.getBoolean("USE_KMH", true));

        // listener for temperature switch
        tempSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sharedPreferences.edit().putBoolean("USE_CELSIUS", isChecked).apply();
        });

        // listener for wind switch
        windSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sharedPreferences.edit().putBoolean("USE_KMH", isChecked).apply();
        });
    }
}
