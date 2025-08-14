package com.stanissudo.jycs_crafters.viewHolders;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * @author Ysabelle Kim
 * created: 8/10/2025
 * Explanation: SharedViewModel stores the state of the UI between changes.
 * @project JYCS-Crafters
 * @name SharedViewModel.java
 */
public class SharedViewModel extends ViewModel {
    private final MutableLiveData<Integer> selectedCarId = new MutableLiveData<>();

    public void selectCar(int carId) {
        selectedCarId.setValue(carId);
    }

    public LiveData<Integer> getSelectedCarId() {
        return selectedCarId;
    }
}