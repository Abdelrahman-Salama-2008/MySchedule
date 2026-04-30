package com.example.myschedule;

import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TimePicker;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

//database imports
import com.example.myschedule.database.RoomDB;

import java.util.List;
import java.time.LocalTime;

public class AddLectureActivity extends AppCompatActivity {

    Button timeButton;
    TextView timeUI;

    LocalTime savetime;
    static List<Lecture> lectures;
    static RoomDB database;
    EditText code, name, prof, section, credit, room, link;
    SwitchCompat onlineSwitch, notification;
    Button addButton, cancelButton;
    String startTime, endTime;
    String roomText,codeText, nameText, profText, sectionText, creditText, starttimesaved, endtimesaved, day, linktext;
    boolean onlineText, notificationText;
    Spinner daySpinner, reminderSpinner;
    LinearLayout reminderContainer;


    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.fade_in_slow, R.anim.fade_out_slow);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        android.content.SharedPreferences sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        boolean isDarkMode = sharedPreferences.getBoolean("isDarkMode", false);

        if (isDarkMode) {
            androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO);
        }

        super.onCreate(savedInstanceState);

        overridePendingTransition(R.anim.fade_in_slow, R.anim.fade_out_slow);

        // --- THEME TRANSITION LOGIC ---
        if (MainActivity.screenshot != null) {
            final android.widget.ImageView overlay = new android.widget.ImageView(this);
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

        androidx.activity.EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_lecture);

        //database
        database = RoomDB.getInstance(this);
        lectures = database.mainDAO().getAll();

        timeUI = findViewById(R.id.display_time);

        Context context = this;
        timeButton = findViewById(R.id.pick_time);

        TextView title = findViewById(R.id.activity_title);
        String[] days = new String[]{"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday","Friday", "Saturday"};

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
        daySpinner = findViewById(R.id.day_spinner);
        reminderContainer = findViewById(R.id.reminder_container);
        link = findViewById(R.id.Course_Link);

        onlineText = onlineSwitch.isChecked();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item, days);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        daySpinner.setAdapter(adapter);

        //reminder spinner
        String[] reminderOptions = {"5 mins before", "10 mins before", "15 mins before", "30 mins before", "45 mins before", "1 hour before"};
        int[] reminderValues = {5, 10, 15, 30, 45, 60};

        reminderSpinner = findViewById(R.id.reminder_spinner);
        ArrayAdapter<String> reminderAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, reminderOptions);
        reminderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        reminderSpinner.setAdapter(reminderAdapter);

        // Dynamic visibility for Reminder Spinner
        reminderContainer.setVisibility(notification.isChecked() ? View.VISIBLE : View.GONE);

        notification.setOnCheckedChangeListener((buttonView, isChecked) -> {
            reminderContainer.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });

        if(getIntent().getBooleanExtra("IS_EDIT_MODE", false)) {
            title.setText("Edit Lecture");
            addButton.setText("Update");
            code.setText(getIntent().getStringExtra("LECTURE_CODE"));
            name.setText(getIntent().getStringExtra("LECTURE_NAME"));
            prof.setText(getIntent().getStringExtra("LECTURE_PROF"));
            section.setText(getIntent().getStringExtra("LECTURE_SECTION"));
            credit.setText(getIntent().getStringExtra("LECTURE_CREDIT"));
            link.setText(getIntent().getStringExtra("LECTURE_LINK"));

            if(getIntent().getStringExtra("LECTURE_ROOM").equalsIgnoreCase("Online")) {
                onlineSwitch.setChecked(true);
            } else {
                room.setText(getIntent().getStringExtra("LECTURE_ROOM"));
            }

            notification.setChecked(getIntent().getBooleanExtra("LECTURE_NOTIFICATION", false));
            reminderContainer.setVisibility(notification.isChecked() ? View.VISIBLE : View.GONE);

            // Set the saved reminder value
            int savedMins = getIntent().getIntExtra("LECTURE_REMINDER", 15);
            for (int i = 0; i < reminderValues.length; i++) {
                if (reminderValues[i] == savedMins) {
                    reminderSpinner.setSelection(i);
                    break;
                }
            }

            startTime = getIntent().getStringExtra("LECTURE_STARTTIME");
            endTime = getIntent().getStringExtra("LECTURE_ENDTIME");
            timeUI.setText("Start: " + startTime + "\nEnd: " + endTime);
            timeUI.setVisibility(View.VISIBLE);

            for(int i = 0; i < days.length; i++) {
                if(days[i].equalsIgnoreCase(getIntent().getStringExtra("LECTURE_DAY"))) {
                    daySpinner.setSelection(i);
                    break;
                }
            }
        } else {
            timeUI.setVisibility(View.GONE);
        }

        String passedDay = getIntent().getStringExtra("SELECTED_DAY");
        if (passedDay != null && !getIntent().getBooleanExtra("IS_EDIT_MODE", false)) {
            for (int i = 0; i < days.length; i++) {
                if (days[i].equalsIgnoreCase(passedDay)) {
                    daySpinner.setSelection(i);
                    break;
                }
            }
        }

        onlineSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                room.setVisibility(View.GONE);
            } else {
                room.setVisibility(View.VISIBLE);
            }
        });

        cancelButton.setOnClickListener(v -> finish()); //the cancel button closes the activity

        addButton.setOnClickListener(new View.OnClickListener() {
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
                linktext = link.getText().toString();

                if(linktext.isEmpty())
                {
                    linktext = "";
                }
                else{
                    if(!linktext.startsWith("http://") && !linktext.startsWith("https://"))
                        linktext = "https://" + linktext;
                }

                // Grab the selected reminder time
                int selectedReminderMinutes = reminderValues[reminderSpinner.getSelectedItemPosition()];

                if(onlineText)
                    roomText = "Online";
                else
                    roomText = room.getText().toString();

                if(isValid()) {
                    if(getIntent().getBooleanExtra("IS_EDIT_MODE", false)) {
                        // Pass selectedReminderMinutes to the constructor
                        Lecture lecture = new Lecture(codeText, nameText, profText, sectionText, creditText, day, starttimesaved, endtimesaved, roomText, notificationText, selectedReminderMinutes, linktext);
                        int passedId = getIntent().getIntExtra("LECTURE_ID", -1);
                        lecture.setId(passedId);
                        database.mainDAO().update(lecture);
                    } else {
                        Lecture lecture = new Lecture(codeText, nameText, profText, sectionText, creditText, day, starttimesaved, endtimesaved, roomText, notificationText, selectedReminderMinutes,linktext);
                        database.mainDAO().insert(lecture);
                    }
                    finish();
                }
            }
        });

        timeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog startDialog = new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        savetime = LocalTime.of(hourOfDay, minute);
                        startTime = savetime.format(TimeConverters.myFormat);

                        TimePickerDialog endDialog = new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                savetime = LocalTime.of(hourOfDay, minute);
                                endTime =  savetime.format(TimeConverters.myFormat);
                                timeUI.setText("Start: " + startTime + "\nEnd: " + endTime);
                                timeUI.setVisibility(View.VISIBLE);
                            }
                        }, savetime.getHour(), savetime.getMinute(), false);
                        endDialog.show();
                    }

                }, LocalTime.now().getHour(), LocalTime.now().getMinute(), false);
                startDialog.show();
            }
        });
    }

    private boolean isValid() {
        boolean valid = true;

        if (code.getText().toString().trim().isEmpty()) {
            code.setError("Course Code is required");
            valid = false;
        }

        if (name.getText().toString().trim().isEmpty()) {
            name.setError("Course Name is required");
            valid = false;
        }

        if (prof.getText().toString().trim().isEmpty()) {
            prof.setError("Professor Name is required");
            valid = false;
        }

        if (section.getText().toString().trim().isEmpty()) {
            section.setError("Section is required");
            valid = false;
        }
        if (credit.getText().toString().trim().isEmpty()) {
            credit.setError("Credits are required");
            valid = false;
        }

        if (!onlineSwitch.isChecked() && room.getText().toString().trim().isEmpty()) {
            room.setError("Room number is required for in-person classes");
            valid = false;
        }

        if (timeUI.getVisibility() == View.GONE || timeUI.getText().toString().isEmpty()) {
            Toast.makeText(this, "Please select a start and end time", Toast.LENGTH_LONG).show();
            valid = false;
        }

        return valid;
    }
}