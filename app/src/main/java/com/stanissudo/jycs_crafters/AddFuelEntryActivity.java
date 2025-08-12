package com.stanissudo.jycs_crafters;

import static com.stanissudo.jycs_crafters.MainActivity.TAG;

import static java.lang.String.format;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.type.DateTime;
import com.stanissudo.jycs_crafters.database.FuelTrackAppRepository;
import com.stanissudo.jycs_crafters.database.entities.FuelEntry;
import com.stanissudo.jycs_crafters.databinding.ActivityAddFuelEntryBinding;
import com.stanissudo.jycs_crafters.utils.CarSelectorHelper;
import com.stanissudo.jycs_crafters.viewHolders.FuelEntryViewModel;

public class AddFuelEntryActivity extends AppCompatActivity {

    private ActivityAddFuelEntryBinding binding;
    //private static final String FUEL_ENTRY_USER_ID = "com.stanissudo.gymlog.FUEL_ENTRY_USER_ID";

    //Used to Insert/Update records------------------
    public static final String EXTRA_LOG_ID = "EXTRA_LOG_ID";
    public static final String EXTRA_USER_ID = "EXTRA_USER_ID";

    public static Intent editIntentFactory(Context ctx, int userId, int logId) {
        Intent i = new Intent(ctx, AddFuelEntryActivity.class);
        i.putExtra(EXTRA_USER_ID, userId);
        i.putExtra(EXTRA_LOG_ID, logId);
        return i;
    }

    public static Intent addIntentFactory(Context ctx, int userId) {
        Intent i = new Intent(ctx, AddFuelEntryActivity.class);
        i.putExtra(EXTRA_USER_ID, userId);
        return i;
    }
    private FuelEntryViewModel vm;
    private boolean isEdit;
    private int editLogId;
    //------------------------------------------------

    FuelTrackAppRepository repository;
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

        //Add cancel icon
        setSupportActionBar(binding.toolbar);
        // Click = cancel/close
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        repository = FuelTrackAppRepository.getRepository(getApplication());

        binding.gasVolumeInputEditText.addTextChangedListener(volumeWatcher);
        binding.pricePerGallonInputEditText.addTextChangedListener(pricePerGalWatcher);
        binding.totalPriceInputEditText.addTextChangedListener(totalPriceWatcher);

        //loggedInUserId = getIntent().getIntExtra(FUEL_ENTRY_USER_ID, -1);

        AutoCompleteTextView carSelectorDropdown = binding.toolbarDropdown;
        CarSelectorHelper.setupDropdown(this, carSelectorDropdown);

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
            }
        });

//Used to Insert/Update records------------------
        vm = new ViewModelProvider(this).get(FuelEntryViewModel.class);

        editLogId = getIntent().getIntExtra(EXTRA_LOG_ID, -1);
        isEdit = editLogId > 0;

        if (isEdit) {
            setTitle("Edit Fuel Entry");
            vm.getById(editLogId).observe(this, e -> {
                if (e == null) return;
                // Prefill UI — adjust to your actual field names/ids
                binding.odometerInputEditText.setText(String.valueOf(e.getOdometer()));
                binding.gasVolumeInputEditText.setText(String.valueOf(e.getGallons()));
                binding.pricePerGallonInputEditText.setText(String.valueOf(e.getPricePerGallon()));
                binding.totalPriceInputEditText.setText(String.valueOf(e.getTotalCost()));
                // date picker / car dropdown if you have them:
                 binding.editTextDateFuelEntry.setText(format(e.getLogDate().toString()));
                 CarSelectorHelper.setSelectedOption(this, e.getCarID().toString());
            });
        } else {
            setTitle("Add Fuel Entry");
        }

        binding.saveEntryButton.setOnClickListener(v -> onSave());
        binding.toolbar.setNavigationOnClickListener(v -> finish());
        //binding.cancelButton.setOnClickListener(v -> finish());
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

    private boolean insertRecord() {
        int selectedCarId = -1;
        try {
            selectedCarId = CarSelectorHelper.getSelectedOptionKey();
            if (selectedCarId == -1) {
                throw new IllegalArgumentException("Invalid selected car ID");
            }
        } catch (Exception e) {
            Log.d(TAG, "Error getting selected car ID", e);
            Toast.makeText(this, "Error creating a record. Did you select your Car?", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (odometer < 0 || gasGal < 0 || pricePerGal < 0) {
            Log.d(TAG, "odometer, gasGal, pricePerGal must be positive");
            Toast.makeText(this, "Missing value.", Toast.LENGTH_SHORT).show();
            return false;
        }
        int carId = selectedCarId;
        repository.checkOdometerAsync(carId, dateTime, odometer, (ok, prev, next) -> {
            if (ok) {
                FuelEntry fuelLog = new FuelEntry(carId, odometer, gasGal, pricePerGal, dateTime);
                repository.insertFuelEntry(fuelLog);
                Intent intent = MainActivity.mainActivityIntentFactory(getApplicationContext(), -1);
                startActivity(intent);
            } else {
                String msg;
                if (prev != null && next != null) {
                    msg = "Odometer must be > " + prev + " and < " + next + ".";
                } else if (prev != null) {
                    msg = "Odometer must be > " + prev + ".";
                } else if (next != null) {
                    msg = "Odometer must be < " + next + ".";
                } else {
                    msg = "Couldn’t validate odometer.";
                }
                Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
            }
        });
        return true;
    }

//    private boolean isValidOdometer(){
//        boolean result;
//        int selectedCarId = -1;
//        try {
//            selectedCarId = CarSelectorHelper.getSelectedOptionKey();
//            if (selectedCarId == -1) {
//                throw new IllegalArgumentException("Invalid selected car ID");
//            }
//        } catch (Exception e) {
//            Log.d(TAG, "Error getting selected car ID", e);
//            Toast.makeText(this, "Error creating a record. Did you select your Car?", Toast.LENGTH_SHORT).show();
//            return false;
//        }
//        final Integer carId = selectedCarId;
//        ExecutorService io = Executors.newSingleThreadExecutor();
//
//        io.execute(() -> {
//            Integer prev = repository.getPreviousOdometer(carId, dateTime).getValue();
//            Integer next = repository.getNextOdometer(carId, dateTime).getValue();
//
//            boolean ok =
//                    (prev == null || odometer > prev) &&
//                            (next == null || odometer < next);
//
//            runOnUiThread(() -> {
//                if (ok) {
//                    result = true;
//                } else {
//                    // error
//                }
//            });
//        });
//    }

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

    private void updateGallons() {
        String pricePerGalStr = binding.pricePerGallonInputEditText.getText().toString().trim();
        String totalPriceStr = binding.totalPriceInputEditText.getText().toString().trim();

        if (!pricePerGalStr.isEmpty() && !totalPriceStr.isEmpty()) {
            try {
                double pricePerGal = Double.parseDouble(pricePerGalStr);
                double totalPrice = Double.parseDouble(totalPriceStr);
                double gasVolume = totalPrice / pricePerGal;

                // Optional: round to 2 decimal places
                binding.gasVolumeInputEditText.setText(format(Locale.US, "%.3f", gasVolume));
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
                binding.pricePerGallonInputEditText.setText(format(Locale.US, "%.2f", pricePerGal));
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
                binding.totalPriceInputEditText.setText(format(Locale.US, "%.2f", total));
            } catch (NumberFormatException e) {
                // Ignore if invalid numbers are entered
                binding.totalPriceInputEditText.setText("");
            }
        } else {
            // Clear if one of them is empty
            binding.totalPriceInputEditText.setText("");
        }
    }
    //Used to Insert/Update records------------------
    private void onSave() {
//        // Gather values from UI, validate, then build entity
        int odo = safeInt(binding.odometerInputEditText.getText().toString());
        double gallons = safeDouble(binding.gasVolumeInputEditText.getText().toString());
        double price = safeDouble(binding.pricePerGallonInputEditText.getText().toString());
        double total = safeDouble(binding.totalPriceInputEditText.getText().toString());
//        // also gather date, carId, notes, etc.
        int selectedCarId = -1;
        try {
            selectedCarId = CarSelectorHelper.getSelectedOptionKey();
            if (selectedCarId == -1) {
                throw new IllegalArgumentException("Invalid selected car ID");
            }
        } catch (Exception e) {
            Log.d(TAG, "Error getting selected car ID", e);
            Toast.makeText(this, "Error creating a record. Did you select your Car?", Toast.LENGTH_SHORT).show();
        }
        if (odometer < 0 || gasGal < 0 || pricePerGal < 0) {
            Log.d(TAG, "odometer, gasGal, pricePerGal must be positive");
            Toast.makeText(this, "Missing value.", Toast.LENGTH_SHORT).show();
        }
        int carId = selectedCarId;


        FuelEntry e = new FuelEntry();
        if (isEdit) e.setLogID(editLogId); // <- keep same PK when updating
        e.setOdometer(odo);
        e.setGallons(gallons);
        e.setPricePerGallon(price);
        e.setTotalCost(total);
        e.setCarID(carId);
        // set carId/date/notes...

        if (isEdit) {
            vm.update(e);
        } else {
            vm.insert(e);
        }
        finish(); // Room LiveData will refresh your list automatically
    }

    private int safeInt(String s){ try { return Integer.parseInt(s); } catch(Exception e){ return 0; } }
    private double safeDouble(String s){ try { return Double.parseDouble(s); } catch(Exception e){ return 0.0; } }
}
