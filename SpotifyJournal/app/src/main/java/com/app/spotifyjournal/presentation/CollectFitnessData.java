package com.app.spotifyjournal.presentation;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.app.spotifyjournal.R;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

public class CollectFitnessData extends Activity {
    // HashMap to store fitness data: each entry contains a list of collected values for a specific key
    HashMap<String, Float> fitnessData = new HashMap<>();

    private float currentHeartRate = 0.0f;

    // Declare sensor manager and sensors
    private SensorManager sensorManager;
    private Sensor heartRateSensor;

    private static final String TAG = "FitnessDataCollection"; // For logging
    private static final String BUCKET_NAME = "fitness-data-bucket";

    Context context;

    public CollectFitnessData(Context context) {
        this.context = context;
        // Initialize sensor manager and sensors
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        heartRateSensor = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
    }

    public void collectData() {
        Handler handler = new Handler(Looper.getMainLooper());
        Runnable periodicUpdate = new Runnable() {
            @Override
            public void run() {
                // Collect data every 10 seconds
                collectHeartRateData();

                // Print the data being collected
                printCollectedData();

                // Send data to a Google Cloud bucket
                try {
                    // Run sendDataToCloud() in a background thread
                    Thread sendToBucket = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                sendDataToCloud();
                                Log.d(TAG, "Data uploaded to Cloud Storage");
                            } catch (Exception e) {
                                Log.e(TAG, "Error sending data to cloud", e);
                            }
                        }
                    });

                    sendToBucket.start();
                } catch (Exception e) {
                    Log.e(TAG, "Error sending data to cloud", e);
                }

                // Store the data every 10 seconds
                storeFitnessData();

                // Call again after 10 seconds
                handler.postDelayed(this, 10 * 1000); // 10 seconds
            }
        };

        handler.post(periodicUpdate);
    }

    // Method to collect heart rate data
    private void collectHeartRateData() {
        sensorManager.registerListener(new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                if (event.sensor.getType() == Sensor.TYPE_HEART_RATE) {
                    currentHeartRate = event.values[0]; // Get current heart rate
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }
        }, heartRateSensor, SensorManager.SENSOR_DELAY_UI);
    }

    // Method to store the fitness data in the fitnessData HashMap
    private void storeFitnessData() {
        String currentTime = "timestamp_" + System.currentTimeMillis(); // Use timestamp as the key
        fitnessData.put(currentTime, currentHeartRate);
    }

    // Method to send collected data to Google Cloud Storage
    private void sendDataToCloud() throws IOException {
        // Authenticate with Google Cloud
        InputStream serviceAccountStream = context.getResources().openRawResource(R.raw.service_account_key);
        ServiceAccountCredentials credentials = ServiceAccountCredentials.fromStream(serviceAccountStream);

        // Create a Storage client
        Storage storage = StorageOptions.newBuilder()
                .setCredentials(credentials)
                .build()
                .getService();

        // Convert the fitness data to JSON (you can use your preferred format)
        String jsonData = fitnessData.toString();

        // Define the object (file) name, e.g., based on timestamp
        String objectName = "fitness_data_" + System.currentTimeMillis() + ".txt";

        // Upload data to Google Cloud Storage bucket
        BlobInfo blobInfo = BlobInfo.newBuilder(BUCKET_NAME, objectName).build();
        Blob blob = storage.create(blobInfo, jsonData.getBytes());

        Log.d(TAG, "Data uploaded to Cloud Storage: " + blob.getName());
    }

    // Method to print the collected data every 10 seconds
    private void printCollectedData() {
        Log.d(TAG, "Heart Rate: " + currentHeartRate);
    }
}
