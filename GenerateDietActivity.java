package com.example.fitnessapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class GenerateDietActivity extends AppCompatActivity {

    private TextView welcomeDietTextView;
    private TextView dietPlanTextView;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate_diet);

        welcomeDietTextView = findViewById(R.id.welcomeDietTextView);
        dietPlanTextView = findViewById(R.id.dietPlanTextView);
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        db = FirebaseFirestore.getInstance();

        checkBmiStatus();
    }

    private void checkBmiStatus() {
        SharedPreferences sharedPreferences = getSharedPreferences("FitnessAppPrefs", MODE_PRIVATE);
        float bmi = sharedPreferences.getFloat("bmi", -1);

        if (bmi == -1) {
            Toast.makeText(this, "Please calculate your BMI first!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            loadUserNameAndDisplayDietPlan(bmi);
        }
    }

    private void loadUserNameAndDisplayDietPlan(float bmi) {
        if (user != null) {
            db.collection("users").document(user.getUid()).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String name = documentSnapshot.getString("name");
                            welcomeDietTextView.setText("Welcome, " + name + "! Here's your personalized diet plan.");

                            // Sample diet plans based on BMI
                            String dietPlan;
                            if (bmi < 18.5) {
                                dietPlan = "Diet Plan for Underweight:\n1. Whole grains\n2. Lean protein\n3. Healthy fats\n4. Regular meals";
                            } else if (bmi < 24.9) {
                                dietPlan = "Diet Plan for Normal Weight:\n1. Balanced meals\n2. Plenty of vegetables\n3. Lean protein sources";
                            } else if (bmi < 29.9) {
                                dietPlan = "Diet Plan for Overweight:\n1. Low-calorie options\n2. High-fiber foods\n3. Limit sugar and carbs";
                            } else {
                                dietPlan = "Diet Plan for Obesity:\n1. Consult a nutritionist\n2. High-fiber and protein-rich foods\n3. Avoid processed foods";
                            }

                            dietPlanTextView.setText(dietPlan);
                        } else {
                            Toast.makeText(this, "User data not found", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to load user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            finish(); // Close the activity if user is not authenticated
        }
    }
}
