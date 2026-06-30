package com.example.myschedule;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myschedule.database.AlarmEntity;

import java.util.List;

public class AlarmAdapter extends RecyclerView.Adapter<AlarmAdapter.AlarmViewHolder> {

    private List<AlarmEntity> alarms;
    private OnAlarmDeleteListener deleteListener;

    public interface OnAlarmDeleteListener {
        void onAlarmDelete(AlarmEntity alarm);
    }

    public AlarmAdapter(List<AlarmEntity> alarms, OnAlarmDeleteListener deleteListener) {
        this.alarms = alarms;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public AlarmViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_alarm_offset, parent, false);
        return new AlarmViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlarmViewHolder holder, int position) {
        AlarmEntity alarm = alarms.get(position);
        String text;
        if (alarm.getTriggerOffsetMinutes() == 0) {
            text = "At lecture start time";
        } else if (alarm.getTriggerOffsetMinutes() == 60) {
            text = "1 hour before";
        } else if (alarm.getTriggerOffsetMinutes() > 60) {
            text = (alarm.getTriggerOffsetMinutes() / 60) + " hours before";
        } else {
            text = alarm.getTriggerOffsetMinutes() + " minutes before";
        }
        holder.offsetText.setText(text);
        holder.deleteIcon.setOnClickListener(v -> deleteListener.onAlarmDelete(alarm));
    }

    @Override
    public int getItemCount() {
        return alarms.size();
    }

    public void setAlarms(List<AlarmEntity> alarms) {
        this.alarms = alarms;
        notifyDataSetChanged();
    }

    static class AlarmViewHolder extends RecyclerView.ViewHolder {
        TextView offsetText;
        ImageView deleteIcon;

        public AlarmViewHolder(@NonNull View itemView) {
            super(itemView);
            offsetText = itemView.findViewById(R.id.tv_alarm_offset_text);
            deleteIcon = itemView.findViewById(R.id.iv_delete_alarm);
        }
    }
}