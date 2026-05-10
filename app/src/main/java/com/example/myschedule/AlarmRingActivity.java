package com.example.myschedule;

import android.os.Bundle;
import android.view.WindowManager;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;

public class AlarmRingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

        TextView titleText = findViewById(R.id.alarm_title);
        TextView detailText = findViewById(R.id.alarm_details);
        MaterialButton dismissBtn = findViewById(R.id.btn_dismiss_alarm);

        String lectureName = getIntent().getStringExtra("lecture_name");
        String room = getIntent().getStringExtra("room");
        int minutes = getIntent().getIntExtra("minutes_before", 0);

        titleText.setText("Time for " + lectureName + "!");
        detailText.setText("Starts in " + minutes + " minutes\nGo to Room: " + room);

        int notificationId = getIntent().getIntExtra("notification_id", -1);

        dismissBtn.setOnClickListener(v -> {
            // 1. Stop the alarm sound and vibration
            AlarmReceiver.stopAlarm(this);

            // 2. Cancel the notification
            if (notificationId != -1) {
                android.app.NotificationManager manager = (android.app.NotificationManager) getSystemService(android.content.Context.NOTIFICATION_SERVICE);
                if (manager != null) {
                    manager.cancel(notificationId);
                }
            }

            // 3. Close the activity and remove it from recents
            finishAndRemoveTask();

            // 4. Kill the app process to ensure everything stops immediately
            new android.os.Handler().postDelayed(() -> {
                android.os.Process.killProcess(android.os.Process.myPid());
                System.exit(0);
            }, 500);
        });
    }

    // Ensure alarm stops if user swipes the app away
    @Override
    protected void onDestroy() {
        super.onDestroy();
        AlarmReceiver.stopAlarm(this);
    }
}