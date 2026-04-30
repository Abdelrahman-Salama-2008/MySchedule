package com.example.myschedule;

import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.cardview.widget.CardView;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.time.LocalTime;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatDelegate;

public class MainActivity extends AppCompatActivity {
    public ArrayList<Lecture> lectures = new ArrayList<>();
    Handler handler = new Handler();
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            updateUI();
            handler.postDelayed(this, 60000);
        }
    };

    LinearLayout container;
    DateTimeFormatter myFormat = DateTimeFormatter.ofPattern("h:mm a");
    DateTimeFormatter dateFormater = DateTimeFormatter.ofPattern("EEEE, MMM dd");
    public static android.graphics.Bitmap screenshot = null;
    TextView emptyMessage;
    TextView dateHeader;

    @Override
    protected void onStop() {
        super.onStop();
        handler.removeCallbacks(runnable);
    }

    @Override
    protected void onResume() {
        super.onResume();
        lectures = ScheduleData.getLectures(this);
        updateUI();
        handler.post(runnable);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.fade_in_slow, R.anim.fade_out_slow);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Lecture Reminders";
            String description = "Notifications for upcoming lectures";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel("lecture_channel", name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        boolean isDarkMode = sharedPreferences.getBoolean("isDarkMode", false);

        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        super.onCreate(savedInstanceState);

        overridePendingTransition(R.anim.fade_in_slow, R.anim.fade_out_slow);

        setContentView(R.layout.activity_main);

        // --- THEME TRANSITION LOGIC ---
        if (screenshot != null) {
            final ImageView overlay = new ImageView(this);
            overlay.setImageBitmap(screenshot);
            android.view.ViewGroup root = (android.view.ViewGroup) getWindow().getDecorView();
            root.addView(overlay);
            screenshot = null;
            overlay.animate()
                    .alpha(0f)
                    .setDuration(800)
                    .setListener(new android.animation.AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(android.animation.Animator animation) {
                            root.removeView(overlay);
                        }
                    });
        }

        container = findViewById(R.id.lecture_container);
        emptyMessage = findViewById(R.id.empty);
        dateHeader = findViewById(R.id.dateHeader);

        TextView btnThemeToggle = findViewById(R.id.btn_theme_toggle);

        if (isDarkMode) {
            btnThemeToggle.setText("🌚");
        } else {
            btnThemeToggle.setText("😎");
        }

        btnThemeToggle.setOnClickListener(v -> {
            // 1. Capture the current screen
            View rootView = getWindow().getDecorView().getRootView();
            rootView.setDrawingCacheEnabled(true);
            screenshot = android.graphics.Bitmap.createBitmap(rootView.getDrawingCache());
            rootView.setDrawingCacheEnabled(false);

            boolean currentMode = sharedPreferences.getBoolean("isDarkMode", false);
            boolean newMode = !currentMode;
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("isDarkMode", newMode);
            editor.apply();

            if (newMode) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        });

        createNotificationChannel();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }

        Button fullButton = findViewById(R.id.full_button);
        fullButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ExplorerActivity.class);
            startActivity(intent);
        });

        lectures = ScheduleData.getLectures(this);
        new NotificationScheduler(this).scheduleAllLectures();
    }

    public void updateUI() {
        boolean foundAny = false;
        container.removeAllViews();
        LocalTime currentTime = LocalTime.now();
        DayOfWeek today = LocalDate.now().getDayOfWeek();
        TextView timeLeftText = findViewById(R.id.time_left);

        dateHeader.setText(LocalDate.now().format(dateFormater));

        Duration timeLeft;
        int counter = 0;
        int todaylecturecounter = 0;

        LayoutInflater inflater = getLayoutInflater();

        lectures.sort((lecture1, lecture2) -> {
            String time1 = lecture1.getStarttime();
            String time2 = lecture2.getStarttime();
            try {
                LocalTime t1 = LocalTime.parse(time1, myFormat);
                LocalTime t2 = LocalTime.parse(time2, myFormat);
                return t1.compareTo(t2);
            } catch (Exception e) {
                return 0;
            }
        });

        for (Lecture lecture : lectures) {
            if (lecture.getDay().equalsIgnoreCase(today.toString())) {
                todaylecturecounter++;

                if (TimeConverters.convertTime(lecture.getEndtime()).isAfter(currentTime)) {
                    foundAny = true;
                    counter++;

                    CardView lectureCard = (CardView) inflater.inflate(R.layout.item_course, container, false);
                    View indicator = lectureCard.findViewById(R.id.type_indicator);
                    ImageView roomIcon = lectureCard.findViewById(R.id.room_icon);

                    TextView linkIcon = lectureCard.findViewById(R.id.lecture_link_icon);
                    String classLink = lecture.getLink();

                    if (classLink == null || classLink.isEmpty()) {
                        linkIcon.setVisibility(View.GONE);
                    } else {
                        linkIcon.setVisibility(View.VISIBLE);
                        linkIcon.setOnClickListener(v -> {
                            new AlertDialog.Builder(MainActivity.this)
                                    .setTitle("External Link")
                                    .setMessage("Are you sure you want to exit the app to follow this link?")
                                    .setPositiveButton("Yes", (dialog, which) -> {
                                        try {
                                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(classLink));
                                            startActivity(intent);
                                        } catch (Exception e) {
                                            Toast.makeText(MainActivity.this, "Could not open link", Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .setNegativeButton("No", null)
                                    .show();
                        });
                    }

                    if (lecture.getRoom().equalsIgnoreCase("Online")) {
                        indicator.setBackgroundColor(getResources().getColor(R.color.color_online));
                        roomIcon.setColorFilter(getResources().getColor(R.color.color_online));
                    } else {
                        indicator.setBackgroundColor(getResources().getColor(R.color.color_attend));
                        roomIcon.setColorFilter(getResources().getColor(R.color.color_attend));
                    }

                    if (TimeConverters.convertTime(lecture.getStarttime()).isBefore(LocalTime.now()) && TimeConverters.convertTime(lecture.getEndtime()).isAfter(LocalTime.now())) {
                        lectureCard.setCardBackgroundColor(getResources().getColor(R.color.live));
                        if (counter == 1) {
                            timeLeft = Duration.between(LocalTime.now(), TimeConverters.convertTime(lecture.getEndtime()));
                            timeLeftText.setText("Lecture ends in " + timeLeft.toHours() + " Hours " + timeLeft.toMinutes() % 60 + " Minutes");
                        }
                    } else {
                        if (counter == 1) {
                            timeLeft = Duration.between(currentTime, TimeConverters.convertTime(lecture.getStarttime()));
                            timeLeftText.setText("Next Lecture in " + timeLeft.toHours() + " Hours " + timeLeft.toMinutes() % 60 + " Minutes");
                        }
                    }

                    TextView code = lectureCard.findViewById(R.id.lecture_code);
                    TextView name = lectureCard.findViewById(R.id.lecture_name);
                    TextView prof = lectureCard.findViewById(R.id.lecture_prof);
                    TextView section = lectureCard.findViewById(R.id.lecture_section);
                    TextView credit = lectureCard.findViewById(R.id.Lecture_credit);
                    TextView day = lectureCard.findViewById(R.id.lecture_day);
                    TextView time = lectureCard.findViewById(R.id.lecture_time);
                    TextView room = lectureCard.findViewById(R.id.lecture_room);

                    lectureCard.findViewById(R.id.edit_button).setVisibility(View.GONE);
                    lectureCard.findViewById(R.id.delete_button).setVisibility(View.GONE);
                    lectureCard.findViewById(R.id.notification_incard).setVisibility(View.GONE);

                    code.setText("Code: " + lecture.getCode());
                    name.setText("Course: " + lecture.getName());
                    prof.setText(lecture.getProf());
                    section.setText("Section: " + lecture.getSection());
                    credit.setText("Credit Hours: " + lecture.getCredit());
                    day.setText("Day: " + lecture.getDay());
                    time.setText(lecture.getStarttime() + " - " + lecture.getEndtime());
                    room.setText(lecture.getRoom());

                    container.addView(lectureCard);
                }
            }
        }

        if (todaylecturecounter == 0) {
            timeLeftText.setText("No Lectures Today");
            emptyMessage.setText("No Lectures Today");
        } else {
            emptyMessage.setText("Day Finished 🎉");
            if (!foundAny) {
                timeLeftText.setText("Day Finished 🎉");
            }
        }

        if (!foundAny) {
            emptyMessage.setVisibility(View.VISIBLE);
        } else {
            emptyMessage.setVisibility(View.GONE);
        }
    }
}