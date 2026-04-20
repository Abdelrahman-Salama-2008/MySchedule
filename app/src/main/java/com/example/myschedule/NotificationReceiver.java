package com.example.myschedule;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;


public class NotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // 1. "Open the Envelope" to get the lecture name
        String lectureName = intent.getStringExtra("lecture_name");

        // 2. Set up the NotificationManager
        NotificationManager manager = context.getSystemService(NotificationManager.class);

        // 3. Create the Notification Channel (the 'if' statement for modern Android)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "lecture_channel",
                    "Lecture Alerts",
                    NotificationManager.IMPORTANCE_HIGH
            );
            manager.createNotificationChannel(channel);
        }

        // 4. Build the actual Notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "lecture_channel")
                .setSmallIcon(R.drawable.ic_launcher_foreground) // Replace with your squircle icon!
                .setContentTitle("Class starting soon!")
                .setContentText(lectureName + " starts in 45 minutes.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        // 5. Show it to the user! (Use a unique ID so they don't overwrite each other)
        int notificationID = (int) System.currentTimeMillis();
        manager.notify(notificationID, builder.build());
    }

    }

