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

    public WeatherThread(String areaName, double latitude, double longitude, Handler handler) {
        this.areaName = areaName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.handler = handler;
    }

    /**
     * Η run() εκτελείται αυτόματα όταν καλέσουμε thread.start().
     * Εδώ γίνεται όλη η διαδικασία λήψης και επεξεργασίας των δεδομένων καιρού.
     */
    @Override
    public void run() {
        try {

            // Δημιουργία του URL για το Open-Meteo API
            String urlText =
                    "https://api.open-meteo.com/v1/forecast" +
                            "?latitude=" + latitude +
                            "&longitude=" + longitude +
                            "&current=temperature_2m,relative_humidity_2m,wind_speed_10m,weather_code" +
                            "&forecast_days=1";

            System.out.println(urlText);

            // Δημιουργία σύνδεσης HTTP με τον server
            URL url = new URL(urlText);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);

            // Ανάγνωση της απάντησης του API
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream())
            );

            StringBuilder result = new StringBuilder();
            String line;

            // Διαβάζουμε το JSON γραμμή-γραμμή
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }

            // Κλείσιμο της σύνδεσης
            reader.close();
            connection.disconnect();

            System.out.println(result.toString());

            // Μετατροπή του JSON σε αντικείμενο JSONObject
            JSONObject root = new JSONObject(result.toString());

            // Ανάκτηση του αντικειμένου "current"
            JSONObject current = root.getJSONObject("current");

            // Εξαγωγή των τιμών που μας ενδιαφέρουν
            double temperature = current.getDouble("temperature_2m");
            int humidity = current.getInt("relative_humidity_2m");
            double windSpeed = current.getDouble("wind_speed_10m");
            int weatherCode = current.getInt("weather_code");

            // Δημιουργία αντικειμένου WeatherData
            WeatherData weatherData = new WeatherData(
                    areaName,
                    temperature,
                    humidity,
                    windSpeed,
                    0
            );

            // Δημιουργία Message που θα σταλεί στο MainActivity
            Message msg = new Message();
            msg.what = 1;

            // Αποθήκευση των δεδομένων μέσα σε Bundle
            Bundle bundle = new Bundle();

            bundle.putString("areaName", weatherData.areaName);
            bundle.putDouble("temperature", weatherData.temperature);
            bundle.putInt("humidity", weatherData.humidity);
            bundle.putDouble("windSpeed", weatherData.windSpeed);
            bundle.putInt("weatherCode", weatherCode);
            bundle.putString("alertMessage", weatherData.alertMessage);
            bundle.putBoolean("danger", weatherData.hasDangerousWeather());

            msg.setData(bundle);

            // Αποστολή του Message στον Handler του MainActivity
            handler.sendMessage(msg);

        } catch (Exception e) {
            // Εμφάνιση του σφάλματος για λόγους αποσφαλμάτωσης
            e.printStackTrace();

            // Αν υπάρξει οποιοδήποτε πρόβλημα,
            // ενημερώνεται το MainActivity με μήνυμα αποτυχίας
            Message msg = new Message();
            msg.what = -1;
            handler.sendMessage(msg);
        }
    }
}