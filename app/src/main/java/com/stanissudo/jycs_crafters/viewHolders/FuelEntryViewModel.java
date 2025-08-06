package com.stanissudo.jycs_crafters.viewHolders;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;

import com.stanissudo.jycs_crafters.database.FuelTrackAppRepository;

/**
 * @author Ysabelle Kim
 * created: 8/4/2025 - 10:35 PM
 * @project JYCS-Crafters
 * file: FuelEntryViewModel.java
 * @since 1.0.0
 * Explanation: FuelEntryViewModel allows the UI state to persist
 */
public class FuelEntryViewModel extends AndroidViewModel {
    private final FuelTrackAppRepository repository;

    public FuelEntryViewModel(Application application) {
        super(application);
        repository = FuelTrackAppRepository.getRepository(application);
    }

    // TODO: create getAllLogsByUserID(int) in FuelTrackAppRepository
    /*public LiveData<List<FuelEntry>> getAllLogsByID(int userID) {
        return repository.getAllLogsByUserID(userID);
    }*/

    // TODO: create insert(FuelEntry) in FuelTrackAppRepository
    /*public void insert(FuelEntry entry) {
        repository.insertGymLog(entry);
    }*/
}
