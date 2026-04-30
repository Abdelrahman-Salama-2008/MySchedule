package com.example.myschedule;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;

import com.example.myschedule.database.RoomDB;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ExplorerActivity extends AppCompatActivity {
    ArrayList<Lecture> lectures = new ArrayList<>();
    LinearLayout container;
    TextView dateHeader;
    Button prevButton;
    Button nextButton;
    Button addLectureButton;
    TextView dayName;

    public String days[] = new String[]{"SUNDAY", "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY"};
    public int index;
    DayOfWeek today = LocalDate.now().getDayOfWeek();

    DateTimeFormatter myFormat = DateTimeFormatter.ofPattern("h:mm a");
    DateTimeFormatter dateFormater = DateTimeFormatter.ofPattern("EEEE, MMM dd");
    FrameLayout gesture_space;
    android.widget.ScrollView explorerScrollView;


    @Override
    protected void onResume() {
        super.onResume();
        lectures = ScheduleData.getLectures(this);
        updateUI();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.fade_in_slow, R.anim.fade_out_slow);
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

        setContentView(R.layout.activity_explorer);

        // --- THEME TRANSITION LOGIC ---
        if (MainActivity.screenshot != null) {
            final ImageView overlay = new ImageView(this);
            overlay.setImageBitmap(MainActivity.screenshot);
            android.view.ViewGroup root = (android.view.ViewGroup) getWindow().getDecorView();
            root.addView(overlay);
            MainActivity.screenshot = null;
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

        lectures = ScheduleData.getLectures(this);
        container = findViewById(R.id.lecture_container);
        prevButton = findViewById(R.id.btn_prev);
        nextButton = findViewById(R.id.btn_next);
        dayName = findViewById(R.id.explorer_day_name);
        addLectureButton = findViewById(R.id.Add_lecture_button);
        gesture_space = findViewById(R.id.gesture_container);
        explorerScrollView = findViewById(R.id.explorer_scroll_view);

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
            MainActivity.screenshot = android.graphics.Bitmap.createBitmap(rootView.getDrawingCache());
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


        Gestures gestureListener = new Gestures(this) {
            @Override
            public void onSwipeLeft() {
                nextDay();
            }

            @Override
            public void onSwipeRight() {
                prevDay();
            }};

        gesture_space.setOnTouchListener(gestureListener);
        explorerScrollView.setOnTouchListener(gestureListener);

        Button LiveButton = findViewById(R.id.Live_button);
        LiveButton.setOnClickListener(v ->{
            finish();
        });

        Button settingsButton = findViewById(R.id.settings_button);
        settingsButton.setOnClickListener(v -> {
            Intent intent = new Intent(ExplorerActivity.this, ImportExportActivity.class);
            startActivity(intent);
        });

        addLectureButton.setOnClickListener(v -> {
            Intent intent = new Intent(ExplorerActivity.this, AddLectureActivity.class);
            intent.putExtra("SELECTED_DAY", days[index]);
            startActivity(intent);
        });

        for (int i = 0; i < days.length; i++) {
            if(days[i].equalsIgnoreCase(today.toString()))
                index = i;
        }
        dayName.setText(days[index]);

        dateHeader = findViewById(R.id.dateHeader);
        dateHeader.setText(LocalDate.now().format(dateFormater));

        if(prevButton != null)
            prevButton.setOnClickListener(v -> prevDay());

        if(nextButton != null)
            nextButton.setOnClickListener(v -> nextDay());

        updateUI();
    }

    public void nextDay() {
        Animation slideOut = AnimationUtils.loadAnimation(this, R.anim.slide_out_left);

        slideOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}
            @Override
            public void onAnimationRepeat(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                if(index < days.length - 1) index++; else index = 0;
                dayName.setText(days[index]);
                updateUI();

                Animation slideIn = AnimationUtils.loadAnimation(ExplorerActivity.this, R.anim.slide_in_right);

                // Both move together
                explorerScrollView.startAnimation(slideIn);
                dayName.startAnimation(slideIn);
            }
        });

        // Both move out together
        explorerScrollView.startAnimation(slideOut);
        dayName.startAnimation(slideOut);
    }

    public void prevDay() {
        Animation slideOut = AnimationUtils.loadAnimation(this, R.anim.slide_out_right);

        slideOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}
            @Override
            public void onAnimationRepeat(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                if(index > 0) index--; else index = days.length - 1;
                dayName.setText(days[index]);
                updateUI();

                Animation slideIn = AnimationUtils.loadAnimation(ExplorerActivity.this, R.anim.slide_in_left);

                // Both move together
                explorerScrollView.startAnimation(slideIn);
                dayName.startAnimation(slideIn);
            }
        });

        // Both move out together
        explorerScrollView.startAnimation(slideOut);
        dayName.startAnimation(slideOut);
    }

    public void updateUI() {
        boolean foundAny = false;
        container.removeAllViews();
        LocalTime currentTime = LocalTime.now();

        dateHeader.setText(LocalDate.now().format(dateFormater));

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

            if (lecture.getDay().equalsIgnoreCase(days[index])) {
                foundAny = true;
                View lectureCard = inflater.inflate(R.layout.item_course, container, false);
                View indicator = lectureCard.findViewById(R.id.type_indicator);
                ImageView roomIcon = lectureCard.findViewById(R.id.room_icon);

                //link logic
                TextView linkIcon = lectureCard.findViewById(R.id.lecture_link_icon);
                String classLink = lecture.getLink();

                if (classLink == null || classLink.isEmpty()) {
                    linkIcon.setVisibility(View.GONE);
                } else {
                    linkIcon.setVisibility(View.VISIBLE);
                    linkIcon.setOnClickListener(v -> {
                        new AlertDialog.Builder(ExplorerActivity.this)
                                .setTitle("External Link")
                                .setMessage("Are you sure you want to exit the app to follow this link?")
                                .setPositiveButton("Yes", (dialog, which) -> {
                                    try {
                                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(classLink));
                                        startActivity(intent);
                                    } catch (Exception e) {
                                        Toast.makeText(ExplorerActivity.this, "Could not open link", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .setNegativeButton("No", null)
                                .show();
                    });
                }

                if(lecture.getRoom().equalsIgnoreCase("Online")) {
                    indicator.setBackgroundColor(getResources().getColor(R.color.color_online));
                    roomIcon.setColorFilter(getResources().getColor(R.color.color_online));
                } else {
                    indicator.setBackgroundColor(getResources().getColor(R.color.color_attend));
                    roomIcon.setColorFilter(getResources().getColor(R.color.color_attend));
                }

                TextView code = lectureCard.findViewById(R.id.lecture_code);
                TextView name = lectureCard.findViewById(R.id.lecture_name);
                TextView prof = lectureCard.findViewById(R.id.lecture_prof);
                TextView section = lectureCard.findViewById(R.id.lecture_section);
                TextView credit = lectureCard.findViewById(R.id.Lecture_credit);
                TextView day = lectureCard.findViewById(R.id.lecture_day);
                TextView time = lectureCard.findViewById(R.id.lecture_time);
                TextView room = lectureCard.findViewById(R.id.lecture_room);
                Button editButton = lectureCard.findViewById(R.id.edit_button);

                Button deleteButton = lectureCard.findViewById(R.id.delete_button);
                deleteButton.setVisibility(View.VISIBLE);
                SwitchCompat notification_incard = lectureCard.findViewById(R.id.notification_incard);
                notification_incard.setChecked(lecture.getWantsNotification());
                notification_incard.setVisibility(View.VISIBLE);

                notification_incard.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    lecture.setWantsNotification(isChecked);
                    RoomDB.getInstance(ExplorerActivity.this).mainDAO().updateNotification(lecture.getId(), isChecked);

                    NotificationScheduler scheduler = new NotificationScheduler(ExplorerActivity.this);
                    if (isChecked) {
                        scheduler.scheduleSingleLecture(lecture);
                    } else {
                        scheduler.cancelSingleLecture(lecture);
                    }
                });

                code.setText("Code: " + lecture.getCode());
                name.setText("Course: " + lecture.getName());
                prof.setText(lecture.getProf());
                section.setText("Section: " + lecture.getSection());
                credit.setText("Credit Hours: " + lecture.getCredit());
                day.setText("Day: " + lecture.getDay());
                time.setText(lecture.getStarttime() + " - " + lecture.getEndtime());
                room.setText(lecture.getRoom());


                deleteButton.setOnClickListener(v -> {
                    AlertDialog.Builder deleteConfirmation = new AlertDialog.Builder(this);
                    deleteConfirmation.setTitle("Delete Lecture?");
                    deleteConfirmation.setMessage("Are you sure you want to delete this lecture?");

                    deleteConfirmation.setPositiveButton("Yes", (dialog, which) -> {
                        new NotificationScheduler(this).cancelSingleLecture(lecture);
                        RoomDB.getInstance(ExplorerActivity.this).mainDAO().delete(lecture);
                        lectures.remove(lecture);
                        updateUI();
                    });

                    deleteConfirmation.setNegativeButton("No", (dialog, which) -> {
                        dialog.dismiss();
                    });

                    deleteConfirmation.show();
                });

                editButton.setOnClickListener(v -> {
                    Intent intent = new Intent(ExplorerActivity.this, AddLectureActivity.class);
                    intent.putExtra("IS_EDIT_MODE", true);
                    intent.putExtra("SELECTED_DAY", days[index]);
                    intent.putExtra("LECTURE_ID", lecture.getId());
                    intent.putExtra("LECTURE_CODE", lecture.getCode());
                    intent.putExtra("LECTURE_NAME", lecture.getName());
                    intent.putExtra("LECTURE_PROF", lecture.getProf());
                    intent.putExtra("LECTURE_SECTION", lecture.getSection());
                    intent.putExtra("LECTURE_CREDIT", lecture.getCredit());
                    intent.putExtra("LECTURE_DAY", lecture.getDay());
                    intent.putExtra("LECTURE_STARTTIME", lecture.getStarttime());
                    intent.putExtra("LECTURE_ENDTIME", lecture.getEndtime());
                    intent.putExtra("LECTURE_ROOM", lecture.getRoom());
                    intent.putExtra("LECTURE_NOTIFICATION", lecture.getWantsNotification());
                    intent.putExtra("LECTURE_REMINDER", lecture.getReminderMinutes());
                    intent.putExtra("LECTURE_LINK", lecture.getLink());

                    startActivity(intent);
                });

                container.addView(lectureCard);
            }
        }
        if (!foundAny) {
            TextView emptyMessage = findViewById(R.id.empty);
            emptyMessage.setVisibility(View.VISIBLE);
        }else {
            TextView emptyMessage = findViewById(R.id.empty);
            emptyMessage.setVisibility(View.GONE);
        }

    }
}