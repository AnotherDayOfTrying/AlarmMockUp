package com.ijfh.alarmmockup;

import android.app.Application;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import java.util.List;


public class AlarmRepository {
    private Alarm.AlarmDao mAlarmDao;
    private LiveData<List<Alarm>> mAllAlarms;

    AlarmRepository(Application application) {
        AlarmRoomDatabase db = AlarmRoomDatabase.getDatabase(application);
        mAlarmDao = db.alarmDao();
        mAllAlarms = mAlarmDao.getAllAlarms();
    }

    private static class insertAsyncTask extends AsyncTask<Alarm, Void, Void> {

        private Alarm.AlarmDao mAsyncTaskDao;

        insertAsyncTask(Alarm.AlarmDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Alarm... alarms) {
            final Alarm a = alarms[0];
            mAsyncTaskDao.insert(a);
            return null;
        }
    }

    private static class deleteAllAsyncTask extends AsyncTask<Void, Void, Void> {
        private Alarm.AlarmDao mAsyncTaskDao;

        deleteAllAsyncTask(Alarm.AlarmDao alarm) {
            mAsyncTaskDao = alarm;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            mAsyncTaskDao.deleteAll();
            return null;
        }
    }

    private static class deleteAlarmAsyncTask extends AsyncTask<Alarm, Void, Void> {
        private Alarm.AlarmDao mAsyncTaskDao;

        deleteAlarmAsyncTask(Alarm.AlarmDao alarm) {
            mAsyncTaskDao = alarm;
        }

        @Override
        protected Void doInBackground(Alarm... alarm) {
            mAsyncTaskDao.delete(alarm[0]);
            return null;
        }
    }

    private static class updateAlarmAsyncTask extends AsyncTask<Alarm, Void, Void> {
        private Alarm.AlarmDao mAsyncAlarmDao;

        updateAlarmAsyncTask(Alarm.AlarmDao alarm) {
            mAsyncAlarmDao = alarm;
        }

        @Override
        protected Void doInBackground(Alarm... alarms) {
            mAsyncAlarmDao.update(alarms);
            return null;
        }
    }

    public LiveData<List<Alarm>> getAllAlarms() {
        return mAllAlarms;
    }

    public void insert(Alarm alarm) {
        new insertAsyncTask(mAlarmDao).execute(alarm);
    }

    public void deleteAll() { new deleteAllAsyncTask(mAlarmDao).execute(); }

    public void delete(Alarm alarm) {new deleteAlarmAsyncTask(mAlarmDao).execute(alarm); }

    public void update(Alarm... alarm) {new updateAlarmAsyncTask(mAlarmDao).execute(alarm);}
}
