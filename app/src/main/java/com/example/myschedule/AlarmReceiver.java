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
    public static final String ACTION_DISMISS_ALARM = "com.example.myschedule.ACTION_DISMISS_ALARM";
    public static final String ACTION_CLOSE_ALARM_ACTIVITY = "com.example.myschedule.ACTION_CLOSE_ALARM_ACTIVITY";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (ACTION_DISMISS_ALARM.equals(intent.getAction())) {
            int notificationId = intent.getIntExtra("notification_id", -1);
            handleDismiss(context, notificationId);
            return;
        }

        int id = intent.getIntExtra("notification_id", (int) System.currentTimeMillis());
        String lectureName = intent.getStringExtra("lecture_name");
        String room = intent.getStringExtra("room");
        int minutes = intent.getIntExtra("minutes_before", 0);

        // Start Sound and Vibration immediately
        startAlarm(context);

        // Prepare Intent for Full-Screen Activity
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

        // Prepare Dismiss Action for Notification
        Intent dismissIntent = new Intent(context, AlarmReceiver.class);
        dismissIntent.setAction(ACTION_DISMISS_ALARM);
        dismissIntent.putExtra("notification_id", id);
        PendingIntent dismissPendingIntent = PendingIntent.getBroadcast(
                context,
                id + 1,
                dismissIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // 4. Setup Notification Channel
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    context.getString(R.string.notification_channel_name),
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription(context.getString(R.string.notification_channel_desc));
            channel.setSound(null, null); // Sound handled by MediaPlayer
            channel.enableVibration(false);
            channel.setLockscreenVisibility(NotificationCompat.VISIBILITY_PUBLIC);
            
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

        // Build Notification
        String title = context.getString(R.string.alarm_title_format, 
                (lectureName != null ? lectureName : context.getString(R.string.class_placeholder)));
        String details = context.getString(R.string.notification_details_format, 
                minutes, (room != null ? room : context.getString(R.string.room_tba)));

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.clock)
                .setContentTitle(title)
                .setContentText(details)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setFullScreenIntent(fullScreenPendingIntent, true)
                .setOngoing(true)
                .setAutoCancel(false)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .addAction(android.R.drawable.ic_menu_close_clear_cancel, 
                        context.getString(R.string.notification_dismiss), dismissPendingIntent);

        if (notificationManager != null) {
            notificationManager.notify(id, builder.build());
        }

        // 6. Force launch activity
        try {
            context.startActivity(wakeUpIntent);
        } catch (Exception ignored) {}
    }

    private void handleDismiss(Context context, int notificationId) {
        // Stop Sound/Vibration
        stopAlarm(context);

        // Cancel Notification
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null && notificationId != -1) {
            manager.cancel(notificationId);
        }

        // Broadcast to Activity to close it
        Intent closeIntent = new Intent(ACTION_CLOSE_ALARM_ACTIVITY);
        closeIntent.setPackage(context.getPackageName());
        context.sendBroadcast(closeIntent);
    }

    private void startAlarm(Context context) {
        Context appContext = context.getApplicationContext();
        stopAlarm(appContext);

        vibrator = (Vibrator) appContext.getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null && vibrator.hasVibrator()) {
            long[] pattern = {0, 800, 400, 800, 400};
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
        } catch (Exception ignored) {}
    }

    public static void stopAlarm(Context context) {
        if (mediaPlayer != null) {
            try {
                if (mediaPlayer.isPlaying()) mediaPlayer.stop();
                mediaPlayer.release();
            } catch (Exception ignored) {}
            mediaPlayer = null;
        }

        try {
            if (context != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    android.os.VibratorManager vm = (android.os.VibratorManager) context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
                    if (vm != null) vm.cancel();
                }
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