package com.ijfh.alarmmockup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.appbar.AppBarLayout;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class MainActivity extends AppCompatActivity{

    //Keys
    public final static String RECYCLE_VIEW_KEY = "recycle.view.key";
    public final static String ALARM_LIST_KEY = "alarm.list.key";
    public final static String ALARM_KEY= "alarm.broadcast.key";
    public final static String ALARM_BUNDLE_KEY = "alarm.broadcast.bundle";
    public final static String STATE_KEY = "alarm.broadcast.state";
    public final static int NEW_ALARM_ACTIVITY_REQUEST_CODE = 1;
    public final static int NEW_USER_ACTIVITY_REQUEST_CODE = 2;

    public static String mUserName;

    //Restore Parcelable
    Parcelable recycleState;

    AlarmAdapter mAdapter;
    ArrayList<Alarm> mAlarmList;
    RecyclerView.LayoutManager mRecyclerViewLayoutManager;

    private AlarmViewModel mAlarmViewModel;
    private NotificationManager mNotificationManager;
    private RecyclerView mRecyclerView;
    private AlarmManager mAlarmManager;

    private static final String CHANNEL_ID = "primary_notification_channel";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_bar_main);
        //Initialize Variables
        final TextSwitcher nextAlarmText = findViewById(R.id.next_alarm);
        nextAlarmText.setInAnimation(getBaseContext(), android.R.anim.slide_in_left);
        nextAlarmText.setOutAnimation(getBaseContext(), android.R.anim.slide_out_right);

        //SetUp Alarm Model
        mAlarmViewModel = new ViewModelProvider(this).get(AlarmViewModel.class);
        mAlarmViewModel.getAllAlarms().observe(this, new Observer<List<Alarm>>() {
            @Override
            public void onChanged(List<Alarm> alarms) {
                mAlarmList = (ArrayList<Alarm>) alarms;
                mAdapter.setAlarmList(mAlarmList);
                String nextAlarmString = getString(R.string.no_set_alarms);
                for (int i = 0; i < mAlarmList.size(); i++) {
                    Alarm alarm = mAlarmList.get(i);
                    boolean b = alarm.getState();
                    if (b) {
                        long timeInMillis = alarm.getTime();
                        Date d = new Date(timeInMillis);
                        nextAlarmString = d.toString();
                        break;
                    }
                }
                nextAlarmText.setText(nextAlarmString);
            }
        });

        if (savedInstanceState != null) {
            mAlarmList = savedInstanceState.getParcelableArrayList(ALARM_LIST_KEY);
        }

        if (mAlarmList == null) {
            mAlarmList = new ArrayList<>();
        }


        //SetUp UI
        final Toolbar myToolbar = findViewById(R.id.app_toolbar);
        setSupportActionBar(myToolbar);
        AppBarLayout appbar = findViewById(R.id.app_appbar);

        final TextView appBarTitle = findViewById(R.id.toolbar_title);

        mRecyclerView = findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerViewLayoutManager = mRecyclerView.getLayoutManager();
        mAdapter = new AlarmAdapter(this, mAlarmList);

        //SetUp Notifications and Alarms
        mAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        createNotificationChannel();

        ItemTouchHelper touchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                mAdapter.onItemRemove(viewHolder, mRecyclerView, position, mAlarmViewModel);
                if (direction == ItemTouchHelper.LEFT) {
                    Toast.makeText(getBaseContext(), "LEFT", Toast.LENGTH_SHORT).show();
                } else if (direction == ItemTouchHelper.RIGHT) {
                    Toast.makeText(getBaseContext(), "RIGHT", Toast.LENGTH_SHORT).show();
                }
            }
        });

        touchHelper.attachToRecyclerView(mRecyclerView);

        //SetUp Listeners
        appbar.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            int scrollRange = -1;
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }
                if (scrollRange + verticalOffset == 0) {
                    appBarTitle.animate()
                            .alpha(1f)
                            .setDuration(1000);
                    appBarTitle.setVisibility(View.VISIBLE);
                } else {
                    appBarTitle.animate()
                            .alpha(0f)
                            .setDuration(1000);
                    appBarTitle.setVisibility(View.INVISIBLE);
                }
            }
        });

        mAdapter.setOnSwitchClickListener(new AlarmAdapter.OnCheckedChangeListener() {
            @Override
            public void onClick(boolean isChecked, int position) {
                Alarm a = mAlarmList.get(position);
                Intent intent = new Intent(getBaseContext(), AlarmService.class);
                //Bundle Will Survive, putExtra will not
                Bundle b = new Bundle();
                b.putParcelable(ALARM_KEY, a);
                intent.putExtra(ALARM_BUNDLE_KEY, b);
                intent.setAction(AlarmService.START_ALARM);
                Log.i("TAG INFO", Boolean.toString(isChecked));
                //Put Extra for Alarm Id, and Time
                PendingIntent notifyIntent = PendingIntent.getService(getBaseContext(),
                        a.getAlarmId(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
                if (isChecked) {
                    mAlarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, a.getTime(), notifyIntent);
                    a.setState(true);
                    mAlarmViewModel.update(a);
                    Toast.makeText(getBaseContext(), a.getTitle() + " ON", Toast.LENGTH_SHORT).show();
                } else {
                    mAlarmManager.cancel(notifyIntent);
                    if (System.currentTimeMillis() >= a.getTime()) { //Hasn't gone off
                        intent.setAction(AlarmService.END_ALARM);
                        startService(intent);
                    }
                    a.setState(false);
                    mAlarmViewModel.update(a);
                    Toast.makeText(getBaseContext(), a.getTitle() + " OFF", Toast.LENGTH_SHORT).show();
                }
            }
        });

        mAdapter.setOnItemClickListener(new AlarmAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View itemView, int position) {
                final int p = position;
                final Alarm a = mAlarmList.get(position);
//                Toast.makeText(getBaseContext(), Long.toString(a.getTime()), Toast.LENGTH_SHORT).show();
                DialogFragmentDatePicker dFDP = new DialogFragmentDatePicker(a);
                dFDP.setOnTimeChanged(new DialogFragmentDatePicker.OnTimeChangedListener() {
                    @Override
                    public void onTimeChanged(long time, String title) {
                        a.setTime(time);
                        a.setTitle(title);
                        a.setState(false);
                        mAlarmViewModel.update(a);
                    }
                });
                dFDP.show(getSupportFragmentManager(), "datePicker");
            }
        });

        mRecyclerView.setAdapter(mAdapter);

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mUserName == null) {
            startActivityForResult(new Intent(MainActivity.this,
                    NewUserActivity.class), NEW_USER_ACTIVITY_REQUEST_CODE);
            overridePendingTransition(R.anim.activity_enter, R.anim.none);
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putParcelable(RECYCLE_VIEW_KEY, mRecyclerViewLayoutManager.onSaveInstanceState());
        outState.putParcelableArrayList(ALARM_LIST_KEY, mAlarmList);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mAlarmList = savedInstanceState.getParcelableArrayList(ALARM_LIST_KEY);
        recycleState = savedInstanceState.getParcelable(RECYCLE_VIEW_KEY);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (recycleState != null) {
            mRecyclerViewLayoutManager.onRestoreInstanceState(recycleState);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_create:
                Intent startIntent = new Intent(this, NewAlarmActivity.class);
                startActivityForResult(startIntent, NEW_ALARM_ACTIVITY_REQUEST_CODE);
                overridePendingTransition(R.anim.activity_enter, R.anim.none);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == NEW_ALARM_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            assert data != null;
            Alarm alarm = data.getParcelableExtra(NewAlarmActivity.EXTRA_REPLY);
            mAlarmViewModel.insert(alarm);
        } else if (requestCode == NEW_USER_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            mUserName = data.getStringExtra(NewUserActivity.USER_NAME);
        }
    }

    public void createNotificationChannel() {

        // Create a notification manager object.
        mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

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