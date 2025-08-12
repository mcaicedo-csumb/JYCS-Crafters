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

public class FuelLogViewModel extends AndroidViewModel {
    private final FuelTrackAppRepository repository;
    private final ExecutorService io = Executors.newSingleThreadExecutor();
    private final MutableLiveData<Integer> selectedCarId = new MutableLiveData<>();
    public final LiveData<List<FuelEntry>> entries;

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

    public void setSelectedCarId(int carId) {
        Integer cur = selectedCarId.getValue();
        if (cur == null || cur != carId) selectedCarId.setValue(carId);
    }

    public void deleteById(long id) {
        io.execute(() -> repository.deleteRecordByID(id));
    }

    @Override protected void onCleared() {
        io.shutdown(); // optional
    }
}
