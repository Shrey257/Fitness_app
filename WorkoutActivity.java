package com.example.fitnessapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorkoutActivity extends AppCompatActivity {

    private TextView workoutTextView;
    private Button nextWorkoutButton, saveWorkoutButton;
    private List<String> workouts;
    private int currentWorkoutIndex = 0;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        user = auth.getCurrentUser();

        workoutTextView = findViewById(R.id.workoutTextView);
        nextWorkoutButton = findViewById(R.id.nextWorkoutButton);
        saveWorkoutButton = findViewById(R.id.saveWorkoutButton); // New button to save workout data

        // Define a list of workouts
        workouts = new ArrayList<>();
        workouts.add("10 Push-ups");
        workouts.add("15 Squats");
        workouts.add("20 Jumping Jacks");
        workouts.add("30-second Plank");
        workouts.add("15 Lunges (each leg)");
        workouts.add("15 Burpees");
        workouts.add("1-minute High Knees");

        // Shuffle workouts for variety
        Collections.shuffle(workouts);

        // Display the first workout
        displayWorkout();

        nextWorkoutButton.setOnClickListener(v -> {
            currentWorkoutIndex++;
            if (currentWorkoutIndex < workouts.size()) {
                displayWorkout();
            } else {
                Toast.makeText(this, "Great job! You've completed today's workouts!", Toast.LENGTH_SHORT).show();
                saveWorkoutButton.setVisibility(View.VISIBLE); // Show save button after all workouts are completed
            }
        });

        saveWorkoutButton.setOnClickListener(v -> saveWorkoutData());
    }

    private void displayWorkout() {
        workoutTextView.setText("Next Workout: " + workouts.get(currentWorkoutIndex));
    }

    private void saveWorkoutData() {
        if (user != null) {
            // Create a map to hold workout data
            Map<String, Object> workoutData = new HashMap<>();
            workoutData.put("workouts_completed", workouts); // Save the list of workouts completed
            workoutData.put("total_workouts", workouts.size());

            // Save workout data to Firestore
            db.collection("users").document(user.getUid()).collection("workouts").add(workoutData)
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(this, "Workout data saved!", Toast.LENGTH_SHORT).show();
                        finish(); // Return to the main activity or any other action you want
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Failed to save workout data: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
        } else {
            Toast.makeText(this, "User is not authenticated", Toast.LENGTH_SHORT).show();
        }
    }
}
