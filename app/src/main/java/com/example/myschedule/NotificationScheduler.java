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

    // Static helper to make it easy to call from AddLectureActivity
    public static void scheduleNotificationAndAlarm(Context context, Lecture lecture) {
        new NotificationScheduler(context).scheduleSingleLecture(lecture);
    }

    public NotificationScheduler(Context context) {
        this.context = context;
        this.alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    }

    public void scheduleSingleLecture(Lecture lecture) {
        // Cancel existing to avoid duplicates
        cancelSingleLecture(lecture);

        if (lecture.getWantsNotification()) {
            Calendar calendar = calculateAlarmTime(lecture, lecture.getReminderMinutes());
            PendingIntent pendingIntent = getPendingIntent(lecture, "NOTIFICATION", lecture.getReminderMinutes());
            schedule(calendar, pendingIntent, false);
        }

        if (lecture.isAlarmEnabled()) {
            Calendar calendar = calculateAlarmTime(lecture, lecture.getAlarmMinutes());
            PendingIntent pendingIntent = getPendingIntent(lecture, "ALARM", lecture.getAlarmMinutes());
            schedule(calendar, pendingIntent, true);
        }
    }

    private void schedule(Calendar calendar, PendingIntent pendingIntent, boolean isAlarm) {
        if (alarmManager != null) {
            if (isAlarm) {
                // setAlarmClock is the most reliable way to wake up the device and bypass Doze
                AlarmManager.AlarmClockInfo alarmClockInfo = new AlarmManager.AlarmClockInfo(calendar.getTimeInMillis(), pendingIntent);
                alarmManager.setAlarmClock(alarmClockInfo, pendingIntent);
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (alarmManager.canScheduleExactAlarms()) {
                        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                    } else {
                        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                    }
                } else {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                }
            }
        }
    }

    public void cancelSingleLecture(Lecture lecture) {
        if (alarmManager != null) {
            alarmManager.cancel(getPendingIntent(lecture, "NOTIFICATION", 0));
            alarmManager.cancel(getPendingIntent(lecture, "ALARM", 0));
        }
    }

    public void scheduleAllLectures() {
        ArrayList<Lecture> lectures = ScheduleData.getLectures(context);
        for (Lecture lecture : lectures) {
            scheduleSingleLecture(lecture);
        }
    }

    private Calendar calculateAlarmTime(Lecture lecture, int minutesBefore) {
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

        calendar.add(Calendar.MINUTE, -minutesBefore);

        if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 7);
        }
        return calendar;
    }

    private PendingIntent getPendingIntent(Lecture lecture, String type, int minutesBefore) {
        int id = lecture.getId();
        Intent intent;

        if ("ALARM".equals(type)) {
            id += 100000; // Large offset to avoid collision
            intent = new Intent(context, AlarmReceiver.class); // Routes to ALARM logic
        } else {
            intent = new Intent(context, NotificationReceiver.class); // Routes to NOTIFICATION logic
        }

        intent.putExtra("lecture_name", lecture.getName());
        intent.putExtra("minutes_before", minutesBefore);
        intent.putExtra("room", lecture.getRoom());
        intent.putExtra("notification_id", id);

        return PendingIntent.getBroadcast(
                context,
                id,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
    }
}