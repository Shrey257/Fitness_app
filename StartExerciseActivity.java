package com.example.fitnessapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class StartExerciseActivity extends AppCompatActivity {

    private TextView welcomeTextView;
    private TextView exerciseLinksTextView;
    private FirebaseFirestore db;
    private FirebaseUser user;
    private static final String TAG = "StartExerciseActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_exercise);

        welcomeTextView = findViewById(R.id.welcomeTextView);
        exerciseLinksTextView = findViewById(R.id.exerciseLinksTextView);
        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

        checkBmiStatus();
    }

    private void checkBmiStatus() {
        SharedPreferences sharedPreferences = getSharedPreferences("FitnessAppPrefs", MODE_PRIVATE);
        float bmi = sharedPreferences.getFloat("bmi", -1);

        Log.d(TAG, "Retrieved BMI value: " + bmi);

        if (bmi == -1) {
            Toast.makeText(this, "Please calculate your BMI first!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            loadUserNameAndDisplay();
            displayExerciseOptions();
        }
    }

    private void loadUserNameAndDisplay() {
        if (user != null) {
            db.collection("users").document(user.getUid()).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String name = documentSnapshot.getString("name");
                            welcomeTextView.setText("Hey " + (name != null ? name : "User") + ", get ready to start exercising!");
                        } else {
                            welcomeTextView.setText("Hey there, get ready to start exercising!");
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error loading user data", e);
                        welcomeTextView.setText("Hey there, get ready to start exercising!");
                    });
        }
    }

    private void displayExerciseOptions() {
        String exerciseOptions = "Here are some recommended exercises:\n\n"
                + "1. Jumping Jacks: 3 sets of 20 reps\n"
                + "2. Crunches: 3 sets of 15 reps\n"
                + "3. Squats: 3 sets of 20 reps\n"
                + "4. Lunges: 3 sets of 15 reps each side\n"
                + "5. Push-ups: 3 sets of 10 reps\n";
        exerciseLinksTextView.setText(exerciseOptions);
    }
}
