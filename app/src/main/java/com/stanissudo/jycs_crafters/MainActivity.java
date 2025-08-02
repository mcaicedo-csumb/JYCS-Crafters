package com.stanissudo.jycs_crafters;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.stanissudo.jycs_crafters.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private static final String MAIN_ACTIVITY_USER_ID = "com.example.wk05hw03_gymlog.MAIN_ACTIVITY_USER_ID";

    private ActivityMainBinding binding;

    // change to be the Repository type
    //private <<GasAppRepository>> repository;

    /**
     * onCreate() creates MainActivity activity
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // TODO: implement click button, "Add Vehicle", and call vehicleIntentFactory
        /*binding.logButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getInformationFromDisplay();
                //insertGymLogRecord();
            }
        });*/
    }

    /**
     * mainActivityIntentFactory returns an intent to change the screen
     *
     * @param context context
     * @param userID  userID
     * @return intent
     */
    static Intent mainActivityIntentFactory(Context context, int userID) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra(MAIN_ACTIVITY_USER_ID, userID);
        return intent;
    }
}