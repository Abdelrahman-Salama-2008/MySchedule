package com.example.myschedule;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.myschedule.database.RoomDB;
import com.google.android.material.button.MaterialButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class ImportExportActivity extends AppCompatActivity {

    private EditText jsonInput;

    // ai prompt
    private final String AI_PROMPT = "I am a university student. Extract the schedule I paste below and format it EXACTLY as a JSON array. " +
            "Return ONLY the raw JSON array. Do not include markdown formatting, code blocks, or any text outside the brackets. " +
            "Use EXACTLY these keys for each object: 'code', 'name', 'prof', 'section', 'credit', 'room', 'day', 'starttime', 'endtime', 'link'. " +
            "CRITICAL RULES: " +
            "1. NO EXTRA KEYS: Ignore and discard any extra information that does not fit into the exact keys listed above. " +
            "2. TRANSLATE TO ENGLISH: The final JSON output MUST be entirely in English. " +
            "3. DAYS: Ensure days are fully spelled out and uppercase in English (e.g., 'SUNDAY', 'MONDAY'). " +
            "4. MISSING DATA: If any data is missing, include the key and set its value to \"\". " +
            "5. MULTI-DAY CLASSES: If a course occurs on multiple days, you MUST create a completely separate JSON object for EACH day. " +
            "6. TIME FORMATTING: All 'starttime' and 'endtime' must be in 'h:mm a' format (e.g., '2:00 PM'). " +
            "7. DYNAMIC ORIENTATION: Before extracting data, identify the header row (Days) and header column (Times/Periods). Determine if the table is Left-to-Right or Right-to-Left based on the sequence of days (e.g., if 'Sunday' is on the far right, the table is RTL). " +
            "8. PERIOD DETECTION: If the schedule uses numbers (1, 2, 3) instead of times, search the provided text for a 'Time Table' or 'Legend' that defines those periods. If no legend exists, ask me: 'Your schedule uses periods. Please provide the time mapping.' " +
            "9. CROSS-REFERENCING: I am providing multiple data sources (a grid and a list). Match the Course Code (e.g., SE1101) from the list to the corresponding slot in the grid to combine Professor, Credits, Room, and Time into one object. " +
            "10. CONSECUTIVE PERIODS: If a course occupies multiple back-to-back periods (e.g., Periods 3 and 4), merge them into one object. The start time is the beginning of the first period and the end time is the completion of the last period. " +
            "11. NAME & CLASSIFICATION: Identify if the class is Theoretical (نظري), Practical (عملي), or Activity (نشاط). The 'name' key MUST contain the course title followed by the type in parentheses. Example: 'Calculus (Theory)'. " +
            "12. DATA INTEGRITY: Ensure the Room and Section correctly match the specific time slot extracted from the grid. " +
            "Here is my schedule data: \n\n[PASTE YOUR SCHEDULE/IMAGE TEXT HERE]";

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

        setContentView(R.layout.activity_import_export);

        if (MainActivity.screenshot != null) {
            final ImageView overlay = new ImageView(this);
            overlay.setImageBitmap(MainActivity.screenshot);
            android.view.ViewGroup root = (android.view.ViewGroup) getWindow().getDecorView();
            root.addView(overlay);

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

        jsonInput = findViewById(R.id.et_json_input);
        MaterialButton btnCopyPrompt = findViewById(R.id.btn_copy_prompt);
        MaterialButton btnImport = findViewById(R.id.btn_import_schedule);
        MaterialButton btnExport = findViewById(R.id.btn_export_schedule);
        MaterialButton btnDeleteAll = findViewById(R.id.btn_delete_all);
        TextView btnThemeToggle = findViewById(R.id.btn_theme_toggle);

        if (isDarkMode) {
            btnThemeToggle.setText("🌚");
        } else {
            btnThemeToggle.setText("😎");
        }

        btnThemeToggle.setOnClickListener(v -> {
            try {
                View view = getWindow().getDecorView();
                android.graphics.Bitmap bitmap = android.graphics.Bitmap.createBitmap(view.getWidth(), view.getHeight(), android.graphics.Bitmap.Config.ARGB_8888);
                android.graphics.Canvas canvas = new android.graphics.Canvas(bitmap);
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

        btnCopyPrompt.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("AI Prompt", AI_PROMPT);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, "Prompt copied to clipboard! Paste it into ChatGPT/Gemini.", Toast.LENGTH_LONG).show();
        });

        btnImport.setOnClickListener(v -> importScheduleFromJson());

        btnExport.setOnClickListener(v -> exportScheduleToJson());

        btnDeleteAll.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Delete All Lectures?")
                    .setMessage("Are you sure you want to delete all lectures? This action cannot be undone.")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        //cancel notifications
                        List<Lecture> allLectures = RoomDB.getInstance(this).mainDAO().getAll();
                        NotificationScheduler scheduler = new NotificationScheduler(this);
                        for(Lecture lecture : allLectures){
                            scheduler.cancelSingleLecture(lecture);
                        }

                        //remove the lectures
                        RoomDB.getInstance(this).mainDAO().deleteAll();
                        Toast.makeText(this, "Schedule wiped clean.", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                    .show();
        });
    }

    private void importScheduleFromJson() {
        String rawJson = jsonInput.getText().toString().trim();

        if (rawJson.isEmpty()) {
            Toast.makeText(this, "Please paste the JSON code first.", Toast.LENGTH_SHORT).show();
            return;
        }

        //validation and editing the text
        if (rawJson.startsWith("```json")) {
            rawJson = rawJson.replace("```json", "").replace("```", "").trim();
        } else if (rawJson.startsWith("```")) {
            rawJson = rawJson.replace("```", "").trim();
        }

        try {
            JSONArray jsonArray = new JSONArray(rawJson);
            int importedCount = 0;

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);

                //finds key if not found uses default string
                String code = obj.optString("code", "");
                String name = obj.optString("name", "Unknown Course");
                String prof = obj.optString("prof", "");
                String section = obj.optString("section", "");
                String credit = obj.optString("credit", "");
                String room = obj.optString("room", "");
                String day = obj.optString("day", "SUNDAY").toUpperCase();
                String starttime = obj.optString("starttime", "8:00 AM");
                String endtime = obj.optString("endtime", "9:00 AM");
                String link = obj.optString("link", "");

                boolean wantsNotification = false;
                int reminderMinutes = 15;

                Lecture newLecture = new Lecture(code, name, prof, section, credit, day, starttime, endtime, room, wantsNotification, reminderMinutes, link);
                RoomDB.getInstance(this).mainDAO().insert(newLecture);
                importedCount++;
            }

            jsonInput.setText("");
            Toast.makeText(this, "Successfully imported " + importedCount + " lectures!", Toast.LENGTH_LONG).show();
            finish();

        } catch (JSONException e) {
            Toast.makeText(this, "Error parsing data. Please make sure you pasted valid JSON.", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    private void exportScheduleToJson() {
        List<Lecture> allLectures = RoomDB.getInstance(this).mainDAO().getAll();

        if (allLectures.isEmpty()) {
            Toast.makeText(this, "Your schedule is empty! Nothing to export.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            JSONArray jsonArray = new JSONArray();
            for (Lecture lecture : allLectures) {
                JSONObject obj = new JSONObject();
                obj.put("code", lecture.getCode());
                obj.put("name", lecture.getName());
                obj.put("prof", lecture.getProf());
                obj.put("section", lecture.getSection());
                obj.put("credit", lecture.getCredit());
                obj.put("room", lecture.getRoom());
                obj.put("day", lecture.getDay());
                obj.put("starttime", lecture.getStarttime());
                obj.put("endtime", lecture.getEndtime());
                obj.put("link", lecture.getLink());

                jsonArray.put(obj);
            }

            String jsonOutput = jsonArray.toString(4);

            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Exported Schedule", jsonOutput);
            clipboard.setPrimaryClip(clip);

            Toast.makeText(this, "Schedule copied to clipboard! You can now share it.", Toast.LENGTH_LONG).show();

        } catch (JSONException e) {
            Toast.makeText(this, "Failed to export schedule.", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
}