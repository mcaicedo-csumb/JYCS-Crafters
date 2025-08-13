package com.stanissudo.jycs_crafters.viewHolders;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.stanissudo.jycs_crafters.database.FuelTrackAppRepository;
import com.stanissudo.jycs_crafters.database.entities.FuelEntry;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * *  @author Stan Permiakov
 * *  created: 8/12/2025
 * *  @project JYCS-Crafters
 * *
 * ViewModel that exposes a list of {@link FuelEntry} items for the currently selected vehicle.
 * <p>
 * <b>Responsibilities</b>
 * <ul>
 *   <li>Holds the selected car id as UI state.</li>
 *   <li>Maps the selected car id to the corresponding list of entries</li>
 *   <li>Runs destructive operations (e.g., delete) on a background thread.</li>
 * </ul>
 *
 * <b>Usage</b>
 * <pre>{@code
 * viewModel.entries.observe(this, adapter::submitList);
 * viewModel.setSelectedCarId(carId);
 * }</pre>
 */
public class FuelLogViewModel extends AndroidViewModel {
    /** Data repository used to load and modify fuel log entries. */
    private final FuelTrackAppRepository repository;

    /** Single-thread executor for IO-bound work (e.g., deletions). */
    private final ExecutorService io = Executors.newSingleThreadExecutor();

    /** Currently selected car id; drives which entries are exposed. */
    private final MutableLiveData<Integer> selectedCarId = new MutableLiveData<>();

    /** LiveData stream of entries for the selected car (empty when no car is selected). */
    public final LiveData<List<FuelEntry>> entries;

    /**
     * Constructs the ViewModel and wires the {@link #entries} stream to the selected car id.
     *
     * @param app Application context provided by the framework
     */
    public FuelLogViewModel(@NonNull Application app) {
        super(app);
        repository = FuelTrackAppRepository.getRepository(app);

        entries = Transformations.switchMap(
                selectedCarId,
                id -> (id == null || id <= 0)
                        ? new MutableLiveData<>(Collections.emptyList())
                        : repository.getEntriesForCar(id)
        );
    }

    /**
     * Sets the id of the car whose entries should be observed and displayed.
     * No update occurs if the id is unchanged.
     *
     * @param carId The selected vehicle's id
     */
    public void setSelectedCarId(int carId) {
        Integer cur = selectedCarId.getValue();
        if (cur == null || cur != carId) selectedCarId.setValue(carId);
    }

    /**
     * Deletes a single entry by its id on a background thread.
     *
     * @param id Primary key of the {@link FuelEntry} to remove
     */
    public void deleteById(long id) {
        io.execute(() -> repository.deleteRecordByID(id));
    }

    /**
     * Called when the ViewModel is about to be destroyed. Shuts down the executor.
     */
    @Override protected void onCleared() {
        io.shutdown(); // optional
    }
}
