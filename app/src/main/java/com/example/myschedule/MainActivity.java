package com.example.myschedule;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import android.os.Handler;

public class MainActivity extends AppCompatActivity
{
    ArrayList<Lecture> lectures = new ArrayList<>();
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
    TextView emptyMessage;

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
        // This connects the Java code to your XML layout


        lectures.add(new Attend("Mth1402","Discrete Math", "Mohammed Alghamdi", "2", "3", "SUNDAY", LocalTime.of(10,0), LocalTime.of(11,50), "25-1.026"));
        lectures.add(new Attend("SE1201","Foundations of Software Engineering", "Tariq Kashmeery", "4", "3", "SUNDAY", LocalTime.of(13,0), LocalTime.of(14,50), "25-1.025"));
        lectures.add(new Attend("ELIE1202","English(1)", "Saleh Alghamdi", "12", "4", "MONDAY", LocalTime.of(8,0), LocalTime.of(11,50), "25-1.024"));
        lectures.add(new Attend("SE1101","OOP Development", "Abdulhadi Al-Eidaroos", "2", "3", "MONDAY", LocalTime.of(13,0), LocalTime.of(14,50), "25-1.026"));
        lectures.add(new Attend("MTH1181","Calculus", "Saber Mansour", "2", "4", "MONDAY", LocalTime.of(16,0), LocalTime.of(17,50), "15-1.006"));
        lectures.add(new Attend("ELIE1202","English (3)(4)", "Saleh Alghamdi", "12", "4", "TUESDAY", LocalTime.of(13,0), LocalTime.of(17,50), "25-1.024"));
        lectures.add(new Attend("SE1101","OOP Development(practical)", "Abdulhadi Al-Eidaroos", "2", "3", "TUESDAY", LocalTime.of(18,0), LocalTime.of(19,50), "12-1.016"));
        lectures.add(new Online("QR2102","Memorize Quran", "Habiballah Alsulami", "64", "2", "WEDNESDAY", LocalTime.of(13,0), LocalTime.of(14,50)));
        lectures.add(new Online("ELIE1202","English (2)", "Naseem Alotaibi", "12", "4", "WEDNESDAY", LocalTime.of(16,0), LocalTime.of(16,50)));
        lectures.add(new Online("CUR1101","University Skills", "Fatimah Amrain", "188", "0", "WEDNESDAY", LocalTime.of(20,0), LocalTime.of(21,50)));
        lectures.add(new Attend("MTH1402","Discrete Math (practical)", "Ismail Daghistani", "3", "3", "THURSDAY", LocalTime.of(8,0), LocalTime.of(9,50), "25-0.022"));
        lectures.add(new Attend("MTH1181","Calculus", "Saber Mansour", "2", "4", "THURSDAY", LocalTime.of(10,0), LocalTime.of(11,50), "15-1.016"));
        lectures.add(new Attend("SE1201","Foundation of Software Engineering", "Tariq Kashmeery", "4", "3", "THURSDAY",LocalTime.of(13,0), LocalTime.of(13,50), "25-1.025"));


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

        LayoutInflater inflater = getLayoutInflater();
        for (Lecture lecture : lectures) {
            if (lecture.getDay().equalsIgnoreCase(today.toString())) {
                if (lecture.getEndtime().isAfter(currentTime)) {
                    foundAny = true;
                    View lectureCard = inflater.inflate(R.layout.item_course, container, false);
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
                    prof.setText("Professor: " + lecture.getProf());
                    section.setText("Section: " + lecture.getSection());
                    credit.setText("Credit Hours: " + lecture.getCredit());
                    day.setText("Day: " + lecture.getDay());
                    time.setText("Time: " + lecture.getStarttime().format(myFormat).toString() + " - " + lecture.getEndtime().format(myFormat).toString());
                    if (lecture instanceof Attend) {
                        room.setText(("Room: " + ((Attend) lecture).getRoom()));
                    } else {
                        room.setText("Room: Online");
                    }

                    container.addView(lectureCard);

                }
            }
        }
        if (!foundAny) {
            emptyMessage.setVisibility(View.VISIBLE);
        }else {
            emptyMessage.setVisibility(View.GONE);
        }
    }
}