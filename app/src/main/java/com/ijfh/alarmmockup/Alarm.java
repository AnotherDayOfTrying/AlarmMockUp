package com.ijfh.alarmmockup;

import android.content.Context;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.room.ColumnInfo;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Entity;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.PrimaryKey;
import androidx.room.Query;
import androidx.room.SkipQueryVerification;
import androidx.room.Update;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

@Entity(tableName = "alarm_table")
public class Alarm implements Parcelable {

    private static int count = 0;

    @ColumnInfo(name = "alarm_id")
    private int mAlarmId;

    @ColumnInfo(name = "title")
    private String mTitle;
    @PrimaryKey
    @ColumnInfo(name = "time")
    private long mTime;

    @ColumnInfo(name = "creator")
    private String mCreator;

    @ColumnInfo(name = "state")
    private boolean mState;

    @Dao
    public interface AlarmDao {

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        void insert(Alarm alarm);

        @Update
        void update(Alarm... alarm);

        @Query("DELETE FROM alarm_table")
        void deleteAll();

        @Query("SELECT * from alarm_table ORDER BY time ASC")
        LiveData<List<Alarm>> getAllAlarms();

        @Delete
        void delete(Alarm alarm);

    }

    Alarm(String title, long time, String creator) {
        mTitle = title;
        mTime = time;
        mCreator = creator;
        mState = false;
        mAlarmId = ++count;
    }

    Alarm(Parcel source) {
        mTitle = source.readString();
        mTime = source.readLong();
        mCreator = Objects.requireNonNull(source.readString());
        mState = source.readInt() == 1; // this is boolean
        mAlarmId = source.readInt();
    }

    public static ArrayList<Alarm> createAlarmList(int numAlarms) {
        ArrayList<Alarm> alarmList = new ArrayList<>();

        for (int i = 0; i < numAlarms; i++) {
            Random rand = new Random();
            alarmList.add(new Alarm("Test" + i, System.currentTimeMillis() + (rand.nextLong() % (1000*60*24*365)), "NAME"));
        }
        return alarmList;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mTitle);
        dest.writeLong(mTime);
        dest.writeString(mCreator);
        dest.writeInt(mState ? 1 : 0); //This is a boolean
        dest.writeInt(mAlarmId);
    }

    public static final Creator<Alarm> CREATOR = new Creator<Alarm>() {
        @Override
        public Alarm[] newArray(int size) {
            return new Alarm[size];
        }

        @Override
        public Alarm createFromParcel(Parcel source) {
            return new Alarm(source);
        }
    };
    //Setter Methods
    public void setTitle(String title) {mTitle = title;}

    public void setState(boolean b) {mState = b;}

    public void setTime(long l) {mTime = l;}

    public void setAlarmId(int id) {mAlarmId = id;}

    public void setCreator(String creator) {mCreator = creator;}

    //Getter Methods
    public boolean getState() {return mState;}

    public String getCreator() {
        return mCreator;
    }

    public long getTime() {
        return mTime;
    }

    public String getTitle() {
        return mTitle;
    }

    public int getAlarmId() {return mAlarmId;}
}
