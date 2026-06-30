package com.example.myschedule;

import android.app.TimePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TimePicker;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;

//database imports
import com.example.myschedule.database.AlarmEntity;
import com.example.myschedule.database.RoomDB;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalTime;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.app.AlertDialog;

public class AddLectureActivity extends AppCompatActivity {

    Button timeButton;
    TextView timeUI;

    LocalTime savetime;
    static List<Lecture> lectures;
    static RoomDB database;
    EditText code, name, prof, section, credit, room, link;
    SwitchCompat onlineSwitch, notification, alarmSwitch;
    Button addButton, cancelButton;
    String startTime, endTime;
    String roomText,codeText, nameText, profText, sectionText, creditText, starttimesaved, endtimesaved, day, linktext;
    boolean onlineText, notificationText, alarmSwitchText;
    Spinner daySpinner, reminderSpinner;
    LinearLayout reminderContainer, alarmContainer;
    
    // Multiple Alarms
    private List<AlarmEntity> temporaryAlarms = new ArrayList<>();
    private MaterialButton btnConfigureAlarms;
    private TextView tvAlarmSummary;
    private int[] alarmOffsetValues = {0, 5, 10, 15, 30, 45, 60};
    private String[] alarmOffsetOptions = {"At start time", "5 mins before", "10 mins before", "15 mins before", "30 mins before", "45 mins before", "1 hour before"};


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

        // --- THEME TRANSITION LOGIC ---
        if (MainActivity.screenshot != null) {
            final ImageView overlay = new ImageView(this);
            overlay.setImageBitmap(MainActivity.screenshot);
            ViewGroup root = (ViewGroup) getWindow().getDecorView();
            root.addView(overlay);

            // Fast transition and robust clearing
            overlay.animate()
                    .alpha(0f)
                    .setDuration(400)
                    .setListener(new android.animation.AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(android.animation.Animator animation) {
                            root.removeView(overlay);
                            MainActivity.screenshot = null;
                        }
                    });
        }

        EdgeToEdge.enable(this);
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
        alarmContainer = findViewById(R.id.alarm_reminder_container);
        link = findViewById(R.id.Course_Link);
        TextView btnThemeToggle = findViewById(R.id.btn_theme_toggle);
        alarmSwitch = findViewById(R.id.alarm_Switch);
        
        btnConfigureAlarms = findViewById(R.id.btn_configure_alarms);
        tvAlarmSummary = findViewById(R.id.tv_alarm_summary);

        onlineText = onlineSwitch.isChecked();
        alarmSwitchText = alarmSwitch.isChecked();

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

        // Dynamic visibility for Reminder Spinner and Alarm Spinner
        reminderContainer.setVisibility(notification.isChecked() ? View.VISIBLE : View.GONE);
        alarmContainer.setVisibility(alarmSwitch.isChecked() ? View.VISIBLE : View.GONE);
        
        btnConfigureAlarms.setOnClickListener(v -> showAlarmsDialog());

        setupTimeButton(this);

        //theme change logic
        if (isDarkMode) {
            btnThemeToggle.setText("🌚");
        } else {
            btnThemeToggle.setText("😎");
        }

        btnThemeToggle.setOnClickListener(v -> {
            try {
                View view = getWindow().getDecorView();
                Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                view.draw(canvas);
                MainActivity.screenshot = bitmap;
            } catch (Exception e) {
                MainActivity.screenshot = null;
            }

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

        notification.setOnCheckedChangeListener((buttonView, isChecked) -> {
            reminderContainer.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });

        alarmSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            alarmContainer.setVisibility(isChecked ? View.VISIBLE : View.GONE);
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
                room.setVisibility(View.GONE);
            } else {
                room.setText(getIntent().getStringExtra("LECTURE_ROOM"));
                room.setVisibility(View.VISIBLE);
            }

            notification.setChecked(getIntent().getBooleanExtra("LECTURE_NOTIFICATION", false));
            reminderContainer.setVisibility(notification.isChecked() ? View.VISIBLE : View.GONE);

            alarmSwitch.setChecked(getIntent().getBooleanExtra("LECTURE_ALARM", false));
            alarmContainer.setVisibility(alarmSwitch.isChecked() ? View.VISIBLE : View.GONE);

            // Fetch existing alarms for edit mode
            int lectureId = getIntent().getIntExtra("LECTURE_ID", -1);
            if (lectureId != -1) {
                temporaryAlarms = database.mainDAO().getAlarmsForLecture(lectureId);
                updateAlarmSummary();
            }

            // Set the saved reminder value
            int savedMins = getIntent().getIntExtra("LECTURE_REMINDER", 5);
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
                alarmSwitchText = alarmSwitch.isChecked();

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
                        Lecture lecture = new Lecture(codeText, nameText, profText, sectionText, creditText, day, starttimesaved, endtimesaved, roomText, notificationText, selectedReminderMinutes, linktext, alarmSwitchText, 0);
                        int passedId = getIntent().getIntExtra("LECTURE_ID", -1);
                        lecture.setId(passedId);
                        database.mainDAO().update(lecture);
                        
                        // Save Multiple Alarms
                        saveAlarmsToDB(passedId);
                        
                        NotificationScheduler.scheduleNotificationAndAlarm(AddLectureActivity.this, lecture);
                    } else {
                        Lecture lecture = new Lecture(codeText, nameText, profText, sectionText, creditText, day, starttimesaved, endtimesaved, roomText, notificationText, selectedReminderMinutes,linktext, alarmSwitchText, 0);
                        long id = database.mainDAO().insert(lecture);
                        lecture.setId((int) id);
                        
                        // Save Multiple Alarms
                        saveAlarmsToDB((int) id);
                        
                        NotificationScheduler.scheduleNotificationAndAlarm(AddLectureActivity.this, lecture);
                    }
                    finish();
                }
            }
        });

    }

    private void saveAlarmsToDB(int lectureId) {
        database.mainDAO().deleteAlarmsForLecture(lectureId);
        if (alarmSwitch.isChecked()) {
            for (AlarmEntity alarm : temporaryAlarms) {
                alarm.setLectureId(lectureId);
                database.mainDAO().insertAlarm(alarm);
            }
        }
    }

    private void showAlarmsDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_configure_alarms, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        RecyclerView rvAlarms = dialogView.findViewById(R.id.rv_alarms);
        Spinner spinnerOffsets = dialogView.findViewById(R.id.spinner_alarm_offsets);
        MaterialButton btnAdd = dialogView.findViewById(R.id.btn_add_alarm_offset);
        MaterialButton btnClose = dialogView.findViewById(R.id.btn_close_alarms);

        rvAlarms.setLayoutManager(new LinearLayoutManager(this));
        AlarmAdapter adapter = new AlarmAdapter(temporaryAlarms, alarm -> {
            temporaryAlarms.remove(alarm);
            rvAlarms.getAdapter().notifyDataSetChanged();
            updateAlarmSummary();
        });
        rvAlarms.setAdapter(adapter);

        ArrayAdapter<String> offsetAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, alarmOffsetOptions);
        offsetAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerOffsets.setAdapter(offsetAdapter);

        btnAdd.setOnClickListener(v -> {
            int offset = alarmOffsetValues[spinnerOffsets.getSelectedItemPosition()];
            // Avoid duplicates
            boolean exists = false;
            for (AlarmEntity a : temporaryAlarms) {
                if (a.getTriggerOffsetMinutes() == offset) {
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                temporaryAlarms.add(new AlarmEntity(0, offset));
                temporaryAlarms.sort((a1, a2) -> Integer.compare(a1.getTriggerOffsetMinutes(), a2.getTriggerOffsetMinutes()));
                adapter.notifyDataSetChanged();
                updateAlarmSummary();
            } else {
                Toast.makeText(this, "Alarm already added", Toast.LENGTH_SHORT).show();
            }
        });

        btnClose.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void updateAlarmSummary() {
        if (temporaryAlarms.isEmpty()) {
            tvAlarmSummary.setText("No alarms set");
        } else {
            temporaryAlarms.sort((a1, a2) -> Integer.compare(a1.getTriggerOffsetMinutes(), a2.getTriggerOffsetMinutes()));
            StringBuilder summary = new StringBuilder();
            for (int i = 0; i < temporaryAlarms.size(); i++) {
                int mins = temporaryAlarms.get(i).getTriggerOffsetMinutes();
                if (mins == 0) summary.append("Start");
                else summary.append(mins).append("m");
                
                if (i < temporaryAlarms.size() - 1) summary.append(", ");
            }
            tvAlarmSummary.setText(summary.toString());
        }
    }

    private void setupTimeButton(Context context) {
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
            //code.setError("Course Code is required");
            //valid = false;
            code.setText("");
        }

        if (name.getText().toString().trim().isEmpty()) {
            name.setError("Course Name is required");
            valid = false;
        }

        if (prof.getText().toString().trim().isEmpty()) {
            //prof.setError("Professor Name is required");
            //valid = false;
            prof.setText("");
        }

        if (section.getText().toString().trim().isEmpty()) {
            //section.setError("Section is required");
            //valid = false;
            section.setText("");
        }
        if (credit.getText().toString().trim().isEmpty()) {
            //credit.setError("Credits are required");
            //valid = false;
            credit.setText("");
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