package com.stanissudo.jycs_crafters.viewHolders;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SharedViewModel extends ViewModel {
    private final MutableLiveData<Integer> selectedCarId = new MutableLiveData<>();

    public void selectCar(int carId) {
        selectedCarId.setValue(carId);
    }

    public LiveData<Integer> getSelectedCarId() {
        return selectedCarId;
    }
}