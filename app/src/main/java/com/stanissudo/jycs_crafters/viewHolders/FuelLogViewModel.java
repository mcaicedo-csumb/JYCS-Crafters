package com.stanissudo.jycs_crafters.viewHolders;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

/**
 * @author Ysabelle Kim
 * created: 8/4/2025 - 8:11 PM
 * @project JYCS-Crafters
 * file: FuelLogViewModel.java
 * @since 1.0.0
 * Explanation: allows UI state to persist for recycler
 */
public class FuelLogViewModel extends AndroidViewModel {
    // TODO: rename to repository name (FuelLogViewModel class)
    // private final GymLogRepository repository;

    public FuelLogViewModel (Application application) {
        super(application);
        // TODO: use repository name here (FuelLogViewModel constructor)
        // repository = GymLogRepository.getRepository(application);
//        allLogsByID = repository.getAllLogsByUserIDLiveData(userID);
    }

    /**
     * generated getters
     * @return
     */
    // TODO: repository name getAllLogsByID(int)
    /*public LiveData<List<GymLog>> getAllLogsByID(int userID) {
        return repository.getAllLogsByUserIDLiveData(userID);
    }*/

    // TODO: repository name insert(FuelLog)
    /*
    public void insert(GymLog log) {
        repository.insertGymLog(log);
    }*/
}
