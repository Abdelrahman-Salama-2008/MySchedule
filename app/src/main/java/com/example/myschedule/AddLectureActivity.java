package com.example.myschedule;

import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TimePicker;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

//database imports
import com.example.myschedule.database.RoomDB;
import com.example.myschedule.Lecture;

import java.util.ArrayList;
import java.util.List;

import java.time.LocalTime;

public class AddLectureActivity extends AppCompatActivity {

    Button timeButton;
    TextView timeUI;

    TimePickerDialog.OnTimeSetListener timeSetListener;

    LocalTime savetime;
    static List<Lecture> lectures;
    static RoomDB database;
    EditText code, name, prof, section, credit, room;
    SwitchCompat onlineSwitch, notification;
    Button addButton, cancelButton;
    String startTime, endTime;
    String roomText,codeText, nameText, profText, sectionText, creditText, starttimesaved, endtimesaved, day;
    boolean onlineText, notificationText;
    Spinner daySpinner;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_lecture);
        //database
        database = RoomDB.getInstance(this);
        lectures = database.mainDAO().getAll();

        timeUI = findViewById(R.id.display_time);


        Context context = this;
        timeButton = findViewById(R.id.pick_time);

        code = findViewById(R.id.Course_Code);
        name = findViewById(R.id.Course_name);
        prof = findViewById(R.id.Course_Professor);
        section = findViewById(R.id.Course_Section);
        credit = findViewById(R.id.Course_Credit);
        onlineSwitch = findViewById(R.id.online_Switch);
        notification = findViewById(R.id.notification_Switch);
        addButton = findViewById(R.id.add_button);
        cancelButton = findViewById(R.id.cancel_button);
        room = findViewById(R.id.Room_number);

        onlineText = onlineSwitch.isChecked();

        daySpinner = findViewById(R.id.day_spinner);
        String[] days = new String[]{"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday","Friday", "Saturday"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item, days);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        daySpinner.setAdapter(adapter);

        String passedDay = getIntent().getStringExtra("SELECTED_DAY");

        if (passedDay != null) {
            for (int i = 0; i < days.length; i++) {
                if (days[i].equalsIgnoreCase(passedDay)) {
                    daySpinner.setSelection(i);
                    break;
                }
            }
        }


        timeUI.setVisibility(View.GONE);

        onlineSwitch.setOnCheckedChangeListener((buttonView, isChecked) ->
        {
            if (isChecked) {
                room.setVisibility(View.GONE);

            } else {
                room.setVisibility(View.VISIBLE);
            }
        });



        cancelButton.setOnClickListener(v -> finish()); //the cancel button closes the activity

        addButton.setOnClickListener(new View.OnClickListener() {// the save button saves the data to the database
            @Override
            public void onClick(View v) {

                 codeText = code.getText().toString();
                 nameText = name.getText().toString();
                 profText = prof.getText().toString();
                 sectionText = section.getText().toString();
                 creditText = credit.getText().toString();
                 onlineText = onlineSwitch.isChecked();
                 notificationText = notification.isChecked();
                day = daySpinner.getSelectedItem().toString();
                starttimesaved = startTime;
                endtimesaved = endTime;
                if(onlineText)

                    roomText = "Online";
                else
                    roomText = room.getText().toString();

                if(isValid()){
                Lecture lecture = new Lecture(codeText, nameText, profText, sectionText, creditText, day, starttimesaved, endtimesaved, roomText, notificationText);
                database.mainDAO().insert(lecture);
                finish();
                }

            }

            });



        timeButton.setOnClickListener(new View.OnClickListener()// the time button opens a time picker dialog
        {
            @Override
            public void onClick(View v) {
                TimePickerDialog startDialog = new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute)
                    {
                        savetime = LocalTime.of(hourOfDay, minute);
                        startTime = savetime.format(TimeConverters.myFormat);

                        TimePickerDialog endDialog = new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener()
                        {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                savetime = LocalTime.of(hourOfDay, minute);
                                endTime =  savetime.format(TimeConverters.myFormat);
                                timeUI.setText("Start: " + startTime + "\nEnd: " +  endTime);
                                timeUI.setVisibility(View.VISIBLE);
                            }
                        }, savetime.getHour(), savetime.getMinute(), false);
                        endDialog.show();
                    }

                }, LocalTime.now().getHour(), LocalTime.now().getMinute(), false);
                startDialog.show();
                timeUI.setVisibility(View.VISIBLE);
            }
        });

    }

    private boolean isValid() {
        boolean valid = true;

        // 1. Check Course Code
        if (code.getText().toString().trim().isEmpty()) {
            code.setError("Course Code is required");
            valid = false;
        }

        // 2. Check Course Name
        if (name.getText().toString().trim().isEmpty()) {
            name.setError("Course Name is required");
            valid = false;
        }

        // 3. Check Professor
        if (prof.getText().toString().trim().isEmpty()) {
            prof.setError("Professor Name is required");
            valid = false;
        }

        // 4. Check Section & Credits
        if (section.getText().toString().trim().isEmpty()) {
            section.setError("Section is required");
            valid = false;
        }
        if (credit.getText().toString().trim().isEmpty()) {
            credit.setError("Credits are required");
            valid = false;
        }

        // 5. Special Check: Room Number vs Online Switch
        // If it's an online class, we don't strictly need a room number!
        if (!onlineSwitch.isChecked() && room.getText().toString().trim().isEmpty()) {
            room.setError("Room number is required for in-person classes");
            valid = false;
        }

        // 6. Special Check: Time Picker
        // EditTexts have the cool .setError() UI, but TextViews don't show it well.
        // We use a Toast for the time warning.
        if (timeUI.getVisibility() == View.GONE || timeUI.getText().toString().isEmpty()) {
            Toast.makeText(this, "Please select a start and end time", Toast.LENGTH_LONG).show();
            valid = false;
        }

        return valid;
    }

}