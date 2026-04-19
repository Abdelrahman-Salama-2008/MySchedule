package com.example.myschedule;

import android.content.Intent;
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
    DateTimeFormatter myFormat = DateTimeFormatter.ofPattern("hh:mm a");
    DateTimeFormatter dateFormater = DateTimeFormatter.ofPattern("EEEE, MMM dd");
    TextView emptyMessage;
    TextView dateHeader;

    @Override
    protected void onStop() {
        super.onStop();
        handler.removeCallbacks(runnable);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        container = findViewById(R.id.lecture_container);
        emptyMessage = findViewById(R.id.empty);
        dateHeader = findViewById(R.id.dateHeader);


        Button fullButton = findViewById(R.id.full_button);
        fullButton.setOnClickListener(v ->{
            Intent intent = new Intent(MainActivity.this, ExplorerActivity.class);
            startActivity(intent);
        });

        // This connects the Java code to your XML layout
        lectures = ScheduleData.getLectures();

        handler.post(runnable);
        // Your test log
        Log.d("MainActivityLog", "Log message: MainActivity has started!");

    }

    public void updateUI()
    {
        boolean foundAny = false; // if no course available no we will use it to display a message
        container.removeAllViews();
        LocalTime currentTime = LocalTime.now();
        DayOfWeek today = LocalDate.now().getDayOfWeek();
        TextView timeLeftText = findViewById(R.id.time_left);

        dateHeader.setText(LocalDate.now().format(dateFormater));

        Duration timeLeft;
        int counter = 0;
        String todayname = today.toString();


        LayoutInflater inflater = getLayoutInflater();
        for (Lecture lecture : lectures) {
            if (lecture.getDay().equalsIgnoreCase(today.toString())) {
                if (lecture.getEndtime().isAfter(currentTime)) {
                    foundAny = true;
                    counter++;

                    View lectureCard = inflater.inflate(R.layout.item_course, container, false);
                    View indicator = lectureCard.findViewById(R.id.type_indicator);
                    ImageView roomIcon = lectureCard.findViewById(R.id.room_icon);

                    if(lecture instanceof Online)
                    {
                        indicator.setBackgroundColor(getResources().getColor(R.color.color_online));
                        roomIcon.setColorFilter(getResources().getColor(R.color.color_online));
                    }
                    else
                    {
                        indicator.setBackgroundColor(getResources().getColor(R.color.color_attend));
                        roomIcon.setColorFilter(getResources().getColor(R.color.color_attend));
                    }
                    if(lecture.getStarttime().isBefore(LocalTime.now()) && lecture.getEndtime().isAfter(LocalTime.now()))//current lecture
                    {
                        lectureCard.setBackgroundColor(getResources().getColor(R.color.live));
                        if(counter == 1) {
                            timeLeft = Duration.between(LocalTime.now(), lecture.getEndtime()); //not sure
                            //time till lecture end
                            timeLeftText.setText("Lecture ends in " + timeLeft.toHours() + " Hours " + timeLeft.toMinutes() % 60 + " Minutes");
                        }
                    }
                    else {
                        if(counter == 1)
                        {
                            timeLeft = Duration.between(currentTime, lecture.getStarttime());//time till next lecture
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

                    code.setText("Code: " + lecture.getCode());
                    name.setText("Course: " + lecture.getName());
                    prof.setText(lecture.getProf());
                    section.setText("Section: " + lecture.getSection());
                    credit.setText("Credit Hours: " + lecture.getCredit());
                    day.setText("Day: " + lecture.getDay());
                    time.setText(lecture.getStarttime().format(myFormat).toString() + " - " + lecture.getEndtime().format(myFormat).toString());
                    if (lecture instanceof Attend) {
                        room.setText(((Attend) lecture).getRoom());
                    } else {
                        room.setText("Online");
                    }

                    container.addView(lectureCard);

                }
            }
        }
        if(todayname.equalsIgnoreCase("SATURDAY") || todayname.equalsIgnoreCase("FRIDAY"))
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