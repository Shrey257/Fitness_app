package com.example.fitnessapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    FirebaseAuth auth;
    Button button;
    TextView textview;
    FirebaseUser user;
    Button calculateBmiButton, startExerciseButton, generateDietButton, calculateStepsButton, startWorkout;
    TextView graphPlaceholder; // Add a TextView reference for the graph placeholder
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();
        button = findViewById(R.id.logout);
        textview = findViewById(R.id.user_details);
        user = auth.getCurrentUser();
        calculateBmiButton = findViewById(R.id.calculateBmiButton);
        startExerciseButton = findViewById(R.id.startExerciseButton);
        generateDietButton = findViewById(R.id.generateDietButton);
        calculateStepsButton = findViewById(R.id.calculateStepsButton);
        startWorkout = findViewById(R.id.startWorkoutButton);
        graphPlaceholder = findViewById(R.id.graphPlaceholder); // Initialize graphPlaceholder

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("FitnessAppPrefs", MODE_PRIVATE);

        // Check if user is logged in
        if (user == null) {
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();
        } else {
            textview.setText(user.getEmail());
            displayAccountCreationInfo();
            displayWorkoutCount();
        }

        // Set click listener for Calculate BMI button
        calculateBmiButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, BmiCalculationActivity.class);
            startActivity(intent);
        });

        // Set click listener for Start Exercise button with BMI check
        startExerciseButton.setOnClickListener(v -> {
            checkBmiAndStartExercise();
        });

        // Set click listener for Generate Diet button
        generateDietButton.setOnClickListener(v -> {
            checkBmiAndStartDiet();
        });

        calculateStepsButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, StepsActivity.class);
            startActivity(intent);
            finish();
        });

        startWorkout.setOnClickListener(v -> {
            // Increment workout count
            incrementWorkoutCount();
            Intent intent = new Intent(MainActivity.this, WorkoutActivity.class);
            startActivity(intent);
        });


        // Set click listener for Logout button
        button.setOnClickListener(view -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();
        });
    }
    public void openAchievementsNews(View view) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://news.google.com/search?cf=all&q=sport+india&hl=en-IN&gl=IN&ceid=IN:en"));
        startActivity(browserIntent);
    }
    private void displayAccountCreationInfo() {
        // Get the account creation date from Firebase User metadata
        if (user != null) {
            // Get the account creation time
            long creationTime = user.getMetadata().getCreationTimestamp(); // Timestamp in milliseconds

            // Format the creation time to a date string
            String accountCreationDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date(creationTime));

            // Calculate the number of days since account creation
            long currentTime = System.currentTimeMillis();
            long daysSinceCreation = (currentTime - creationTime) / (1000 * 60 * 60 * 24); // Convert milliseconds to days

            // Display the account creation date and number of days since creation
            graphPlaceholder.append("Account Creation Date: " + accountCreationDate + "\n");
            graphPlaceholder.append("Days Since Account Creation: " + daysSinceCreation + " days\n");
        }
    }

    private void displayWorkoutCount() {
        int workoutCount = sharedPreferences.getInt(user.getUid() + "_workout_count", 0); // Get workout count

        // Display the workout count in the graph placeholder
        graphPlaceholder.append("Workouts Completed Today: " + workoutCount);
    }

    private void incrementWorkoutCount() {
        int workoutCount = sharedPreferences.getInt(user.getUid() + "_workout_count", 0); // Get the current count
        workoutCount++; // Increment the count

        // Save the updated count back to SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(user.getUid() + "_workout_count", workoutCount);
        editor.apply();
    }

    private void checkBmiAndStartExercise() {
        SharedPreferences sharedPreferences = getSharedPreferences("FitnessAppPrefs", MODE_PRIVATE);
        float bmi = sharedPreferences.getFloat(user.getUid() + "_bmi", -1); // Default to -1 if no data

        if (bmi == -1) {
            Toast.makeText(this, "Please calculate your BMI first!", Toast.LENGTH_SHORT).show();
        } else {
            Intent intent = new Intent(MainActivity.this, StartExerciseActivity.class);
            startActivity(intent);
        }
    }

    private void checkBmiAndStartDiet() {
        SharedPreferences sharedPreferences = getSharedPreferences("FitnessAppPrefs", MODE_PRIVATE);
        float bmi = sharedPreferences.getFloat(user.getUid() + "_bmi", -1); // Default to -1 if no data

        if (bmi == -1) {
            Toast.makeText(this, "Please calculate your BMI first!", Toast.LENGTH_SHORT).show();
        } else {
            Intent intent = new Intent(MainActivity.this, GenerateDietActivity.class);
            startActivity(intent);
        }
    }
}
