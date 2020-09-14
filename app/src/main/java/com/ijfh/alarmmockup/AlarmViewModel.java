package com.ijfh.alarmmockup;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class AlarmViewModel extends AndroidViewModel {
    private AlarmRepository mAlarmRepository;
    private LiveData<List<Alarm>> mAllAlarms;

    public AlarmViewModel(@NonNull Application application) {
        super(application);
        mAlarmRepository = new AlarmRepository(application);
        mAllAlarms = mAlarmRepository.getAllAlarms();
    }

    public LiveData<List<Alarm>> getAllAlarms() {return mAllAlarms;}

    public void insert(Alarm alarm) {mAlarmRepository.insert(alarm);}

    public void deleteAll() {mAlarmRepository.deleteAll();}

    public void delete(Alarm alarm) {mAlarmRepository.delete(alarm);}

    public void update(Alarm... alarms) {mAlarmRepository.update(alarms);}
}
