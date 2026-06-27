package com.example.baraweatherapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
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

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationListener;
import android.content.Intent;

import android.location.Address;
import android.location.Geocoder;

import java.util.List;
import java.util.Locale;

import android.os.Build;

import androidx.core.app.ActivityCompat;

import android.os.Handler;
import android.os.Message;

public class MainActivity extends AppCompatActivity  implements View.OnClickListener, SensorEventListener {
    // UI variables
    private TextView cityNameText, temperatureText, humidityText, windText;
    private ImageView weatherImage;
    private Button refreshButton, currentLocationButton;
    private EditText cityNameInput;

    // shake variables for SensorEventListener
    private SensorManager SM;
    private Sensor Acc;

    // movement variables
    private float acceleration;
    private float currentAcceleration;
    private float lastAcceleration;

    private LocationManager locationManager;


    private Handler weatherHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {

            if (msg.what == 1) {
                Bundle bundle = msg.getData();

                double temperature = bundle.getDouble("temperature");
                int humidity = bundle.getInt("humidity");
                double windSpeed = bundle.getDouble("windSpeed");
                String areaName = bundle.getString("areaName");

                cityNameText.setText(areaName);
                SharedPreferences prefs = getSharedPreferences("WeatherPrefs", MODE_PRIVATE);

                // sets Celsius/KMH to default measure if the user does not specify preference
                boolean isCelsius = prefs.getBoolean("USE_CELSIUS", true);
                boolean isKmH = prefs.getBoolean("USE_KMH", true);

                if (!isCelsius) {
                    // converts to Fahrenheit
                    double fahrenheit = (temperature * 1.8) + 32;
                    temperatureText.setText(String.format("%.1f°F", fahrenheit));
                } else {
                    // uses the API data (already selected Celsius)
                    temperatureText.setText(temperature + "°C");
                }
                humidityText.setText(humidity + "%");

                //
                if (isKmH) {
                    // uses the API data (already selected KMH)
                    windText.setText(String.format("%.1f km/h", windSpeed));
                } else {
                    // converts the data to MPH
                    double mph = windSpeed * 0.621371;
                    windText.setText(String.format("%.1f Mph", mph));
                }

            } else {
                Toast.makeText(MainActivity.this, "Weather error", Toast.LENGTH_SHORT).show();
            }

            return true;
        }
    });


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
        currentLocationButton = findViewById(R.id.currentLocationButton);



        // this button is used for the application to hear the clicks taht the user makes
        // moves straight to the onClick() function
        refreshButton.setOnClickListener(this);

        currentLocationButton.setOnClickListener(this);

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

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        200
                );
            }
        }
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
            if (!cityToSearch.isEmpty()) {
                try {
                    Geocoder geocoder = new Geocoder(this, Locale.getDefault());

                    List<Address> addresses =
                            geocoder.getFromLocationName(cityToSearch, 1);

                    if (addresses != null && addresses.size() > 0) {
                        Address address = addresses.get(0);

                        double latitude = address.getLatitude();
                        double longitude = address.getLongitude();

                        WeatherThread thread = new WeatherThread(
                                cityToSearch,
                                latitude,
                                longitude,
                                weatherHandler
                        );

                        thread.start();

                    } else {
                        Toast.makeText(this, "Δεν βρέθηκε η περιοχή", Toast.LENGTH_SHORT).show();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Σφάλμα στην αναζήτηση περιοχής", Toast.LENGTH_SHORT).show();
                }
            }
        } else if (v == currentLocationButton) {

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        100
                );


                return;
            }

            locationManager.requestSingleUpdate(
                    LocationManager.GPS_PROVIDER,
                    new LocationListener() {
                        @Override
                        public void onLocationChanged(Location location) {
                            double latitude = location.getLatitude();
                            double longitude = location.getLongitude();

                            Intent serviceIntent = new Intent(MainActivity.this, WeatherService.class);
                            serviceIntent.putExtra("areaName", "Current Location");
                            serviceIntent.putExtra("latitude", latitude);
                            serviceIntent.putExtra("longitude", longitude);
                            startService(serviceIntent);

                            WeatherThread thread = new WeatherThread(
                                    "Current Location",
                                    latitude,
                                    longitude,
                                    weatherHandler
                            );



                            thread.start();
                        }
                    },
                    null
            );
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