package com.stanissudo.jycs_crafters;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.stanissudo.jycs_crafters.database.FuelTrackAppRepository;
import com.stanissudo.jycs_crafters.databinding.ActivityVehicleBinding;

/**
 * @author Ysabelle Kim
 * created: 8/1/2025 - 6:42 PM
 * @project JYCS-Crafters
 * file: VehicleActivity.java
 * @since 1.0.0
 * Explanation: VehicleActivity handles adding new vehicles to a user's account
 */
public class VehicleActivity extends AppCompatActivity {
    private com.stanissudo.jycs_crafters.databinding.ActivityVehicleBinding binding;

    private FuelTrackAppRepository repository;

    /**
     * onCreate() creates Vehicle activity to add vehicles
     * @param savedInstanceState Bundle object
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityVehicleBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // TODO: remake to use Repository
        // repository = GymLogRepository.getRepository(getApplication());

        // TODO: remake to use vehicleLogButton
        /*binding.loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verifyUser();
            }
        });*/
    }

    /**
     * getInformationFromDisplay() sets the user's input from application fields to variables
     */
    private void getInformationFromDisplay() {
        // TODO: implement try-catch blocks for user input
        /*mExercise = binding.exerciseInputEditText.getText().toString();
        try {
            mWeight = Double.parseDouble(binding.weightInputEditText.getText().toString());
        } catch (NumberFormatException e) {
            Log.e(TAG, "Error reading value from Weight edit text.");
        }
        try {
            mReps = Integer.parseInt(binding.repInputEditText.getText().toString());
        } catch (NumberFormatException e) {
            Log.e(TAG, "Error reading value from Reps edit text.");
        }*/
    }

    /**
     * vehicleIntentFactory() returns an intent to change the screen
     * @param context context
     * @return intent
     */
    static Intent vehicleIntentFactory(Context context) {
        return new Intent(context, VehicleActivity.class);
    }
}