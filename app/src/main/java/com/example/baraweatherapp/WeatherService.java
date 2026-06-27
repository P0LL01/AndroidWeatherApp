package com.example.baraweatherapp;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

/**
 * Background service that periodically fetches weather updates
 * to check for dangerous conditions and trigger notifications.
 */

public class WeatherService extends Service {

    private Handler timerHandler;
    private Runnable weatherRunnable;

    private String areaName;
    private double latitude;
    private double longitude;

    // Defines the frequency of background checks
    private static final long CHECK_INTERVAL = 60 * 1000;

    // Handles results from the background thread and triggers alerts if necessary
    private Handler serviceHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {

            if (msg.what == 1) {
                Bundle bundle = msg.getData();

                String receivedAreaName = bundle.getString("areaName");
                String alertMessage = bundle.getString("alertMessage");
                boolean danger = bundle.getBoolean("danger");

                // Trigger a notification if the weather condition is classified as dangerous
                if (danger) {
                    NotificationHelper.showNotification(
                            WeatherService.this,
                            "Weather Alert",
                            receivedAreaName + ": " + alertMessage
                    );
                }
            }

            return true;
        }
    });

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // Retrieve location data from the starting intent
        latitude = intent.getDoubleExtra("latitude", 37.9838);
        longitude = intent.getDoubleExtra("longitude", 23.7275);
        areaName = intent.getStringExtra("areaName");

        if (areaName == null) {
            areaName = "Selected Area";
        }

        // Schedule periodic background tasks
        if (timerHandler == null) {
            timerHandler = new Handler();

            weatherRunnable = new Runnable() {
                @Override
                public void run() {

                    // Start a new thread for each check to perform network operations
                    WeatherThread thread = new WeatherThread(
                            areaName,
                            latitude,
                            longitude,
                            serviceHandler
                    );

                    thread.start();

                    // Schedule the next check after the specified interval
                    timerHandler.postDelayed(this, CHECK_INTERVAL);
                }
            };

            weatherRunnable.run();
        }

        return START_STICKY; // Keeps the service running even if the app is closed
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Stop periodic tasks to prevent memory leaks when service is destroyed
        if (timerHandler != null && weatherRunnable != null) {
            timerHandler.removeCallbacks(weatherRunnable);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}