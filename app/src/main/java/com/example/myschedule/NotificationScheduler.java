package com.example.myschedule;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.time.LocalTime;
import java.util.Calendar;
import java.util.ArrayList;

public class NotificationScheduler {
    private Context context;
    private AlarmManager alarmManager;

    public NotificationScheduler(Context context) {
        this.context = context;
        this.alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    }


    public void scheduleSingleLecture(Lecture lecture) {
        if (!lecture.getWantsNotification()) return;

        Calendar calendar = calculateAlarmTime(lecture);
        PendingIntent pendingIntent = getPendingIntent(lecture);

        if (alarmManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            }
        }
    }

    public void cancelSingleLecture(Lecture lecture) {
        PendingIntent pendingIntent = getPendingIntent(lecture);
        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }
    }


    public void scheduleAllLectures() {
        ArrayList<Lecture> lectures = ScheduleData.getLectures(context);
        for (Lecture lecture : lectures) {
            scheduleSingleLecture(lecture);
        }
    }


    private Calendar calculateAlarmTime(Lecture lecture) {
        Calendar calendar = Calendar.getInstance();
        LocalTime startTime = TimeConverters.convertTime(lecture.getStarttime());

        calendar.set(Calendar.HOUR_OF_DAY, startTime.getHour());
        calendar.set(Calendar.MINUTE, startTime.getMinute());
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        String[] days = {"SUNDAY", "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY"};
        for (int i = 0; i < days.length; i++) {
            if (lecture.getDay().equalsIgnoreCase(days[i])) {
                calendar.set(Calendar.DAY_OF_WEEK, i + 1);
            }
        }

        calendar.add(Calendar.MINUTE, -lecture.getReminderMinutes());

        if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 7);
        }
        return calendar;
    }

    private PendingIntent getPendingIntent(Lecture lecture) {
        int id = lecture.getId();
        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.putExtra("lecture_name", lecture.getName());
        intent.putExtra("minutes_before", lecture.getReminderMinutes());

        return PendingIntent.getBroadcast(
                context,
                id,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
    }
}