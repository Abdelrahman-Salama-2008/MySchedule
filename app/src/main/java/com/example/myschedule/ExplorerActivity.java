package com.example.myschedule;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.example.myschedule.database.RoomDB;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ExplorerActivity extends AppCompatActivity
{
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_explorer);

        lectures = ScheduleData.getLectures(this);//lecture data
        container = findViewById(R.id.lecture_container);
        prevButton = findViewById(R.id.btn_prev);
        nextButton = findViewById(R.id.btn_next);
        dayName = findViewById(R.id.explorer_day_name);
        addLectureButton = findViewById(R.id.Add_lecture_button);
        gesture_space = findViewById(R.id.gesture_container);
        explorerScrollView = findViewById(R.id.explorer_scroll_view);

        Gestures gestureListener = new Gestures(this) {
            @Override
            public void onSwipeLeft()
            {
                // Swipe Left -> Next Day
                nextDay();
            }

            @Override
            public void onSwipeRight()
            {
                // Swipe Right -> Previous Day
                prevDay();
            }};

        // Set listener on both the container and the scrollview to ensure swipes are caught
        gesture_space.setOnTouchListener(gestureListener);
        explorerScrollView.setOnTouchListener(gestureListener);


        Button LiveButton = findViewById(R.id.Live_button);
        LiveButton.setOnClickListener(v ->{
            finish();
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

    public void nextDay()
    {
        if(index < days.length - 1) {
            index++;
            dayName.setText(days[index]);
            updateUI();
        }
        else {
            index = 0;
            dayName.setText(days[index]);
            updateUI();
        }
    }

    public void prevDay()
    {
        if(index > 0)
        {
            index--;
            dayName.setText(days[index]);
            updateUI();
        }
        else {
            index = days.length - 1;
            dayName.setText(days[index]);
            updateUI();
        }
    }

    public void updateUI() {
        boolean foundAny = false;
        container.removeAllViews();
        LocalTime currentTime = LocalTime.now();

        dateHeader.setText(LocalDate.now().format(dateFormater));

        LayoutInflater inflater = getLayoutInflater();

        // Sort the list by time before showing the cards
        lectures.sort((lecture1, lecture2) -> {

            String time1 = lecture1.getStarttime();
            String time2 = lecture2.getStarttime();

            try {

                LocalTime t1 = LocalTime.parse(time1, myFormat);
                LocalTime t2 = LocalTime.parse(time2, myFormat);


                return t1.compareTo(t2);
            } catch (Exception e) {
                return 0; //return if there are any error
            }
        });

        Button deleteAllButton = findViewById(R.id.deleteAll_button);

        deleteAllButton.setOnClickListener(v ->{
            AlertDialog.Builder deleteConfirmation = new AlertDialog.Builder(this);
            deleteConfirmation.setTitle("Delete All Lectures?");
            deleteConfirmation.setMessage("Are you sure you want to delete all lectures?, This action cannot be undone.");

            deleteConfirmation.setPositiveButton("Yes", (dialog, which) -> {
                NotificationScheduler scheduler = new NotificationScheduler(this);
                for(Lecture lecture: lectures){
                    scheduler.cancelSingleLecture(lecture);
                }
                RoomDB.getInstance(ExplorerActivity.this).mainDAO().deleteAll();
                lectures.clear();
                updateUI();
            });

            deleteConfirmation.setNegativeButton("No", (dialog, which) -> {
                dialog.dismiss();
            });
            deleteConfirmation.show();
        });

        for (Lecture lecture : lectures) {



            if (lecture.getDay().equalsIgnoreCase(days[index])) {
                foundAny = true;
                View lectureCard = inflater.inflate(R.layout.item_course, container, false);
                View indicator = lectureCard.findViewById(R.id.type_indicator);
                ImageView roomIcon = lectureCard.findViewById(R.id.room_icon);

                if(lecture.getRoom().equalsIgnoreCase("Online"))
                {
                    indicator.setBackgroundColor(getResources().getColor(R.color.color_online));
                    roomIcon.setColorFilter(getResources().getColor(R.color.color_online));
                }
                else
                {
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
                Button editButton = lectureCard.findViewById(R.id.edit_button); //added this line

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