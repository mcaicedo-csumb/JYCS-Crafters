package com.stanissudo.jycs_crafters.viewHolders;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.stanissudo.jycs_crafters.database.FuelTrackAppRepository;
import com.stanissudo.jycs_crafters.database.pojos.CarCostStats;

public class CostStatsViewModel extends AndroidViewModel {
    private final FuelTrackAppRepository repository;

    public CostStatsViewModel(@NonNull Application application) {
        super(application);
        repository = FuelTrackAppRepository.getRepository(application);
    }

    public LiveData<CarCostStats> getCostStats(int vehicleId) {
        return repository.getCostStatsForVehicle(vehicleId);
    }
}