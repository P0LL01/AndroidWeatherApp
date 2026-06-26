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

public abstract class MainActivity extends AppCompatActivity implements View.OnClickListener, SensorEventListener {
    // UI variables
    private TextView cityNameText, temperatureText, humidityText, windText;
    private ImageView weatherImage;
    private Button refreshButton;
    private EditText cityNameInput;

    // shake variables for SensorEventListener
    private SensorManager SM;
    private Sensor Acc;

    // movement variables
    private float acceleration;
    private float currentAcceleration;
    private float lastAcceleration;





    // function for when the user opens the app for the first time
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        // connects with the xml file activity_main.xml
        setContentView(R.layout.activity_main);

        // enables the variable IDs from the XML file
        cityNameText = findViewById(R.id.layoutCityNameText);
        temperatureText = findViewById(R.id.layoutTemperature);
        humidityText = findViewById(R.id.humidityText);
        windText = findViewById(R.id.windText);
        weatherImage = findViewById(R.id.weatherImage);
        cityNameInput = findViewById(R.id.cityNameInput);
        refreshButton = findViewById(R.id.searchWeatherButton);



        // this button is used for the application to hear the clicks taht the user makes
        // moves straight to the onClick() function
        refreshButton.setOnClickListener(this);

        // enabling the sensor and getting the values
        SM = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        // get acceleration values from the accelerometer sensor
        Acc = SM.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        // gravity values for the moving sensors
        acceleration = 0.00f;
        currentAcceleration = SensorManager.GRAVITY_EARTH;
        lastAcceleration = SensorManager.GRAVITY_EARTH;

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    // function for when the app is running on the background of the phone
    @Override
    protected void onPause(){
        super.onPause();
        // unregisters the listener when the app is onPause() to consume less batery
        if (SM != null) {
            SM.unregisterListener(this);
        }
    }

    // function for when the user opens up the app again from the background
    @Override
    protected void onResume(){
         super.onResume();
        if (Acc != null) {
            // sets the listener on onResume to listen for new data
            // the sensor listens for the phones movement, later used in the current_location
            SM.registerListener(this, Acc, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }


    // the onClick() function enables and controlls all the apps buttons.
    @Override
    public void onClick(View v){
        if (v == refreshButton){

            // reads the users input in the citytoSearch bar
            String cityToSearch = cityNameInput.getText().toString();

            // checks if the user input is empty or not
            if(!cityToSearch.isEmpty()) {
                // API CALLING
                System.out.println("searching for city " + cityToSearch);
            }
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event){
        if (event.sensor == Acc) {
            // movement is calculated with 3 ajones
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            // currentAcceleration is calculated with the Pythagoreum theorem?
            lastAcceleration = currentAcceleration;
            currentAcceleration = (float) Math.sqrt((double) (x * x + y * y + z * z));

            // subtracting the lastAcceleration from the current one to see the difference
            float delta = currentAcceleration - lastAcceleration;
            
            // calculates the sensors noise
            acceleration = acceleration * 0.9f + delta;

            // Αν η επιτάχυνση είναι πάνω από 12, πάει να πει ότι το κινητό ΚΟΥΝΗΘΗΚΕ ΔΥΝΑΤΑ
            // (Το περπάτημα ή το απλό πιάσιμο δίνει μικρότερα νούμερα)
            if (acceleration > 12) {
                Toast.makeText(this, "Έγινε Shake! Ανανέωση...", Toast.LENGTH_SHORT).show();
                // RECALLING API FOR REFRESH LATITUDE, AMPLITUDE (SYNTETAGMENES)
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy){


    }
}