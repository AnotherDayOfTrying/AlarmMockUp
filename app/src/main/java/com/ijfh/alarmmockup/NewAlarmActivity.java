package com.ijfh.alarmmockup;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class NewAlarmActivity extends AppCompatActivity {
    public static final String EXTRA_REPLY = "com.ijfh.universalalarm.REPLY";

    private EditText mTitleEdit;
    private Calendar c;

    private SimpleDateFormat dateFormatter = new SimpleDateFormat("E, dd MMMM yyyy", Locale.CANADA);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_alarm_activity);
        //Set All View Elements
        TimePicker timePicker = findViewById(R.id.timePicker);
        Button ok = findViewById(R.id.positive);
        Button cancel = findViewById(R.id.negative);
        ImageButton calendar = findViewById(R.id.calendarButton);
        final TextView date = findViewById(R.id.date_text);
        mTitleEdit = findViewById(R.id.edit_alarm_title);

        //Initialize Time
        c = Calendar.getInstance();
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        timePicker.setHour(c.get(Calendar.HOUR_OF_DAY));
        timePicker.setMinute(c.get(Calendar.MINUTE));
        date.setText(dateFormatter.format(c.getTime()));

        //Change Cancel Text
        ok.setText(getResources().getString(R.string.alarm_create));

        final DatePickerDialog.OnDateSetListener listener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                c.set(Calendar.YEAR, year);
                c.set(Calendar.MONTH, month);
                c.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                date.setText(dateFormatter.format(c.getTime()));
            }
        };

        timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                c.set(Calendar.HOUR_OF_DAY, hourOfDay);
                c.set(Calendar.MINUTE, minute);
            }
        });

        calendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(NewAlarmActivity.this, listener, c.get(Calendar.YEAR), c.get(Calendar.MONTH),
                        c.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent replyIntent = new Intent();
                replyIntent.putExtra(EXTRA_REPLY, new Alarm(mTitleEdit.getText().toString()
                        , c.getTimeInMillis(), MainActivity.mUserName));
                setResult(RESULT_OK, replyIntent);
                finish();
                overridePendingTransition(R.anim.none, R.anim.activity_exit);
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_CANCELED);
                finish();
                overridePendingTransition(R.anim.none, R.anim.activity_exit);
            }
        });
    }
}
