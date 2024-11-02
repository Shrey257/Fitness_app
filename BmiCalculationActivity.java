package com.example.fitnessapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class BmiCalculationActivity extends AppCompatActivity {

    private EditText nameInput, genderInput, ageInput, heightInput, weightInput;
    private TextView bmiResult, bmiCategory;
    private Button calculateButton, saveButton;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bmi_calculation);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        user = auth.getCurrentUser();

        nameInput = findViewById(R.id.nameInput);
        genderInput = findViewById(R.id.genderInput);
        ageInput = findViewById(R.id.ageInput);
        heightInput = findViewById(R.id.heightInput);
        weightInput = findViewById(R.id.weightInput);
        bmiResult = findViewById(R.id.bmiResult);
        bmiCategory = findViewById(R.id.bmiCategory);
        calculateButton = findViewById(R.id.calculateButton);
        saveButton = findViewById(R.id.saveButton);

        calculateButton.setOnClickListener(v -> calculateBmi());
        saveButton.setOnClickListener(v -> saveBmiData());

        if (user != null) {
            loadBmiData();
        }
    }

    private void calculateBmi() {
        String heightStr = heightInput.getText().toString();
        String weightStr = weightInput.getText().toString();

        if (!heightStr.isEmpty() && !weightStr.isEmpty()) {
            try {
                double height = Double.parseDouble(heightStr) / 100;
                double weight = Double.parseDouble(weightStr);
                double bmi = weight / (height * height);
                String bmiCategoryStr = getBmiCategory(bmi);

                bmiResult.setText(String.format("Your BMI: %.2f", bmi));
                bmiCategory.setText(bmiCategoryStr);

                if (user != null) {
                    // Save BMI to SharedPreferences with user-specific key
                    SharedPreferences sharedPreferences = getSharedPreferences("FitnessAppPrefs", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putFloat(user.getUid() + "_bmi", (float) bmi);
                    editor.apply();
                    Toast.makeText(this, "BMI calculated and saved!", Toast.LENGTH_SHORT).show();
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Please enter valid numbers", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show();
        }
    }

    private String getBmiCategory(double bmi) {
        if (bmi < 18.5) return "Category: Underweight";
        else if (bmi < 24.9) return "Category: Normal weight";
        else if (bmi < 29.9) return "Category: Overweight";
        else return "Category: Obesity";
    }

    // Inside BmiCalculationActivity after calculating BMI
    private void saveDataToFirebase(float bmi, String weight, String age) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
        userRef.child("weight").setValue(weight);
        userRef.child("bmi").setValue(String.valueOf(bmi)); // Assuming BMI is a float
        userRef.child("age").setValue(age);
    }


    private void saveBmiData() {
        String name = nameInput.getText().toString();
        String gender = genderInput.getText().toString();
        String ageStr = ageInput.getText().toString();
        String heightStr = heightInput.getText().toString();
        String weightStr = weightInput.getText().toString();
        String bmiStr = bmiResult.getText().toString();

        if (!name.isEmpty() && !gender.isEmpty() && !ageStr.isEmpty() && !heightStr.isEmpty() && !weightStr.isEmpty()) {
            try {
                Map<String, Object> data = new HashMap<>();
                data.put("name", name);
                data.put("gender", gender);
                data.put("age", Integer.parseInt(ageStr));
                data.put("height", Double.parseDouble(heightStr));
                data.put("weight", Double.parseDouble(weightStr));

                String[] bmiParts = bmiStr.split(": ");
                if (bmiParts.length > 1) {
                    data.put("bmi", Double.parseDouble(bmiParts[1]));
                } else {
                    Toast.makeText(this, "BMI calculation error", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (user != null) {
                    db.collection("users").document(user.getUid()).set(data)
                            .addOnSuccessListener(unused ->
                                    Toast.makeText(BmiCalculationActivity.this, "BMI data saved!", Toast.LENGTH_SHORT).show()
                            )
                            .addOnFailureListener(e ->
                                    Toast.makeText(BmiCalculationActivity.this, "Failed to save data: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                            );
                } else {
                    Toast.makeText(this, "User is not authenticated", Toast.LENGTH_SHORT).show();
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Please enter valid numbers", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadBmiData() {
        if (user != null) {
            db.collection("users").document(user.getUid()).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            nameInput.setText(documentSnapshot.getString("name"));
                            genderInput.setText(documentSnapshot.getString("gender"));
                            ageInput.setText(String.valueOf(documentSnapshot.getLong("age")));
                            heightInput.setText(String.valueOf(documentSnapshot.getDouble("height")));
                            weightInput.setText(String.valueOf(documentSnapshot.getDouble("weight")));
                            double bmi = documentSnapshot.getDouble("bmi");
                            bmiResult.setText(String.format("Your BMI: %.2f", bmi));
                            bmiCategory.setText(getBmiCategory(bmi));
                        } else {
                            Toast.makeText(this, "No existing data found", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Failed to load data: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
        } else {
            Toast.makeText(this, "User is not authenticated", Toast.LENGTH_SHORT).show();
        }
    }
}
