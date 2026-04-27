package com.example.myschedule;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.cardview.widget.CardView;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.time.LocalTime;
import java.time.Duration;
import java.time.format.DateTimeFormatter;

public class MainActivity extends AppCompatActivity
{
    public ArrayList<Lecture> lectures = new ArrayList<>();
    Handler handler = new Handler();
    Runnable runnable = new Runnable(){
        @Override
        public void run()
        {
            updateUI();
            handler.postDelayed(this, 60000);
        }
    };

    LinearLayout container;
    DateTimeFormatter myFormat = DateTimeFormatter.ofPattern("h:mm a");
    DateTimeFormatter dateFormater = DateTimeFormatter.ofPattern("EEEE, MMM dd");
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
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        container = findViewById(R.id.lecture_container);
        emptyMessage = findViewById(R.id.empty);
        dateHeader = findViewById(R.id.dateHeader);

        // Inside MainActivity.onCreate
        createNotificationChannel();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }


        Button fullButton = findViewById(R.id.full_button);
        fullButton.setOnClickListener(v ->{
            Intent intent = new Intent(MainActivity.this, ExplorerActivity.class);
            startActivity(intent);
        });


        lectures = ScheduleData.getLectures(this);

        new NotificationScheduler(this).scheduleAllLectures();


    }

    public void updateUI()
    {
        boolean foundAny = false;
        container.removeAllViews();
        LocalTime currentTime = LocalTime.now();
        DayOfWeek today = LocalDate.now().getDayOfWeek();
        TextView timeLeftText = findViewById(R.id.time_left);

        dateHeader.setText(LocalDate.now().format(dateFormater));

        Duration timeLeft;
        int counter = 0;
        int todaylecturecounter = 0;
        String todayname = today.toString();


        LayoutInflater inflater = getLayoutInflater();

        // Sort the list by time before showing the cards
        lectures.sort((lecture1, lecture2) -> {
            // 1. Get the clean strings
            String time1 = lecture1.getStarttime();
            String time2 = lecture2.getStarttime();

            try {
                LocalTime t1 = LocalTime.parse(time1, myFormat);
                LocalTime t2 = LocalTime.parse(time2, myFormat);

                return t1.compareTo(t2);
            } catch (Exception e) {
                return 0; // If there are any errors, return
            }
        });


        for (Lecture lecture : lectures) {
            if (lecture.getDay().equalsIgnoreCase(today.toString())) {
                todaylecturecounter++;

                if (TimeConverters.convertTime(lecture.getEndtime()).isAfter(currentTime)) {
                    foundAny = true;
                    counter++;

                    CardView lectureCard =(CardView) inflater.inflate(R.layout.item_course, container, false);
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
                    if(TimeConverters.convertTime(lecture.getStarttime()).isBefore(LocalTime.now()) && TimeConverters.convertTime(lecture.getEndtime()).isAfter(LocalTime.now()))//current lecture
                    {
                        lectureCard.setCardBackgroundColor(getResources().getColor(R.color.live));
                        if(counter == 1) {
                            timeLeft = Duration.between(LocalTime.now(), TimeConverters.convertTime(lecture.getEndtime()));
                            //time till lecture end
                            timeLeftText.setText("Lecture ends in " + timeLeft.toHours() + " Hours " + timeLeft.toMinutes() % 60 + " Minutes");
                        }
                    }
                    else {
                        if(counter == 1)
                        {
                            timeLeft = Duration.between(currentTime, TimeConverters.convertTime(lecture.getStarttime()));//time till next lecture
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

                    Button deleteButton = lectureCard.findViewById(R.id.delete_button);
                    deleteButton.setVisibility(View.GONE);
                    SwitchCompat notification_incard = lectureCard.findViewById(R.id.notification_incard);
                    notification_incard.setVisibility(View.GONE);


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
        if(todaylecturecounter == 0)
        {
            timeLeftText.setText("No Lectures Today");
            emptyMessage.setText("No Lectures Today");
        }
        else{
            emptyMessage.setText("Day Finished 🎉");
            if(!foundAny)
            {
                timeLeftText.setText("Day Finished 🎉");
            }
        }

        if (!foundAny) {
            emptyMessage.setVisibility(View.VISIBLE);
        }else {
            emptyMessage.setVisibility(View.GONE);
        }
    }
}