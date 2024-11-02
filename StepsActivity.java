package com.example.fitnessapp;

import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class StepsActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor stepCounterSensor;
    private TextView stepsTextView;
    private TextView greetingTextView;
    private Button incrementStepsButton;
    private int stepCount = 0; // Total steps counted
    private FirebaseFirestore db;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_steps); // Your layout for steps activity

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

        greetingTextView = findViewById(R.id.greetingTextView);
        stepsTextView = findViewById(R.id.stepsTextView);
        incrementStepsButton = findViewById(R.id.incrementStepsButton);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

        // Load username and previous step count from Firestore
        if (user != null) {
            loadUsername();
            loadStepCount();
        }

        // Set click listener for increment steps button
        incrementStepsButton.setOnClickListener(v -> {
            stepCount++;
            stepsTextView.setText("Steps: " + stepCount);
            saveStepCount(); // Save the incremented step count
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (stepCounterSensor != null) {
            sensorManager.registerListener(this, stepCounterSensor, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (stepCounterSensor != null) {
            sensorManager.unregisterListener(this);
        }
        // Save the step count to Firestore when the activity is paused
        saveStepCount();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            stepCount = (int) event.values[0];
            stepsTextView.setText("Steps: " + stepCount);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not used
    }

    private void loadUsername() {
        if (user != null) {
            db.collection("users").document(user.getUid()).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String name = documentSnapshot.getString("name");
                            greetingTextView.setText("Hey " + (name != null ? name : "User" +" !") );
                        } else {
                            greetingTextView.setText("Hey there, get ready to start exercising!");
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(StepsActivity.this, "Error fetching username: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }
    private void loadStepCount() {
        SharedPreferences sharedPreferences = getSharedPreferences("FitnessAppPrefs", MODE_PRIVATE);
        stepCount = sharedPreferences.getInt(user.getUid() + "_steps", 0);
        stepsTextView.setText("Steps: " + stepCount);
    }

    private void saveStepCount() {
        // Save step count to SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("FitnessAppPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(user.getUid() + "_steps", stepCount);
        editor.apply();

        // Save step count to Firestore
        if (user != null) {
            db.collection("users").document(user.getUid())
                    .update("steps", stepCount)
                    .addOnSuccessListener(aVoid -> Toast.makeText(StepsActivity.this, "Steps saved", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(StepsActivity.this, "Error saving steps: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }
}
