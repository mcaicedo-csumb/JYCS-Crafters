package com.stanissudo.jycs_crafters.viewHolders;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.stanissudo.jycs_crafters.database.FuelTrackAppRepository;
import com.stanissudo.jycs_crafters.database.entities.FuelEntry;

/**
 * @author Ysabelle Kim
 * created: 8/4/2025
 * Explanation: FuelEntryViewModel stores the state of the UI between changes.
 * @project JYCS-Crafters
 * @name FuelEntryViewModel.java
 */
public class FuelEntryViewModel extends AndroidViewModel {
    private final FuelTrackAppRepository repository;
    public FuelEntryViewModel(Application application) {
        super(application);
        repository = FuelTrackAppRepository.getRepository(application);
    }
    public LiveData<FuelEntry> getById(int id) { return repository.getRecordById(id); }
    public void update(FuelEntry e) { repository.updateFuelEntry(e); }
    public void insert(FuelEntry e) { repository.insertFuelEntry(e); }

}
