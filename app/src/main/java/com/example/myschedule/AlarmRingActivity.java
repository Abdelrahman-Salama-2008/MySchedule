package com.example.myschedule;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.google.android.material.button.MaterialButton;

public class AlarmRingActivity extends AppCompatActivity {

    private final BroadcastReceiver closeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (AlarmReceiver.ACTION_CLOSE_ALARM_ACTIVITY.equals(intent.getAction())) {
                finishAndKillProcess();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Register receiver to close activity if dismissed from notification
        IntentFilter filter = new IntentFilter(AlarmReceiver.ACTION_CLOSE_ALARM_ACTIVITY);
        ContextCompat.registerReceiver(this, closeReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED);

        // Force the screen to turn on and bypass the lock screen
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
            android.app.KeyguardManager keyguardManager = (android.app.KeyguardManager) getSystemService(android.content.Context.KEYGUARD_SERVICE);
            if (keyguardManager != null) {
                keyguardManager.requestDismissKeyguard(this, null);
            }
        } else {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                    | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                    | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        }

        setContentView(R.layout.activity_alarm_ring);
        updateUI();

        MaterialButton dismissBtn = findViewById(R.id.btn_dismiss_alarm);
        int notificationId = getIntent().getIntExtra("notification_id", -1);

        dismissBtn.setOnClickListener(v -> {
            // Stop the alarm sound and vibration
            AlarmReceiver.stopAlarm(this);

            // Cancel the notification
            if (notificationId != -1) {
                android.app.NotificationManager manager = (android.app.NotificationManager) getSystemService(android.content.Context.NOTIFICATION_SERVICE);
                if (manager != null) {
                    manager.cancel(notificationId);
                }
            }

            // Close and kill
            finishAndKillProcess();
        });
    }

    private void updateUI() {
        TextView titleText = findViewById(R.id.alarm_title);
        TextView detailText = findViewById(R.id.alarm_details);

        String lectureName = getIntent().getStringExtra("lecture_name");
        if (lectureName == null) lectureName = getString(R.string.class_placeholder);

        String room = getIntent().getStringExtra("room");
        if (room == null) room = getString(R.string.room_tba);

        int minutes = getIntent().getIntExtra("minutes_before", 0);

        titleText.setText(getString(R.string.alarm_title_format, lectureName));
        detailText.setText(getString(R.string.alarm_details_format, minutes, room));
    }

    @Override
    protected void onNewIntent(@NonNull Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        updateUI();
    }

    private void finishAndKillProcess() {
        //Close the activity and remove it from recents
        finishAndRemoveTask();

        //Kill the app process to ensure everything stops immediately
        new android.os.Handler().postDelayed(() -> {
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(0);
        }, 500);
    }

    // Ensure alarm stops if user swipes the app away
    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(closeReceiver);
        } catch (Exception ignored) {}
        AlarmReceiver.stopAlarm(this);
    }
}