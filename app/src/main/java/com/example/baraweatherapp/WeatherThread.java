package com.example.baraweatherapp;

import android.os.Handler;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import android.os.Bundle;
import android.os.Message;

import org.json.JSONObject;

public class WeatherThread extends Thread {

    private String cityName;
    private Handler handler;

    public WeatherThread(String cityName, Handler handler) {
        this.cityName = cityName;
        this.handler = handler;
    }

    @Override
    public void run() {
        try {
            double latitude = 37.9838;
            double longitude = 23.7275;

            String urlText =
                    "https://api.open-meteo.com/v1/forecast" +
                            "?latitude=" + latitude +
                            "&longitude=" + longitude +
                            "&current=temperature_2m,relative_humidity_2m,wind_speed_10m" +
                            "&forecast_days=1";

            System.out.println(urlText);

            URL url = new URL(urlText);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream())
            );

            StringBuilder result = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                result.append(line);
            }

            reader.close();
            connection.disconnect();

            System.out.println(result.toString());

            JSONObject root = new JSONObject(result.toString());
            JSONObject current = root.getJSONObject("current");

            double temperature = current.getDouble("temperature_2m");
            int humidity = current.getInt("relative_humidity_2m");
            double windSpeed = current.getDouble("wind_speed_10m");

            WeatherData weatherData = new WeatherData(
                    cityName,
                    temperature,
                    humidity,
                    windSpeed,
                    0
            );

            Message msg = new Message();
            msg.what = 1;

            Bundle bundle = new Bundle();
            bundle.putString("cityName", weatherData.cityName);
            bundle.putDouble("temperature", weatherData.temperature);
            bundle.putInt("humidity", weatherData.humidity);
            bundle.putDouble("windSpeed", weatherData.windSpeed);
            bundle.putString("alertMessage", weatherData.alertMessage);
            bundle.putBoolean("danger", weatherData.hasDangerousWeather());

            msg.setData(bundle);
            handler.sendMessage(msg);

        } catch (Exception e) {
            e.printStackTrace();
            Message msg = new Message();
            msg.what = -1;
            handler.sendMessage(msg);
        }
    }
}