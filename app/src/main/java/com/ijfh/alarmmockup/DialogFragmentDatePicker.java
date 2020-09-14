package com.ijfh.alarmmockup;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DialogFragmentDatePicker extends DialogFragment{
    private long mTime;
    private String mTitle;
    private Calendar c;
    private DialogFragmentDatePicker.OnTimeChangedListener mListener;
    private SimpleDateFormat dateFormatter = new SimpleDateFormat("E, dd MMMM yyyy", Locale.CANADA);

    interface OnTimeChangedListener {
        void onTimeChanged(long time, String title);
    }

    DialogFragmentDatePicker() {
    }

    DialogFragmentDatePicker(Alarm alarm) {
        mTime = alarm.getTime();
        mTitle = alarm.getTitle();
    }

    public void setOnTimeChanged(DialogFragmentDatePicker.OnTimeChangedListener listener) {
        mListener = listener;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        c = Calendar.getInstance();
        //Initialize Time to Current Alarm Time
        c.setTimeInMillis(mTime);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        View v = inflater.inflate(R.layout.dialog_fragment_alarm_picker, container, false);

        TimePicker timePicker = v.findViewById(R.id.timePicker);
        Button ok = v.findViewById(R.id.positive);
        Button cancel = v.findViewById(R.id.negative);
        final TextView date = v.findViewById(R.id.date_text);
        final ImageButton calendar = v.findViewById(R.id.calendarButton);
        final EditText editText = v.findViewById(R.id.edit_alarm_title);

        editText.setText(mTitle);
        date.setText(dateFormatter.format(c.getTime()));

        timePicker.setMinute(c.get(Calendar.MINUTE));
        timePicker.setHour(c.get(Calendar.HOUR_OF_DAY));

        timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                c.set(Calendar.HOUR_OF_DAY, hourOfDay);
                c.set(Calendar.MINUTE, minute);
            }
        });

        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onTimeChanged(c.getTimeInMillis(), editText.getText().toString());
                }
                dismiss();
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        final DatePickerDialog.OnDateSetListener listener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                c.set(Calendar.YEAR, year);
                c.set(Calendar.MONTH, month);
                c.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                date.setText(dateFormatter.format(c.getTime()));
            }
        };

        calendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(getContext(), listener, c.get(Calendar.YEAR),
                        c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();

            }
        });
        return v;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().getAttributes().windowAnimations = R.style.fragment_anims;
        return dialog;
    }



    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
    }
}
