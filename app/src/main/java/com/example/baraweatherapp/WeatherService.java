package com.example.baraweatherapp;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

public class WeatherService extends Service {

    private Handler serviceHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {

            if (msg.what == 1) {
                Bundle bundle = msg.getData();

                String areaName = bundle.getString("areaName");
                String alertMessage = bundle.getString("alertMessage");
                boolean danger = bundle.getBoolean("danger");

                if (danger) {
                    NotificationHelper.showNotification(
                            WeatherService.this,
                            "Weather Alert",
                            areaName + ": " + alertMessage
                    );
                }
            }

            return true;
        }
    });

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        double latitude = intent.getDoubleExtra("latitude", 37.9838);
        double longitude = intent.getDoubleExtra("longitude", 23.7275);
        String areaName = intent.getStringExtra("areaName");

        if (areaName == null) {
            areaName = "Selected Area";
        }

        WeatherThread thread = new WeatherThread(
                areaName,
                latitude,
                longitude,
                serviceHandler
        );

        thread.start();

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}