package com.example.myschedule;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;


public class NotificationReceiver extends BroadcastReceiver
{

    @Override
    public void onReceive(Context context, Intent intent)
    {
        String lectureName = intent.getStringExtra("lecture_name");
        int minutes_before = intent.getIntExtra("minutes_before", 0);

        //set up the NotificationManager
        NotificationManager manager = context.getSystemService(NotificationManager.class);

        // build Notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "lecture_channel")
                .setSmallIcon(R.drawable.clock)
                .setContentTitle("Class starting soon!")
                .setContentText(lectureName + " starts in " + minutes_before + " minutes.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        //show it
        int notificationID = (int) System.currentTimeMillis();
        if (manager != null)
        {
            manager.notify(notificationID, builder.build());
        }
    }

    }

