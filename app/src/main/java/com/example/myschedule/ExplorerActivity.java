package com.example.myschedule;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity; // 1. Added missing import

import org.w3c.dom.Text;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

// 2. Fixed "A" and "C" in AppCompatActivity
public class ExplorerActivity extends AppCompatActivity
{
    ArrayList<Lecture> lectures = new ArrayList<>();
    LinearLayout container;
    TextView dateHeader;
    Button prevButton;
    Button nextButton;
    TextView dayName;

    public String days[] = new String[]{"SUNDAY", "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY"};
    public int index;
    DayOfWeek today = LocalDate.now().getDayOfWeek();

    DateTimeFormatter myFormat = DateTimeFormatter.ofPattern("hh:mm a");
    DateTimeFormatter dateFormater = DateTimeFormatter.ofPattern("EEEE, MMM dd");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_explorer);

        lectures = ScheduleData.getLectures();//lecture data
        container = findViewById(R.id.lecture_container);
        prevButton = findViewById(R.id.btn_prev);
        nextButton = findViewById(R.id.btn_next);
        dayName = findViewById(R.id.explorer_day_name);

        Button LiveButton = findViewById(R.id.Live_button);
        LiveButton.setOnClickListener(v ->{
            finish();
        });

        for (int i = 0; i < days.length; i++) {
            if(days[i].equalsIgnoreCase(today.toString()))
                index = i;
        }
        dayName.setText(days[index]);

        dateHeader = findViewById(R.id.dateHeader);
        dateHeader.setText(LocalDate.now().format(dateFormater));

        if(prevButton != null)
            prevButton.setOnClickListener(v -> {
                if(index > 0) {
                    index--;
                    dayName.setText(days[index]);
                    updateUI();
                }
            });

        if(nextButton != null)
            nextButton.setOnClickListener(v -> {
                if(index < days.length - 1) {
                    index++;
                    dayName.setText(days[index]);
                    updateUI();
                }
            });

        updateUI();
    }

    public void updateUI() {
        boolean foundAny = false; // if no course available no we will use it to display a message
        container.removeAllViews();
        LocalTime currentTime = LocalTime.now();
        //today = LocalDate.now().getDayOfWeek();

        dateHeader.setText(LocalDate.now().format(dateFormater));

        LayoutInflater inflater = getLayoutInflater();
        for (Lecture lecture : lectures) {
            if (lecture.getDay().equalsIgnoreCase(days[index])) {
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
        if (!foundAny) {
            TextView emptyMessage = findViewById(R.id.empty);
            emptyMessage.setVisibility(View.VISIBLE);
        }else {
            TextView emptyMessage = findViewById(R.id.empty);
            emptyMessage.setVisibility(View.GONE);
        }

    }
}