package com.example.baraweatherapp;


/**
 * WeatherData is a class that stores weather information retrieved from the Open-Meteo API.
 * This object is used to transfer processed weather data from the WeatherThread
 * to the MainActivity.
 */
public class WeatherData {

    public String areaName; // Όνομα της πόλης για την οποία έγινε η αναζήτηση
    public double temperature; // Τρέχουσα θερμοκρασία σε βαθμούς Κελσίου
    public int humidity; // Τρέχουσα σχετική υγρασία (%)
    public double windSpeed; // Ταχύτητα ανέμου (km/h)
    public String alertMessage; // Μήνυμα προειδοποίησης που εμφανίζεται στον χρήστη

    /**
     * Class constructor.
     * Initializes the API data fields and triggers the alert calculation logic.
     */
    public WeatherData(String areaName, double temperature, int humidity, double windSpeed) {
        this.areaName = areaName;
        this.temperature = temperature;
        this.humidity = humidity;
        this.windSpeed = windSpeed;
        // Calculate alert status immediately upon initialization
        calculateAlertMessage();
    }

    /**
     * Checks if current weather conditions are considered dangerous.
     * * @return true if temperature is high (>=38°C) or wind speed is excessive (>=60 km/h).
     */
    public boolean hasDangerousWeather() {
        return temperature >= 38 || windSpeed >= 60;
    }

    /**
     * Generates an appropriate alert message based on the current weather data.
     */
    public void calculateAlertMessage() {
        if (temperature >= 38) {
            alertMessage = "Προειδοποίηση για υψηλές θερμοκρασίες";
        } else if (windSpeed >= 60) {
            alertMessage = "Προειδοποίηση για ισχυρούς ανέμους";
        } else {
            alertMessage = "Κανονικές καιρικές συνθήκες";
        }
    }
}