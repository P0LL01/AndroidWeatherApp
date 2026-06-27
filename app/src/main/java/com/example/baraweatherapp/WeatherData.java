package com.example.baraweatherapp;

/**
 * Η κλάση WeatherData αποθηκεύει όλες τις πληροφορίες
 * καιρού που λαμβάνονται από το Open-Meteo API.
 *
 * Το αντικείμενο αυτό χρησιμοποιείται για τη μεταφορά
 * των δεδομένων από το WeatherThread προς το MainActivity.
 */
public class WeatherData {

    public String areaName; // Όνομα της πόλης για την οποία έγινε η αναζήτηση
    public double temperature; // Τρέχουσα θερμοκρασία σε βαθμούς Κελσίου
    public int humidity; // Τρέχουσα σχετική υγρασία (%)
    public double windSpeed; // Ταχύτητα ανέμου (km/h)
    public String alertMessage; // Μήνυμα προειδοποίησης που εμφανίζεται στον χρήστη

    /**
     * Constructor της κλάσης.
     * Αρχικοποιεί όλες τις τιμές που επιστρέφει το API
     * και δημιουργεί το κατάλληλο μήνυμα προειδοποίησης.
     */
    public WeatherData(String areaName, double temperature, int humidity, double windSpeed) {
        this.areaName = areaName;
        this.temperature = temperature;
        this.humidity = humidity;
        this.windSpeed = windSpeed;
        // Δημιουργία του κατάλληλου μηνύματος προειδοποίησης
        calculateAlertMessage();
    }

    /**
     * Ελέγχει αν οι καιρικές συνθήκες θεωρούνται επικίνδυνες.
     *
     * @return true αν υπάρχει υψηλή θερμοκρασία,
     * ισχυρός άνεμος ή μεγάλη πιθανότητα βροχής.
     */
    public boolean hasDangerousWeather() {
        return temperature >= 38 || windSpeed >= 60;
    }

    /**
     * Δημιουργεί το κατάλληλο μήνυμα προειδοποίησης
     * ανάλογα με τις τρέχουσες καιρικές συνθήκες.
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