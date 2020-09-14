package com.ijfh.alarmmockup;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.util.LinkedList;
import java.util.Queue;

public class AlarmService extends Service {


    private static final String CHANNEL_ID = "primary_notification_channel";

    public static final String START_ALARM = "alarm.service.start.alarm";
    public static final String END_ALARM = "alarm.service.end.alarm";

    private NotificationManager mNotificationManager;

    AlarmRepository mAlarmRepo;
    Uri alarmURI = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);

    private Queue<Ringtone> ringtoneQueue = new LinkedList<>();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        createNotificationChannel();
        Log.i("TAG", "ON START COMMAND");
        mAlarmRepo = new AlarmRepository(getApplication());
        Ringtone alarmSound = RingtoneManager.getRingtone(this, alarmURI);
        if (intent.getAction() != null) {
            Bundle b = intent.getBundleExtra(MainActivity.ALARM_BUNDLE_KEY);
            Alarm alarm = b.getParcelable(MainActivity.ALARM_KEY);
            switch(intent.getAction()) {
                case START_ALARM:
                    mNotificationManager.notify(alarm.getAlarmId(), createNotification(alarm));
                    alarmSound.play();
                    ringtoneQueue.add(alarmSound);
                    alarm.setState(true);
                    mAlarmRepo.update(alarm);
                    break;
                case END_ALARM:
                    mNotificationManager.cancel(alarm.getAlarmId());
                    try{
                        alarmSound = ringtoneQueue.remove();
                        alarmSound.stop();
                    } catch (Exception e) {
                        //Alarm wasn't set in service
                    }
                    alarm.setState(false);
                    mAlarmRepo.update(alarm);
                    break;
                default:
                    //Nothing Happens
            }
        }
        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public Notification createNotification(Alarm alarm) {
        Intent alarmIntent = new Intent(getBaseContext(), MainActivity.class);
        PendingIntent alarmPendingIntent = PendingIntent.getActivity
                (getBaseContext(), alarm.getAlarmId(), alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        Intent turnOffIntent = new Intent(getBaseContext(), AlarmService.class);
        Bundle b = new Bundle();
        b.putParcelable(MainActivity.ALARM_KEY, alarm);
        turnOffIntent.putExtra(MainActivity.ALARM_BUNDLE_KEY, b);
        turnOffIntent.setAction(AlarmService.END_ALARM);
        PendingIntent turnOffPendingIntent = PendingIntent.getService
                (getBaseContext(), alarm.getAlarmId(), turnOffIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        return new NotificationCompat.Builder(getBaseContext(), CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_alarm_clock)
                .setContentTitle(alarm.getTitle())
                .setContentText(alarm.getCreator())
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(alarmPendingIntent)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .addAction(R.drawable.ic_alarm_clock,"TURN OFF ALARM", turnOffPendingIntent)
                .setAutoCancel(true)
                .build();
    }
    public void createNotificationChannel() {

        // Create a notification manager object.
        mNotificationManager =
                (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

        // Notification channels are only available in OREO and higher.
        // So, add a check on SDK version.
        if (android.os.Build.VERSION.SDK_INT >=
                android.os.Build.VERSION_CODES.O) {

            // Create the NotificationChannel with all the parameters.
            NotificationChannel notificationChannel = new NotificationChannel
                    (CHANNEL_ID,
                            "Universal Alarm Notification",
                            NotificationManager.IMPORTANCE_HIGH);

            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.enableVibration(true);
            notificationChannel.setDescription
                    ("Start When an Alarm from Universal Alarm is Triggered");
            mNotificationManager.createNotificationChannel(notificationChannel);
        }
    }
}
