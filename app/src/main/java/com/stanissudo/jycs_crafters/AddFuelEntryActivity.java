package com.stanissudo.jycs_crafters;

import static com.stanissudo.jycs_crafters.MainActivity.TAG;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Calendar;
import java.util.Locale;

import com.google.android.material.navigation.NavigationView;
import com.google.type.DateTime;
import com.stanissudo.jycs_crafters.database.FuelTrackAppRepository;
import com.stanissudo.jycs_crafters.database.entities.FuelEntry;
import com.stanissudo.jycs_crafters.databinding.ActivityAddFuelEntryBinding;
import com.stanissudo.jycs_crafters.utils.CarSelectorHelper;

public class AddFuelEntryActivity extends AppCompatActivity {

    private ActivityAddFuelEntryBinding binding;
    //private static final String FUEL_ENTRY_USER_ID = "com.stanissudo.gymlog.FUEL_ENTRY_USER_ID";
    FuelTrackAppRepository repository = FuelTrackAppRepository.getRepository(getApplication());
    // int loggedInUserId = -1;
    private int odometer = -1;
    private double gasGal = -1.0;
    private double pricePerGal = -1.0;
    private LocalDateTime dateTime;
    private int priority = 1;
    private int gasVolumePriority = 0;
    private int pricePerGalPriority = 0;
    private int totalPricePriority = 0;
    private boolean isUpdating = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddFuelEntryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.gasVolumeInputEditText.addTextChangedListener(volumeWatcher);
        binding.pricePerGallonInputEditText.addTextChangedListener(pricePerGalWatcher);
        binding.totalPriceInputEditText.addTextChangedListener(totalPriceWatcher);

        //loggedInUserId = getIntent().getIntExtra(FUEL_ENTRY_USER_ID, -1);

        AutoCompleteTextView carSelectorDropdown = binding.toolbarDropdown;
        CarSelectorHelper.setupDropdown(this, carSelectorDropdown);

        setSupportActionBar(binding.toolbar);
        Calendar calendar = Calendar.getInstance();
        // Format date and time
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
        SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.US);

        binding.editTextDateFuelEntry.setText(dateFormat.format(calendar.getTime()));
        binding.editTextTimeFuelEntry.setText(timeFormat.format(calendar.getTime()));

        binding.editTextDateFuelEntry.setOnClickListener(v -> {
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    this,
                    (view, y, m, d) -> {
                        // Months are 0-based in Calendar
                        calendar.set(Calendar.YEAR, y);
                        calendar.set(Calendar.MONTH, m);
                        calendar.set(Calendar.DAY_OF_MONTH, d);
                        binding.editTextDateFuelEntry.setText(dateFormat.format(calendar.getTime()));
                    },
                    year, month, day
            );
            datePickerDialog.show();
        });

        binding.editTextTimeFuelEntry.setOnClickListener(v -> {
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            TimePickerDialog timePickerDialog = new TimePickerDialog(
                    this,
                    (view, h, m) -> {
                        calendar.set(Calendar.HOUR_OF_DAY, h);
                        calendar.set(Calendar.MINUTE, m);
                        binding.editTextTimeFuelEntry.setText(timeFormat.format(calendar.getTime()));
                    },
                    hour, minute, true // true = 24-hour
            );
            timePickerDialog.show();
        });

        binding.saveEntryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(AddFuelEntryActivity.this, "It worked!", Toast.LENGTH_SHORT).show();
                getInformationFromDisplay();
                insertRecord();
                Intent intent = MainActivity.mainActivityIntentFactory(getApplicationContext(), -1);
                startActivity(intent);
                // updateDisplay();
            }
        });

    }

    private void getInformationFromDisplay() {
        String odometerText = binding.odometerInputEditText.getText().toString().trim();
        if (!odometerText.isEmpty()) {
            try {
                odometer = Integer.parseInt(odometerText);
            } catch (NumberFormatException e) {
                Log.d(TAG, "Invalid number format in Odometer input");
            }
        } else {
            Log.d(TAG, "Odometer input is empty");
            Toast.makeText(AddFuelEntryActivity.this, "Odometer cannot be empty!", Toast.LENGTH_SHORT).show();
        }
        String gasVolumeText = binding.gasVolumeInputEditText.getText().toString().trim();
        if (!gasVolumeText.isEmpty()) {
            try {
                gasGal = Double.parseDouble(gasVolumeText);
            } catch (NumberFormatException e) {
                Log.d(TAG, "Error reading value from Gas Gallons edit text.");
            }
        } else {
            Log.d(TAG, "Gas Volume input is empty");
            Toast.makeText(AddFuelEntryActivity.this, "Gas Volume cannot be empty!", Toast.LENGTH_SHORT).show();
        }
        String gasPricePerGalText = binding.pricePerGallonInputEditText.getText().toString().trim();
        if (!gasPricePerGalText.isEmpty()) {
            try {
                pricePerGal = Double.parseDouble(gasPricePerGalText);
            } catch (NumberFormatException e) {
                Log.d(TAG, "Error reading value from Gas Price per Gallon edit text.");
            }
        } else {
            Log.d(TAG, "Gas Price per Gallon input is empty");
            Toast.makeText(AddFuelEntryActivity.this, "Gas Price per Gallon cannot be empty!", Toast.LENGTH_SHORT).show();
        }

// 📅 Read date & time fields and assign to LocalDateTime
        String dateStr = binding.editTextDateFuelEntry.getText().toString().trim();
        String timeStr = binding.editTextTimeFuelEntry.getText().toString().trim();

        if (!dateStr.isEmpty() && !timeStr.isEmpty()) {
            try {
                // Match the format used in display
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy h:mm a", Locale.US);
                dateTime = LocalDateTime.parse(dateStr + " " + timeStr, formatter);

            } catch (DateTimeParseException e) {
                Log.d(TAG, "Invalid date/time format", e);
            }
        } else {
            Log.d(TAG, "Date or Time is empty");
            Toast.makeText(this, "Date and Time cannot be empty!", Toast.LENGTH_SHORT).show();
        }
    }

    private void insertRecord() {
        int selectedCarId = -1;
        try {
            selectedCarId = CarSelectorHelper.getSelectedOptionKey();
            if (selectedCarId == -1) {
                throw new IllegalArgumentException("Invalid selected car ID");
            }
        } catch (Exception e) {
            Log.d(TAG, "Error getting selected car ID", e);
            Toast.makeText(this, "Error creating a record. Did you select your Car?", Toast.LENGTH_SHORT).show();
            return;
        }
        FuelEntry fuelLog = new FuelEntry(selectedCarId, odometer, gasGal, pricePerGal, dateTime);
        repository.insertFuelEntry(fuelLog);
    }


    static Intent addFuelEntryIntentFactory(Context context, int userId) {
        Intent intent = new Intent(context, AddFuelEntryActivity.class);
        //intent.putExtra(FUEL_ENTRY_USER_ID, userId);
        return intent;
    }

    @Override
    protected void onResume() {
        super.onResume();
        CarSelectorHelper.updateDropdownText(binding.toolbarDropdown);
    }

    private final TextWatcher volumeWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (isUpdating) return;
            isUpdating = true;
            try {
                gasVolumePriority = priority++;
                if (pricePerGalPriority < totalPricePriority) {
                    updatePricePerGallon();
                } else {
                    updateTotalPrice();
                }
            } finally {
                isUpdating = false;
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };

    private final TextWatcher pricePerGalWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (isUpdating) return;
            isUpdating = true;
            try {
                pricePerGalPriority = priority++;
                if (gasVolumePriority < totalPricePriority) {
                    updateGallons();
                } else {
                    updateTotalPrice();
                }
            } finally {
                isUpdating = false;
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };

    private final TextWatcher totalPriceWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (isUpdating) return;
            isUpdating = true;
            try {
                totalPricePriority = priority++;
                if (pricePerGalPriority < gasVolumePriority) {
                    updatePricePerGallon();
                } else {
                    updateGallons();
                }

            } finally {
                isUpdating = false;
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };
//    private final TextWatcher simpleWatcher = new TextWatcher() {
//        @Override
//        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
//
//        @Override
//        public void onTextChanged(CharSequence s, int start, int before, int count) {
//            updateTotalPrice();
//        }
//
//        @Override
//        public void afterTextChanged(Editable s) {}
//    };

    private void updateGallons() {
        String pricePerGalStr = binding.pricePerGallonInputEditText.getText().toString().trim();
        String totalPriceStr = binding.totalPriceInputEditText.getText().toString().trim();

        if (!pricePerGalStr.isEmpty() && !totalPriceStr.isEmpty()) {
            try {
                double pricePerGal = Double.parseDouble(pricePerGalStr);
                double totalPrice = Double.parseDouble(totalPriceStr);
                double gasVolume = totalPrice / pricePerGal;

                // Optional: round to 2 decimal places
                binding.gasVolumeInputEditText.setText(String.format(Locale.US, "%.2f", gasVolume));
            } catch (NumberFormatException e) {
                // Ignore if invalid numbers are entered
                binding.gasVolumeInputEditText.setText("");
            }
        } else {
            // Clear if one of them is empty
            binding.gasVolumeInputEditText.setText("");
        }
    }

    private void updatePricePerGallon() {
        String gasVolumeStr = binding.gasVolumeInputEditText.getText().toString().trim();
        String totalPriceStr = binding.totalPriceInputEditText.getText().toString().trim();

        if (!gasVolumeStr.isEmpty() && !totalPriceStr.isEmpty()) {
            try {
                double gasVol = Double.parseDouble(gasVolumeStr);
                double totalPrice = Double.parseDouble(totalPriceStr);
                double pricePerGal = totalPrice / gasVol;

                // Optional: round to 2 decimal places
                binding.pricePerGallonInputEditText.setText(String.format(Locale.US, "%.2f", pricePerGal));
            } catch (NumberFormatException e) {
                // Ignore if invalid numbers are entered
                binding.pricePerGallonInputEditText.setText("");
            }
        } else {
            // Clear if one of them is empty
            binding.pricePerGallonInputEditText.setText("");
        }
    }

    private void updateTotalPrice() {
        String gasVolumeStr = binding.gasVolumeInputEditText.getText().toString().trim();
        String pricePerGalStr = binding.pricePerGallonInputEditText.getText().toString().trim();

        if (!gasVolumeStr.isEmpty() && !pricePerGalStr.isEmpty()) {
            try {
                double gasVol = Double.parseDouble(gasVolumeStr);
                double pricePerGal = Double.parseDouble(pricePerGalStr);
                double total = gasVol * pricePerGal;

                // Optional: round to 2 decimal places
                binding.totalPriceInputEditText.setText(String.format(Locale.US, "%.2f", total));
            } catch (NumberFormatException e) {
                // Ignore if invalid numbers are entered
                binding.totalPriceInputEditText.setText("");
            }
        } else {
            // Clear if one of them is empty
            binding.totalPriceInputEditText.setText("");
        }
    }
}
