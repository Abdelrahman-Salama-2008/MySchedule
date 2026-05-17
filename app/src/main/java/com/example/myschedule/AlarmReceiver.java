package com.example.myschedule;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

public class AlarmReceiver extends BroadcastReceiver {

    public static MediaPlayer mediaPlayer;
    public static Vibrator vibrator;
    private static final String CHANNEL_ID = "alarm_channel_v1";

    @Override
    public void onReceive(Context context, Intent intent) {
        // Confirm receiver triggered
        Toast.makeText(context, "Alarm Triggered!", Toast.LENGTH_LONG).show();

        int id = intent.getIntExtra("notification_id", (int) System.currentTimeMillis());
        String lectureName = intent.getStringExtra("lecture_name");
        String room = intent.getStringExtra("room");
        int minutes = intent.getIntExtra("minutes_before", 0);

        // 1. Start Sound and Vibration immediately
        startAlarm(context);

        // 2. Prepare Intent for Full-Screen Activity
        Intent wakeUpIntent = new Intent(context, AlarmRingActivity.class);
        wakeUpIntent.putExtra("lecture_name", lectureName);
        wakeUpIntent.putExtra("room", room);
        wakeUpIntent.putExtra("minutes_before", minutes);
        wakeUpIntent.putExtra("notification_id", id);
        wakeUpIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent fullScreenPendingIntent = PendingIntent.getActivity(
                context,
                id,
                wakeUpIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // 3. Setup Notification Channel
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Lecture Alarms",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Critical notifications for your classes");
            channel.setSound(null, null); // Sound handled by MediaPlayer
            channel.enableVibration(false); // Disable channel vibration to avoid conflicts with manual vibration
            channel.setLockscreenVisibility(NotificationCompat.VISIBILITY_PUBLIC);
            
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

        // 4. Build Notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.clock)
                .setContentTitle("WAKE UP: " + (lectureName != null ? lectureName : "Class"))
                .setContentText("Room " + room + " in " + minutes + " mins")
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setFullScreenIntent(fullScreenPendingIntent, true)
                .setOngoing(true)
                .setAutoCancel(false)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

        if (notificationManager != null) {
            notificationManager.notify(id, builder.build());
        }

        // 5. Force launch activity (works on some devices if unlocked)
        try {
            context.startActivity(wakeUpIntent);
        } catch (Exception ignored) {}
    }

    private void startAlarm(Context context) {
        Context appContext = context.getApplicationContext();
        stopAlarm(appContext);

        // Vibration
        vibrator = (Vibrator) appContext.getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null && vibrator.hasVibrator()) {
            long[] pattern = {0, 800, 400, 800, 400}; // Aggressive pattern
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                AudioAttributes aa = new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setFlags(AudioAttributes.FLAG_AUDIBILITY_ENFORCED)
                        .build();
                vibrator.vibrate(VibrationEffect.createWaveform(pattern, 0), aa);
            } else {
                vibrator.vibrate(pattern, 0);
            }
        }

        // Sound
        try {
            Uri alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            if (alarmUri == null) alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(appContext, alarmUri);
            mediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build());
            mediaPlayer.setLooping(true);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (Exception e) {
            // fallback
        }
    }

    public static void stopAlarm(Context context) {
        // 1. Stop Audio
        if (mediaPlayer != null) {
            try {
                if (mediaPlayer.isPlaying()) mediaPlayer.stop();
                mediaPlayer.release();
            } catch (Exception ignored) {}
            mediaPlayer = null;
        }

        // 2. Stop Vibration
        try {
            if (context != null) {
                // For modern Android (API 31+)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    android.os.VibratorManager vm = (android.os.VibratorManager) context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
                    if (vm != null) vm.cancel();
                }
                // Standard Vibrator cancel
                Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
                if (v != null) v.cancel();
            }
            if (vibrator != null) {
                vibrator.cancel();
            }
        } catch (Exception ignored) {}
        vibrator = null;
    }
}
