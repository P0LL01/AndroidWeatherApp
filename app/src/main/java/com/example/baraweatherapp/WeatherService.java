package com.example.baraweatherapp;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

public class WeatherService extends Service {

    private Handler timerHandler;
    private Runnable weatherRunnable;

    private String areaName;
    private double latitude;
    private double longitude;

    // Για demo βάζουμε 1 λεπτό. Στην τελική εργασία μπορείτε να το κάνετε 30 λεπτά.
    private static final long CHECK_INTERVAL = 60 * 1000;

    private Handler serviceHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {

            if (msg.what == 1) {
                Bundle bundle = msg.getData();

                String receivedAreaName = bundle.getString("areaName");
                String alertMessage = bundle.getString("alertMessage");
                boolean danger = bundle.getBoolean("danger");

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

        latitude = intent.getDoubleExtra("latitude", 37.9838);
        longitude = intent.getDoubleExtra("longitude", 23.7275);
        areaName = intent.getStringExtra("areaName");

        if (areaName == null) {
            areaName = "Selected Area";
        }

        if (timerHandler == null) {
            timerHandler = new Handler();

            weatherRunnable = new Runnable() {
                @Override
                public void run() {

                    WeatherThread thread = new WeatherThread(
                            areaName,
                            latitude,
                            longitude,
                            serviceHandler
                    );

                    thread.start();

                    timerHandler.postDelayed(this, CHECK_INTERVAL);
                }
            };

            weatherRunnable.run();
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (timerHandler != null && weatherRunnable != null) {
            timerHandler.removeCallbacks(weatherRunnable);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}