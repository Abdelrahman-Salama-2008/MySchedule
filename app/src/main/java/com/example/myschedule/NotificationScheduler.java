package com.example.myschedule;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;
import java.util.ArrayList;

public class NotificationScheduler {
    private Context context;
    AlarmManager alarmManager;
    public NotificationScheduler(Context context) {
        this.context = context;
        alarmManager = context.getSystemService(AlarmManager.class);
    }

    ArrayList<Lecture> lectures = new ArrayList<>();
    String days[] = new String[]{"SUNDAY", "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY"};


    public void scheduleNotifications()
    {
        lectures = ScheduleData.getLectures();
        for (Lecture lecture : lectures)
        {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, lecture.getStarttime().getHour());
            calendar.set(Calendar.MINUTE, lecture.getStarttime().getMinute());
            for(int i = 0; i < 7; i++){
                if(lecture.getDay().equalsIgnoreCase(days[i]))
                {
                    calendar.set(Calendar.DAY_OF_WEEK, i + 1);
                }
            }

            calendar.add(Calendar.MINUTE, -45);

            if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
                calendar.add(Calendar.DAY_OF_YEAR, 7);
            }


            int ID = (lecture.getName() + lecture.getDay()).hashCode();

            Intent intent = new Intent(context, NotificationReceiver.class);
            intent.putExtra("lecture_name", lecture.getName());



            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, ID, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                }
            } else {
                // For older Android versions, just set it
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            }
        }
    }

}
