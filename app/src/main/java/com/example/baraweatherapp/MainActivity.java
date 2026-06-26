package com.example.baraweatherapp;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.Image;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.os.Handler;
import android.os.Message;

/**
 * Κύρια οθόνη της εφαρμογής.
 * Εμφανίζει τα δεδομένα καιρού, δέχεται είσοδο πόλης από τον χρήστη
 * και χρησιμοποιεί τον αισθητήρα επιτάχυνσης για λειτουργία shake-to-refresh.
 */
public class MainActivity extends AppCompatActivity  implements View.OnClickListener, SensorEventListener {
    // Στοιχεία του γραφικού περιβάλλοντος που εμφανίζουν τα δεδομένα καιρού
    private TextView cityNameText, temperatureText, humidityText, windText;
    private ImageView weatherImage;
    private Button refreshButton;
    private EditText cityNameInput;

    // Μεταβλητές για χρήση του αισθητήρα επιτάχυνσης
    private SensorManager SM;
    private Sensor Acc;

    // Μεταβλητές που χρησιμοποιούνται για τον υπολογισμό έντονης κίνησης της συσκευής
    private float acceleration;
    private float currentAcceleration;
    private float lastAcceleration;

    /**
     * Handler που λαμβάνει τα αποτελέσματα από το WeatherThread.
     * Επειδή το WeatherThread τρέχει σε background thread, δεν μπορεί να ενημερώσει
     * απευθείας το UI. Για αυτό στέλνει Message σε αυτόν τον Handler.
     */
    private Handler weatherHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {

            if (msg.what == 1) {
                // Αν το μήνυμα είναι επιτυχές, παίρνουμε τα δεδομένα καιρού από το Bundle
                Bundle bundle = msg.getData();

                double temperature = bundle.getDouble("temperature");
                int humidity = bundle.getInt("humidity");
                double windSpeed = bundle.getDouble("windSpeed");
                String areaName = bundle.getString("areaName");

                // Ενημέρωση των TextViews με τα νέα δεδομένα
                cityNameText.setText(areaName);
                temperatureText.setText(temperature + "°C");
                humidityText.setText(humidity + "%");
                windText.setText(windSpeed + " km/h");

            } else {
                // Αν κάτι πάει λάθος στο WeatherThread, εμφανίζεται μήνυμα σφάλματος
                Toast.makeText(MainActivity.this, "Weather error", Toast.LENGTH_SHORT).show();
            }

            return true;
        }
    });


    /**
     * Εκτελείται όταν δημιουργείται η Activity.
     * Εδώ συνδέουμε το XML layout με την Java κλάση και αρχικοποιούμε
     * τα στοιχεία του UI και τον αισθητήρα.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        // Σύνδεση της Activity με το αντίστοιχο XML layout
        setContentView(R.layout.activity_main);

        // Σύνδεση των UI μεταβλητών με τα αντίστοιχα στοιχεία του XML
        cityNameText = findViewById(R.id.layoutCityNameText);
        temperatureText = findViewById(R.id.layoutTemperature);
        humidityText = findViewById(R.id.humidityText);
        windText = findViewById(R.id.windText);
        weatherImage = findViewById(R.id.weatherImage);
        cityNameInput = findViewById(R.id.cityNameInput);
        refreshButton = findViewById(R.id.searchWeatherButton);



        // Ορισμός listener ώστε το κουμπί να καλεί την onClick()
        refreshButton.setOnClickListener(this);

        // Αρχικοποίηση του SensorManager και επιλογή του accelerometer
        SM = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Acc = SM.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        // Αρχικές τιμές για τον υπολογισμό της επιτάχυνσης
        acceleration = 0.00f;
        currentAcceleration = SensorManager.GRAVITY_EARTH;
        lastAcceleration = SensorManager.GRAVITY_EARTH;

        // Προσαρμογή padding ώστε το περιεχόμενο να μη συγκρούεται με system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    /**
     * Εκτελείται όταν η εφαρμογή μπαίνει σε κατάσταση pause.
     * Σταματάμε τον sensor listener για εξοικονόμηση μπαταρίας.
     */
    @Override
    protected void onPause(){
        super.onPause();
        if (SM != null) {
            SM.unregisterListener(this);
        }
    }

    /**
     * Εκτελείται όταν η εφαρμογή επιστρέφει στο προσκήνιο.
     * Ενεργοποιούμε ξανά τον accelerometer listener.
     */
    @Override
    protected void onResume(){
         super.onResume();
        if (Acc != null) {
            SM.registerListener(this, Acc, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }


    /**
     * Διαχειρίζεται τα clicks των κουμπιών της Activity.
     * Όταν ο χρήστης πατήσει το κουμπί αναζήτησης, ξεκινάει WeatherThread
     * για να γίνει η λήψη των δεδομένων καιρού από το API.
     */
    @Override
    public void onClick(View v){
        if (v == refreshButton){

            // Παίρνουμε την πόλη που έγραψε ο χρήστης
            String cityToSearch = cityNameInput.getText().toString();

            // Αν το πεδίο δεν είναι άδειο, ξεκινάμε background thread για το API request
            if (!cityToSearch.isEmpty()) {

                double latitude = 37.9838;
                double longitude = 23.7275;

                // Επιλογή συντεταγμένων ανάλογα με την πόλη
                if (cityToSearch.equalsIgnoreCase("Athens")) {
                    latitude = 37.9838;
                    longitude = 23.7275;

                } else if (cityToSearch.equalsIgnoreCase("Thessaloniki")) {
                    latitude = 40.6401;
                    longitude = 22.9444;

                } else if (cityToSearch.equalsIgnoreCase("Patras")) {
                    latitude = 38.2466;
                    longitude = 21.7346;

                } else if (cityToSearch.equalsIgnoreCase("Heraklion")) {
                    latitude = 35.3387;
                    longitude = 25.1442;
                }

                WeatherThread thread = new WeatherThread(
                        cityToSearch,
                        latitude,
                        longitude,
                        weatherHandler
                );

                thread.start();
            }
        }
    }

    /**
     * Καλείται κάθε φορά που ο αισθητήρας επιτάχυνσης δίνει νέα δεδομένα.
     * Χρησιμοποιείται για να ανιχνεύσουμε αν ο χρήστης κούνησε έντονα τη συσκευή.
     */
    @Override
    public void onSensorChanged(SensorEvent event){
        if (event.sensor == Acc) {
            // Τιμές επιτάχυνσης στους τρεις άξονες της συσκευής
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            // Υπολογισμός συνολικής επιτάχυνσης από τους τρεις άξονες
            lastAcceleration = currentAcceleration;
            currentAcceleration = (float) Math.sqrt((double) (x * x + y * y + z * z));

            // Διαφορά τρέχουσας και προηγούμενης επιτάχυνσης
            float delta = currentAcceleration - lastAcceleration;

            // Φίλτρο για μείωση θορύβου του αισθητήρα
            acceleration = acceleration * 0.9f + delta;

            // Αν η επιτάχυνση είναι πάνω από 12, πάει να πει ότι το κινητό ΚΟΥΝΗΘΗΚΕ ΔΥΝΑΤΑ
            // (Το περπάτημα ή το απλό πιάσιμο δίνει μικρότερα νούμερα)
            if (acceleration > 12) {
                Toast.makeText(this, "Έγινε Shake! Ανανέωση...", Toast.LENGTH_SHORT).show();
                // RECALLING API FOR REFRESH LATITUDE, AMPLITUDE (SYNTETAGMENES)
            }
        }
    }

    /**
     * Μέθοδος του SensorEventListener.
     * Δεν χρησιμοποιείται στην εφαρμογή, αλλά πρέπει να υπάρχει λόγω του interface.
     */
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy){


    }
}