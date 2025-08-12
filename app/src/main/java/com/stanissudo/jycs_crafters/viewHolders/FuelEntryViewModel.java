//package com.stanissudo.jycs_crafters.viewHolders;
//
//import android.app.Application;
//
//import androidx.lifecycle.AndroidViewModel;
//import androidx.lifecycle.LiveData;
//
//import com.stanissudo.jycs_crafters.database.FuelTrackAppRepository;
//import com.stanissudo.jycs_crafters.database.entities.FuelEntry;
//
//import java.util.List;
//
///**
// * @author Ysabelle Kim
// * created: 8/4/2025 - 10:35 PM
// * @project JYCS-Crafters
// * file: FuelEntryViewModel.java
// * @since 1.0.0
// * Explanation: FuelEntryViewModel allows the UI state to persist
// */
//public class FuelEntryViewModel extends AndroidViewModel {
//    private final FuelTrackAppRepository repository;
//
//    public FuelEntryViewModel(Application application) {
//        super(application);
//        repository = FuelTrackAppRepository.getRepository(application);
//    }
//
//    public LiveData<List<FuelEntry>> getAllLogsByUserId(int userId) {
//        return repository.getAllLogsByUserId(userId);
//    }
//
//    public void insert(FuelEntry entry) { repository.insertFuelEntry(entry); }
//}
