package com.ijfh.alarmmockup;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModel;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AlarmAdapter extends RecyclerView.Adapter<AlarmAdapter.ViewHolder> {
    private Context mContext;
    private List<Alarm> mAlarmList;
    private static OnItemClickListener listener;
    private static OnCheckedChangeListener alarmListener;

    public interface OnItemClickListener {
        void onItemClick(View itemView, int position);
    }

    public interface OnCheckedChangeListener {
        void onClick(boolean isChecked, int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        AlarmAdapter.listener = listener;
    }

    public void setOnSwitchClickListener(OnCheckedChangeListener listener) {
        AlarmAdapter.alarmListener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView mTitle;
        public TextView mAlarmTime;
        public TextView mCreator;
        public TextView mDate;
        public TextView mAmPm;
        public Switch mSwitch;

        public ViewHolder(final View itemView) {
            super(itemView);

            mTitle = itemView.findViewById(R.id.alarm_title);
            mAlarmTime = itemView.findViewById(R.id.alarm_time);
            mCreator = itemView.findViewById(R.id.alarm_creator);
            mDate = itemView.findViewById(R.id.alarm_date);
            mAmPm = itemView.findViewById(R.id.alarm_ampm);
            mSwitch = itemView.findViewById(R.id.alarm_switch);


            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onItemClick(itemView, position);
                        }
                    }
                }
            });
            mSwitch.setOnClickListener(new Switch.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (alarmListener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            alarmListener.onClick(mSwitch.isChecked(), position);
                        }
                    }
                }
            });
        }

    }

    AlarmAdapter(Context context, List<Alarm> alarmList) {
        mContext = context;
        mAlarmList = alarmList;
    }

    @NonNull
    @Override
    public AlarmAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.item_alarm_view, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlarmAdapter.ViewHolder holder, int position) {
        Alarm alarm = mAlarmList.get(position);
        Date d = new Date(alarm.getTime());
        //simplify this
        SimpleDateFormat formatterDate = new SimpleDateFormat("EEE, MMM d");
        SimpleDateFormat formatterAmPm = new SimpleDateFormat("a");
        SimpleDateFormat formatter = new SimpleDateFormat("hh:mm");
        String date = formatter.format(d);
        String AmPm = formatterAmPm.format(d);
        String day = formatterDate.format(d);
        //

        holder.itemView.setBackground(getBGColor(d));

        holder.mTitle.setText(alarm.getTitle());
        holder.mCreator.setText(alarm.getCreator());
        holder.mAlarmTime.setText(date);
        holder.mAmPm.setText(AmPm);
        holder.mDate.setText(day);
        holder.mSwitch.setChecked(alarm.getState());
    }

    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        super.onViewRecycled(holder);
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public void onViewAttachedToWindow(@NonNull ViewHolder holder) {
        super.onViewAttachedToWindow(holder);
    }

    @Override
    public int getItemCount() {
        return mAlarmList.size();
    }

    public void setAlarmList(ArrayList<Alarm> alarmList) {
        mAlarmList = alarmList;
        notifyDataSetChanged();
    }

    public void onItemRemove(final RecyclerView.ViewHolder viewHolder, final RecyclerView recyclerView
            , final int position, final AlarmViewModel mAlarmViewModel) {
        final int adapterPosition = viewHolder.getAdapterPosition();
        final Alarm mAlarm = mAlarmList.get(adapterPosition);
        Snackbar snackbar = Snackbar
                .make(recyclerView, "ALARM REMOVED", Snackbar.LENGTH_LONG)
                .setAction("UNDO", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int mAdapterPosition = viewHolder.getAdapterPosition();
                        mAlarmList.add(position, mAlarm);
                        notifyItemInserted(position);
                        recyclerView.scrollToPosition(position);
                        mAlarmViewModel.insert(mAlarm);
                    }
                });
        snackbar.show();
        mAlarmList.remove(adapterPosition);
        notifyItemRemoved(adapterPosition);
        mAlarmViewModel.delete(mAlarm);
    }

    private Drawable getBGColor(Date date) {
        //change background color based on time
        PorterDuff.Mode mMode = PorterDuff.Mode.SRC_OVER;
        Drawable bgDrawable = mContext.getDrawable(R.drawable.bg_item_alarm);
        SimpleDateFormat formatter = new SimpleDateFormat("HH mm");
        String time = formatter.format(date);

        try {
            double minuteInDay = Integer.parseInt(time.substring(0, 2)) * 60;
            minuteInDay += Integer.parseInt(time.substring(3, 5));
            minuteInDay -= 12*60;
            minuteInDay /= (60 * 12);
            double s = Math.abs(minuteInDay) * 255;
            String alpha = stringLento(Integer.toHexString((int) s), 2);
            String nightColor = stringLento(
                    Integer.toHexString(ContextCompat.getColor(mContext, R.color.night) & 0x00ffffff),
                    6);
            int color = Color.parseColor("#"+ alpha + nightColor);
            bgDrawable.setColorFilter(color, mMode);
            return bgDrawable;
        } catch (Exception e) {
            Log.e("TAG", "Color Went Wrong");
            e.printStackTrace();
            return mContext.getDrawable(R.drawable.bg_item_alarm);
        }
    }

    private String stringLento(String s, int len) {
        for (int i = s.length(); i < len; i++) {
            s = "0" + s;
        }
        return s;
    }
}
