package com.example.baraweatherapp;

import android.os.Handler;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import android.os.Bundle;
import android.os.Message;

import org.json.JSONObject;

/**
 * Η κλάση WeatherThread εκτελεί το αίτημα προς το Open-Meteo API
 * σε ξεχωριστό νήμα (Thread), ώστε η εφαρμογή να μην "παγώνει"
 * όσο περιμένει την απάντηση από το διαδίκτυο.
 */
public class WeatherThread extends Thread {

    private String areaName;
    private double latitude;
    private double longitude;
    private Handler handler;

    /**
     * Executes API network requests in a separate thread to prevent
     * blocking the main UI thread (ANR - Application Not Responding).
     */
    public WeatherThread(String areaName, double latitude, double longitude, Handler handler) {
        this.areaName = areaName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.handler = handler;
    }


    // run() function runs automatically when thread.start() is called
    // all the weather data fetching and weather data process happens here
    @Override
    public void run() {
        try {

            // Makes the URL for Open-Meteo API
            String urlText =
                    "https://api.open-meteo.com/v1/forecast" +
                            "?latitude=" + latitude +
                            "&longitude=" + longitude +
                            "&current=temperature_2m,relative_humidity_2m,wind_speed_10m,weather_code" +
                            "&forecast_days=1";

            System.out.println(urlText);

            // establishes the HTTP connection with the server
            URL url = new URL(urlText);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);

            // reding of the API's answer
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream())
            );

            StringBuilder result = new StringBuilder();
            String line;

            // reading of the JSON file for each line
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }

            // closes the connection
            reader.close();
            connection.disconnect();

            System.out.println(result.toString());

            // parse JSON response data
            // converts the JSON to JSONObject
            JSONObject root = new JSONObject(result.toString());

            // retrieves the "current" object
            JSONObject current = root.getJSONObject("current");

            // Extract relevant weather variables
            double temperature = current.getDouble("temperature_2m");
            int humidity = current.getInt("relative_humidity_2m");
            double windSpeed = current.getDouble("wind_speed_10m");
            int weatherCode = current.getInt("weather_code");

            // Create data model object WeatherData
            WeatherData weatherData = new WeatherData(
                    areaName,
                    temperature,
                    humidity,
                    windSpeed
            );

            // Prepare message bundle to send back to the UI/Service handler (MainActivity)
            Message msg = new Message();
            msg.what = 1;

            // saves the data in the bundle
            Bundle bundle = new Bundle();

            bundle.putString("areaName", weatherData.areaName);
            bundle.putDouble("temperature", weatherData.temperature);
            bundle.putInt("humidity", weatherData.humidity);
            bundle.putDouble("windSpeed", weatherData.windSpeed);
            bundle.putInt("weatherCode", weatherCode);
            bundle.putString("alertMessage", weatherData.alertMessage);
            bundle.putBoolean("danger", weatherData.hasDangerousWeather());

            msg.setData(bundle);

            // Dispatch message to handler (MainActivity)
            handler.sendMessage(msg);

        } catch (Exception e) {
            // debug
            e.printStackTrace();

            // debug
            // if an error occurs, it informs MainActivity with an error status msg
            Message msg = new Message();
            msg.what = -1;
            handler.sendMessage(msg);
        }
    }
}