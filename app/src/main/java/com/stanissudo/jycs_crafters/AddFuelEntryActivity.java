package com.stanissudo.jycs_crafters;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.stanissudo.jycs_crafters.database.FuelTrackAppRepository;
import com.stanissudo.jycs_crafters.database.entities.FuelEntry;
import com.stanissudo.jycs_crafters.databinding.ActivityAddFuelEntryBinding;
import com.stanissudo.jycs_crafters.utils.CarSelectorHelper;
import com.stanissudo.jycs_crafters.utils.DecimalDigitsInputFilter;
import com.stanissudo.jycs_crafters.viewHolders.FuelEntryViewModel;
import com.stanissudo.jycs_crafters.viewHolders.SharedViewModel;
import com.stanissudo.jycs_crafters.viewHolders.VehicleViewModel;
import com.stanissudo.jycs_crafters.utils.NumberFormatter;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Calendar;
import java.util.Locale;

/**
 *  * @author Stan Permiakov
 *  * created: 8/12/2025
 *  * @project JYCS-Crafters
 *  * file: AddFuelEntryActivity.java
 *  *
 * Activity for creating or editing a single fuel log entry.
 * <p>
 * This screen supports two modes:
 * <ul>
 *   <li><b>ADD</b> — Create a brand new {@link FuelEntry}. Launch via
 *       {@link #addFuelIntentFactory(Context, int)}.</li>
 *   <li><b>EDIT</b> — Modify an existing entry. Launch via
 *       {@link #editIntentFactory(Context, int, int)} with the entry's LogID.</li>
 * </ul>
 *
 * <h3>Auto-calculation behavior</h3>
 * Three TextWatchers keep the following fields in sync:
 * <ul>
 *   <li>Gallons</li>
 *   <li>Price per Gallon</li>
 *   <li>Total</li>
 * </ul>
 * The most recently edited field gets higher <em>priority</em>; dependent values are recalculated
 * from it to avoid circular updates. Two boolean guards remove re-entrant churn:
 * <ul>
 *   <li>{@link #suppressWatchers} — Temporarily mute watchers during programmatic field fills
 *       (e.g., when preloading in EDIT mode).</li>
 *   <li>{@link #isUpdating} — Prevent recursive updates while a watcher is actively writing.</li>
 * </ul>
 *
 * <h3>Date & Time handling</h3>
 * Uses {@link java.time} to maintain a single canonical {@link #recordTimeStamp}. Two inputs (date
 * and time) write back into the same timestamp so persisted data is always consistent.
 */
public class AddFuelEntryActivity extends AppCompatActivity {

    /** ViewBinding for this Activity's layout. */
    private ActivityAddFuelEntryBinding binding;

    // --------------------------------------------------------------------------------------------
    // Intent extras
    // --------------------------------------------------------------------------------------------

    /** Intent extra key: primary key of a {@link FuelEntry} to edit. Only present in EDIT mode. */
    public static final String EXTRA_LOG_ID  = "EXTRA_LOG_ID";
    /** Intent extra key: the selected car ID this entry belongs to. */
    public static final String EXTRA_CAR_ID = "EXTRA_CAR_ID";

    // --------------------------------------------------------------------------------------------
    // ViewModels & data access
    // --------------------------------------------------------------------------------------------

    /** Screen-scoped ViewModel for CRUD on {@link FuelEntry} objects. */
    private FuelEntryViewModel viewModel;
    /** Provides list of user's vehicles for the top toolbar dropdown. */
    private VehicleViewModel vehicleViewModel;
    /** Shared selection model for currently chosen car in the toolbar dropdown. */
    private SharedViewModel sharedViewModel;

    /** True if this Activity was launched to edit an existing record. */
    private boolean isEdit;
    /** The LogID of the record being edited. Only meaningful if {@link #isEdit} is true. */
    private int editLogId;
    /** The car this entry is associated with (from the calling context). */
    private int carId;

    /** Repository used for asynchronous validation (e.g., odometer sanity check). */
    private FuelTrackAppRepository repository;

    // --------------------------------------------------------------------------------------------
    // Watcher guards & priority system
    // --------------------------------------------------------------------------------------------

    /**
     * Set true to temporarily mute TextWatchers during programmatic {@code setText} calls.
     * Prevents accidental recalculation when hydrating UI from DB.
     */
    private boolean suppressWatchers = false;

    /** Set true while a watcher is actively writing, to avoid recursive onTextChanged loops. */
    private boolean isUpdating = false;

    /**
     * Monotonically increasing counter used to decide which of the three pricing fields was
     * modified most recently; higher value = higher priority.
     */
    private int priority = 1;
    private int gasVolumePriority = 0;
    private int pricePerGalPriority = 0;
    private int totalPricePriority = 0;

    // --------------------------------------------------------------------------------------------
    // Date / time state
    // --------------------------------------------------------------------------------------------

    /** Canonical timestamp of this record (combined date + time from the two UI controls). */
    private LocalDateTime recordTimeStamp;

    /** Formatter for the date input field (UI only). */
    private static final DateTimeFormatter UI_DATE_FMT =
            DateTimeFormatter.ofPattern("MM/dd/yyyy", Locale.US);
    /** Formatter for the time input field (UI only, shown as 12-hour). */
    private static final DateTimeFormatter UI_TIME_FMT =
            DateTimeFormatter.ofPattern("h:mm a", Locale.US);
    /** Parser used when persisting merged date + time back into {@link #recordTimeStamp}. */
    private static final DateTimeFormatter UI_DATE_TIME_PARSE =
            DateTimeFormatter.ofPattern("MM/dd/yyyy h:mm a", Locale.US);

    // --------------------------------------------------------------------------------------------
    // Intent factories
    // --------------------------------------------------------------------------------------------

    /**
     * Build an {@link Intent} to open this Activity in ADD mode for a given car.
     *
     * @param context Caller context
     * @param carId   Car ID the new entry will belong to
     * @return Intent ready to pass to {@link Context#startActivity(Intent)}
     */
    public static Intent addFuelIntentFactory(Context context, int carId) {
        return new Intent(context, AddFuelEntryActivity.class)
                .putExtra(EXTRA_CAR_ID, carId);
    }

    /**
     * Build an {@link Intent} to open this Activity in EDIT mode.
     *
     * @param context Caller context
     * @param carId   Car ID the entry belongs to
     * @param logId   Primary key of the existing {@link FuelEntry}
     * @return Intent ready to pass to {@link Context#startActivity(Intent)}
     */
    public static Intent editIntentFactory(Context context, int carId, int logId) {
        return new Intent(context, AddFuelEntryActivity.class)
                .putExtra(EXTRA_CAR_ID, carId)
                .putExtra(EXTRA_LOG_ID, logId);
    }

    // --------------------------------------------------------------------------------------------
    // Lifecycle
    // --------------------------------------------------------------------------------------------

    /**
     * Standard Activity lifecycle entry point. Wires ViewModels, dropdown, pickers, watchers, and
     * pre-fills UI in EDIT mode.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // For AppCompatActivity, call super FIRST.
        super.onCreate(savedInstanceState);

        // Inflate view binding and set content view.
        binding = ActivityAddFuelEntryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Gallons: up to 3 decimals and up to 3 digits before the dot
        binding.gasVolumeInputEditText.setFilters(new InputFilter[]{
                new DecimalDigitsInputFilter(3, 3)
        });

        // Price/gal: up to 2 decimals (and up to 2 digits before the dot)
        binding.pricePerGallonInputEditText.setFilters(new InputFilter[]{
                new DecimalDigitsInputFilter(2, 2)
        });

        // TotalPrice: up to 2 decimals (and up to 4 digits before the dot)
        binding.totalPriceInputEditText.setFilters(new InputFilter[]{
                new DecimalDigitsInputFilter(4, 2)
        });

        // Toolbar back button behavior is defined in XML; this just finishes the Activity.
        setSupportActionBar(binding.toolbar);
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        // --- 1) Initialize ViewModels ---
        vehicleViewModel = new ViewModelProvider(this).get(VehicleViewModel.class);
        sharedViewModel  = new ViewModelProvider(this).get(SharedViewModel.class);
        viewModel        = new ViewModelProvider(this).get(FuelEntryViewModel.class);
        repository       = FuelTrackAppRepository.getRepository(getApplication());

        // Extract intent extras.
        carId = getIntent().getIntExtra(EXTRA_CAR_ID, -1);
        editLogId = getIntent().getIntExtra(EXTRA_LOG_ID, -1);
        isEdit = editLogId > 0;
        setTitle(isEdit ? "Edit Fuel Entry" : "Add Fuel Entry");

        // Load vehicles for the dropdown and set initial selection.
        SharedPreferences sharedPreferences = getSharedPreferences("login_prefs", MODE_PRIVATE);
        vehicleViewModel.loadUserVehicles(sharedPreferences.getInt("userId", -1));
        vehicleViewModel.getUserVehicles().observe(this, vehicles -> {
            if (vehicles != null && !vehicles.isEmpty()) {
                // (a) Push fresh list into helper (may update its cache)
                CarSelectorHelper.loadVehicleData(this, vehicles);

                // (b) Recreate dropdown adapter with fresh list
                AutoCompleteTextView carSelectorDropdown = binding.toolbarDropdown;
                //CarSelectorHelper.setupDropdown(this, carSelectorDropdown);
                CarSelectorHelper.setupDropdown(this, carSelectorDropdown, id -> {
                    if (id != -1) sharedViewModel.selectCar(id); // emits to fragments
                });

                // (c) Pick initial selection and propagate it
                Integer initialCarId = CarSelectorHelper.getSelectedOptionKey();
                if (initialCarId != -1) {
                    sharedViewModel.selectCar(initialCarId);
                }
            }
        });

        // Initialize date/time inputs to "now". These will be overridden in EDIT mode below.
        Calendar calendarNow = Calendar.getInstance();
        recordTimeStamp = LocalDateTime.ofInstant(calendarNow.toInstant(), ZoneId.systemDefault());
        binding.editTextDateFuelEntry.setText(UI_DATE_FMT.format(recordTimeStamp));
        binding.editTextTimeFuelEntry.setText(UI_TIME_FMT.format(recordTimeStamp));

        // Date picker: updates only the date portion of {@link #recordTimeStamp}.
        binding.editTextDateFuelEntry.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            // Start the picker on the currently selected date.
            c.set(recordTimeStamp.getYear(), recordTimeStamp.getMonthValue() - 1, recordTimeStamp.getDayOfMonth());

            new DatePickerDialog(
                    this,
                    (view, y, m, d) -> {
                        // Keep time; replace date.
                        recordTimeStamp = recordTimeStamp.withYear(y).withMonth(m + 1).withDayOfMonth(d);
                        binding.editTextDateFuelEntry.setText(UI_DATE_FMT.format(recordTimeStamp));
                    },
                    c.get(Calendar.YEAR),
                    c.get(Calendar.MONTH),
                    c.get(Calendar.DAY_OF_MONTH)
            ).show();
        });

        // Time picker: updates only the time portion of {@link #recordTimeStamp}.
        binding.editTextTimeFuelEntry.setOnClickListener(v -> {
            new TimePickerDialog(
                    this,
                    (view, h, m) -> {
                        // Keep date; replace time.
                        recordTimeStamp = recordTimeStamp.withHour(h).withMinute(m).withSecond(0).withNano(0);
                        binding.editTextTimeFuelEntry.setText(UI_TIME_FMT.format(recordTimeStamp));
                    },
                    recordTimeStamp.getHour(),
                    recordTimeStamp.getMinute(),
                    true // 24-hour picker; display remains 12h via UI_TIME_FMT
            ).show();
        });

        // Attach watchers AFTER initial setText so they don't fire during setup.
        binding.gasVolumeInputEditText.addTextChangedListener(volumeWatcher);
        binding.pricePerGallonInputEditText.addTextChangedListener(pricePerGalWatcher);
        binding.totalPriceInputEditText.addTextChangedListener(totalPriceWatcher);

        // EDIT mode: prefill from DB without triggering calculations.
        if (isEdit) {
            suppressWatchers = true;
            viewModel.getById(editLogId).observe(this, e -> {
                if (e == null) return;

                // Prefill numeric fields.
                binding.odometerInputEditText.setText(String.valueOf(e.getOdometer()));
                binding.gasVolumeInputEditText.setText(NumberFormatter.upTo3(e.getGallons()));
                binding.pricePerGallonInputEditText.setText(NumberFormatter.upTo2(e.getPricePerGallon()));
                binding.totalPriceInputEditText.setText(NumberFormatter.upTo2(e.getTotalCost()));

                // Prefill date/time from entity.
                LocalDateTime ldt = e.getLogDate();
                if (ldt != null) {
                    recordTimeStamp = ldt;
                    binding.editTextDateFuelEntry.setText(UI_DATE_FMT.format(ldt));
                    binding.editTextTimeFuelEntry.setText(UI_TIME_FMT.format(ldt));
                }

                // Prefill/lock car selection (helper may choose by ID or by name internally).
                try {
                    CarSelectorHelper.setSelectedOptionById(AddFuelEntryActivity.this, carId);
                } catch (Exception ignore) { /* helper may expect names; best-effort */ }
                binding.toolbarDropdown.setEnabled(false); // keep car immutable during edit

                suppressWatchers = false;
            });
        }

        // Save action: validate + async odometer check + insert/update.
        binding.saveEntryButton.setOnClickListener(v -> onSave());
    }

    /** Keep the dropdown's visible text in sync with persisted selection. */
    @Override
    protected void onResume() {
        super.onResume();
        CarSelectorHelper.updateDropdownText(binding.toolbarDropdown);
    }

    // --------------------------------------------------------------------------------------------
    // TextWatchers
    // --------------------------------------------------------------------------------------------

    /** Watches Gallons input and updates dependent fields according to priority rules. */
    private final TextWatcher volumeWatcher = new TextWatcher() {
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
        @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (isUpdating || suppressWatchers) return;
            isUpdating = true;
            try {
                gasVolumePriority = priority++;
                if (pricePerGalPriority < totalPricePriority) {
                    updatePricePerGallon();
                } else {
                    updateTotalPrice();
                }
            } finally { isUpdating = false; }
        }
        @Override public void afterTextChanged(Editable s) { }
    };

    /** Watches Price/Gal input and updates dependent fields according to priority rules. */
    private final TextWatcher pricePerGalWatcher = new TextWatcher() {
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
        @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (isUpdating || suppressWatchers) return;
            isUpdating = true;
            try {
                pricePerGalPriority = priority++;
                if (gasVolumePriority < totalPricePriority) {
                    updateGallons();
                } else {
                    updateTotalPrice();
                }
            } finally { isUpdating = false; }
        }
        @Override public void afterTextChanged(Editable s) { }
    };

    /** Watches Total input and updates dependent fields according to priority rules. */
    private final TextWatcher totalPriceWatcher = new TextWatcher() {
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
        @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (isUpdating || suppressWatchers) return;
            isUpdating = true;
            try {
                totalPricePriority = priority++;
                if (pricePerGalPriority < gasVolumePriority) {
                    updatePricePerGallon();
                } else {
                    updateGallons();
                }
            } finally { isUpdating = false; }
        }
        @Override public void afterTextChanged(Editable s) { }
    };

    // --------------------------------------------------------------------------------------------
    // Watcher helpers
    // --------------------------------------------------------------------------------------------

    /**
     * Derive Gallons from Total and Price/Gal.
     * <p>If either input is empty or unparsable, clears the Gallons field.</p>
     */
    private void updateGallons() {
        String pricePerGalStr = text(binding.pricePerGallonInputEditText);
        String totalPriceStr  = text(binding.totalPriceInputEditText);

        if (!pricePerGalStr.isEmpty() && !totalPriceStr.isEmpty()) {
            try {
                double pricePerGal = Double.parseDouble(pricePerGalStr);
                double totalPrice  = Double.parseDouble(totalPriceStr);
                double gasVolume   = totalPrice / Math.max(pricePerGal, 0.000001d);
                binding.gasVolumeInputEditText.setText(NumberFormatter.upTo3(gasVolume));
            } catch (NumberFormatException e) {
                binding.gasVolumeInputEditText.setText("");
            }
        } else {
            binding.gasVolumeInputEditText.setText("");
        }
    }

    /**
     * Derive Price/Gal from Total and Gallons.
     * <p>If either input is empty or unparsable, clears the Price/Gal field.</p>
     */
    private void updatePricePerGallon() {
        String gasVolumeStr   = text(binding.gasVolumeInputEditText);
        String totalPriceStr  = text(binding.totalPriceInputEditText);

        if (!gasVolumeStr.isEmpty() && !totalPriceStr.isEmpty()) {
            try {
                double gasVol      = Double.parseDouble(gasVolumeStr);
                double totalPrice  = Double.parseDouble(totalPriceStr);
                double pricePerGal = totalPrice / Math.max(gasVol, 0.000001d);
                binding.pricePerGallonInputEditText.setText(NumberFormatter.upTo2(pricePerGal));
            } catch (NumberFormatException e) {
                binding.pricePerGallonInputEditText.setText("");
            }
        } else {
            binding.pricePerGallonInputEditText.setText("");
        }
    }

    /**
     * Derive Total from Gallons and Price/Gal.
     * <p>If either input is empty or unparsable, clears the Total field.</p>
     */
    private void updateTotalPrice() {
        String gasVolumeStr   = text(binding.gasVolumeInputEditText);
        String pricePerGalStr = text(binding.pricePerGallonInputEditText);

        if (!gasVolumeStr.isEmpty() && !pricePerGalStr.isEmpty()) {
            try {
                double gasVol      = Double.parseDouble(gasVolumeStr);
                double pricePerGal = Double.parseDouble(pricePerGalStr);
                double total       = gasVol * pricePerGal;
                binding.totalPriceInputEditText.setText(NumberFormatter.upTo2(total));
            } catch (NumberFormatException e) {
                binding.totalPriceInputEditText.setText("");
            }
        } else {
            binding.totalPriceInputEditText.setText("");
        }
    }

    // --------------------------------------------------------------------------------------------
    // Save flow
    // --------------------------------------------------------------------------------------------

    /**
     * Gather user inputs, perform basic validation, then run an asynchronous odometer sanity check
     * before committing insert/update via the {@link FuelEntryViewModel}.
     */
    private void onSave() {
        // Read dropdown selection
        int _carId = CarSelectorHelper.getSelectedOptionKey();

        // Read numeric fields (fallback to 0 on parse issues)
        int odo        = safeInt(text(binding.odometerInputEditText));
        double gallons = safeDouble(text(binding.gasVolumeInputEditText));
        double price   = safeDouble(text(binding.pricePerGallonInputEditText));
        double total   = gallons * price; // single source of truth; UI Total mirrors this

        // Merge date + time from UI into recordTimeStamp
        String dateStr = text(binding.editTextDateFuelEntry);
        String timeStr = text(binding.editTextTimeFuelEntry);
        try {
            recordTimeStamp = LocalDateTime.parse(dateStr + " " + timeStr, UI_DATE_TIME_PARSE);
        } catch (DateTimeParseException ex) {
            Toast.makeText(this, "Invalid date/time.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Build entity
        FuelEntry e = new FuelEntry();
        if (isEdit) e.setLogID(editLogId);
        e.setCarID(_carId);
        e.setOdometer(odo);
        e.setGallons(gallons);
        e.setPricePerGallon(price);
        e.setTotalCost(total);
        e.setLogDate(recordTimeStamp);

        // Synchronous field validation (basic required checks)
        if (!validateInputsBasic(e)) return;

        // Async odometer validation. Callback provides bounds of neighboring entries (if any).
        repository.checkOdometerAsync(
                e.getLogID(),
                _carId,
                e.getLogDate(),
                e.getOdometer(),
                (ok, prev, next) -> runOnUiThread(() -> {
                    if (ok) {
                        if (isEdit) viewModel.update(e); else viewModel.insert(e);
                        finish();
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
                })
        );
    }

    /**
     * Perform quick client-side validation for required fields and non-negative values.
     * <p>
     * Note: The current implementation allows 0 for <i>gallons</i> and <i>price per gallon</i>
     * (it checks <code>&lt; 0</code>, not <code>&lt;= 0</code>) while the messages say
     * "must be &gt; 0". Tighten the comparisons if you want to forbid zeros.
     * </p>
     *
     * @param entry The entity being validated
     * @return true if basic checks pass; false otherwise (with a Toast shown)
     */
    private boolean validateInputsBasic(FuelEntry entry) {
        if (entry.getCarID() <= 0) {
            Toast.makeText(this, "Please select a car.", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (entry.getOdometer() < 0) {
            Toast.makeText(this, "Odometer must be ≥ 0.", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (entry.getPricePerGallon() <= 0) {
            // Message says > 0, but code allows 0. Change to <= 0 if you want to enforce strictly > 0.
            Toast.makeText(this, "Price per gallon must be > 0.", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (entry.getGallons() <= 0) {
            // Message says > 0, but code allows 0. Change to <= 0 if you want to enforce strictly > 0.
            Toast.makeText(this, "Gallons must be > 0.", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (entry.getLogDate() == null) {
            Toast.makeText(this, "Date/time required.", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    // --------------------------------------------------------------------------------------------
    // Small helpers
    // --------------------------------------------------------------------------------------------

    /** Return trimmed text from any TextView (empty string if null). */
    private static String text(android.widget.TextView tv) {
        return tv.getText() == null ? "" : tv.getText().toString().trim();
    }

    /** Parse int or return 0 on failure. */
    private int safeInt(String s) {
        try { return Integer.parseInt(s); } catch (Exception e) { return 0; }
    }

    /** Parse double or return 0.0 on failure. */
    private double safeDouble(String s) {
        try { return Double.parseDouble(s); } catch (Exception e) { return 0.0; }
    }
}
