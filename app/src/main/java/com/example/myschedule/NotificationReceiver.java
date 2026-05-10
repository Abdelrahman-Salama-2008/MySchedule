package com.example.myschedule;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;

public class NotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String lectureName = intent.getStringExtra("lecture_name");
        int minutes_before = intent.getIntExtra("minutes_before", 0);
        int notificationID = intent.getIntExtra("notification_id", (int) System.currentTimeMillis());

        NotificationManager manager = context.getSystemService(NotificationManager.class);

        String title = "Class starting soon!";
        String content = lectureName + " starts in " + minutes_before + " minutes.";

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "lecture_channel")
                .setSmallIcon(R.drawable.clock) // Make sure this icon exists
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setAutoCancel(true);

        if (manager != null) {
            manager.notify(notificationID, builder.build());
        }
    }
}