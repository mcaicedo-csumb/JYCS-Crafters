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

//public class FuelLogViewModel extends AndroidViewModel {
//    private final FuelTrackAppRepository repo;
//    private LiveData<List<FuelEntry>> entries;
//
//    public FuelLogViewModel(@NonNull Application app) {
//        super(app);
//        repo = FuelTrackAppRepository.getRepository(app);
//    }
//
//    public LiveData<List<FuelEntry>> getEntries(int carId) {
//        if (entries == null) {
//            entries = repo.getEntriesForCar(carId);
//        }
//        return entries;
//    }
//}
public class FuelLogViewModel extends AndroidViewModel {
    private final FuelTrackAppRepository repo;
    private final MutableLiveData<Integer> selectedCarId = new MutableLiveData<>();
    public final LiveData<List<FuelEntry>> entries;

    public FuelLogViewModel(@NonNull Application app) {
        super(app);
        repo = FuelTrackAppRepository.getRepository(app);

        entries = Transformations.switchMap(
                selectedCarId,
                id -> (id == null || id <= 0)
                        ? new MutableLiveData<>(Collections.emptyList())
                        : repo.getEntriesForCar(id)
        );
    }

    public void setSelectedCarId(int carId) {
        Integer cur = selectedCarId.getValue();
        if (cur == null || cur != carId) selectedCarId.setValue(carId);
    }
}
