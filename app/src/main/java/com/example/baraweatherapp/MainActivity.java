package com.example.baraweatherapp;

import android.content.Context;
import android.content.SharedPreferences;
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

public class MainActivity extends AppCompatActivity  implements View.OnClickListener {
    // UI variables
    private TextView cityNameText, temperatureText, humidityText, windText, descriptionWeatherText;
    private ImageView weatherImage;
    private Button refreshButton, currentLocationButton;
    private EditText cityNameInput;

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
                // reads the weatherCode, sets -1 as default for debugging later
                int weatherCode = bundle.getInt("weatherCode", -1);

                // this switch statement receives the weatherCode from the API and returns the
                // proper weatherImage and viewText to the screen.
                switch (weatherCode) {

                    case 0: // Sunny weather
                        weatherImage.setImageResource(R.drawable.baseline_wb_sunny_24);
                        descriptionWeatherText.setText("Sunny");
                        break;

                    case 1:
                    case 2:
                    case 3: // Cloudy weather
                        weatherImage.setImageResource(R.drawable.clouds);
                        descriptionWeatherText.setText("Cloudy");
                        break;

                    case 55:
                    case 61:
                    case 63:
                    case 65: // Rainy weather
                        weatherImage.setImageResource(R.drawable.rain);
                        descriptionWeatherText.setText("Rainy");
                        break;

                    case 71:
                    case 73:
                    case 75: // Snowy eather
                        weatherImage.setImageResource(R.drawable.snow);
                        descriptionWeatherText.setText("Snowy");
                        break;

                    case 95:
                    case 96:
                    case 99: // Thunderstorm / Heavy rain
                        weatherImage.setImageResource(R.drawable.thunderstorm);
                        descriptionWeatherText.setText("Stormy");
                        break;

                    default:
                        // Τυπώνουμε τον ακριβή αριθμό για να ξέρουμε τι φταίει!
                        descriptionWeatherText.setText("Άγνωστος κωδικός: " + weatherCode);
                        // weatherImage.setImageResource(R.drawable.mia_genikh_eikona); // Προαιρετικά
                        break;
                }

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
        descriptionWeatherText = findViewById(R.id.descriptionWeatherText);


        // this button is used for the application to hear the clicks taht the user makes
        // moves straight to the onClick() function
        refreshButton.setOnClickListener(this);

        currentLocationButton.setOnClickListener(this);

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


    // the onClick() function enables and controlls all the apps buttons.
    @Override
    public void onClick(View v) {
        if (v == refreshButton) {

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
}