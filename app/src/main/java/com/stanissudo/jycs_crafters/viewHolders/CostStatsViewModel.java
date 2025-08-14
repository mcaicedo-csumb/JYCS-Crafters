package com.stanissudo.jycs_crafters.viewHolders;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.stanissudo.jycs_crafters.database.FuelTrackAppRepository;
import com.stanissudo.jycs_crafters.database.pojos.CarCostStats;

public class CostStatsViewModel extends AndroidViewModel {
    private final FuelTrackAppRepository repository;
    private final MutableLiveData<Integer> vehicleId = new MutableLiveData<>();

    public final LiveData<CarCostStats> stats;

    public CostStatsViewModel(@NonNull Application application) {
        super(application);
        repository = FuelTrackAppRepository.getRepository(application);

        stats = Transformations.switchMap(vehicleId, id -> {
            if (id == null || id == -1) return emptyLiveData();
            return repository.getCostStatsForVehicle(id);
        });
    }

    public LiveData<CarCostStats> getCostStats(int vehicleId) {
        return repository.getCostStatsForVehicle(vehicleId);
    }

    public void setVehicleId(int id) { vehicleId.setValue(id); }

    // small helper to return a LiveData that emits null
    private static <T> LiveData<T> emptyLiveData() {
        MutableLiveData<T> m = new MutableLiveData<>();
        m.setValue(null);
        return m;
    }
}




