package com.example.baraweatherapp;

public class WeatherData {

    public String cityName;
    public double temperature;
    public int humidity;
    public double windSpeed;
    public int rainProbability;
    public String alertMessage;

    public WeatherData(String cityName, double temperature, int humidity, double windSpeed, int rainProbability) {
        this.cityName = cityName;
        this.temperature = temperature;
        this.humidity = humidity;
        this.windSpeed = windSpeed;
        this.rainProbability = rainProbability;
        this.alertMessage = "Normal weather conditions";
        calculateAlertMessage();
    }

    public boolean hasDangerousWeather() {
        return temperature >= 38 || windSpeed >= 60 || rainProbability >= 80;
    }

    public void calculateAlertMessage() {
        if (temperature >= 38) {
            alertMessage = "Προειδοποίηση για υψηλές θερμοκρασίες";
        } else if (windSpeed >= 60) {
            alertMessage = "Προειδοποίηση για ισχυρούς ανέμους";
        } else if (rainProbability >= 80) {
            alertMessage = "Προειδοποίηση για έντονες βροχοπτώσεις";
        } else {
            alertMessage = "Κανονικές καιρικές συνθήκες";
        }
    }
}