# MySchedule — Advanced University Lecture Alarm Engine (v3.0)

MySchedule is a robust, high-performance Android application tailored for university students to track weekly courses and manage highly customizable reminder states. Built natively in Java, the application features a relational multi-alarm scheduling architecture, a modular popup UI configuration layer, and an ironclad full-screen system override for alarm events.

---

## 🛠️ Tech Stack & Architecture

* **Core Language:** Java 11
* **Minimum SDK:** API 24 (Android 7.0 Nougat)
* **Target SDK:** API 36 (Android 16)
* **Database Layer:** Room Persistence Library (v2.8.4) providing local SQLite abstraction.
* **UI Framework:** Material Components (MaterialButton, CardView) with dynamic dark theme optimization.
* **Asynchronous Engine:** Core Library Desugaring for native `java.time` API support across historical SDK versions.

---

## 📊 Relational Database Schema

The core persistence tier establishes a clean **One-to-Many (1:N)** relationship between lectures and individual alarm configurations.

### 1. Lecture Entity (`LectureDetails` table)
| Column | Type | Description |
| :--- | :--- | :--- |
| `id` 🔑 | Integer | Auto-generated primary key. |
| `name` | String | The title/name of the university course. |
| `starttime` | String | Formatted lecture commencement time. |
| `day` | String | Active day of the week (e.g., "Monday"). |
| `wantsNotification` | Boolean | Toggle for standard passive reminders. |
| `isAlarmEnabled` | Boolean | Global override for highly intrusive waking alarms. |

### 2. Alarm Entity (`alarms` table)
| Column | Type | Description |
| :--- | :--- | :--- |
| `id` 🔑 | Integer | Auto-generated primary key. |
| `lecture_id` 🔗 | Integer | Foreign Key referencing `LectureDetails(id)`. |
| `trigger_offset_minutes`| Integer | Minute offset before class (e.g., `0` = on time, `15` = 15m early). |
| `is_active` | Boolean | Activation state of the specific offset instance. |

> ⚠️ **Data Integrity Rule:** Enforces `ForeignKey.CASCADE` on `lecture_id`. Deleting a lecture automatically purges all child alarm entries from the device storage.

---

## 📂 Project Directory Tree

```text
app/src/main/java/com/example/myschedule/
├── database/
│   ├── AlarmEntity.java          # Relational multi-alarm data model
│   ├── Lecture.java              # Main course data model
│   ├── MainDAO.java              # Database Access Object for room queries
│   └── RoomDB.java               # Room Database singleton wrapper
├── AddLectureActivity.java       # Form entry & modular alarm config manager
├── AlarmAdapter.java             # RecyclerView adapter for the popup configuration list
├── AlarmReceiver.java            # Ghost Engine: OS wake-up intent router
├── AlarmRingActivity.java        # Full-screen lock screen overlay interface
├── ExplorerActivity.java         # Weekly schedule visualization matrix
├── ImportExportActivity.java     # Serialization utilities & database sanitation
├── MainActivity.java             # Core entry point dashboard & theme manager
├── NotificationReceiver.java     # Passive background reminder handler
├── NotificationScheduler.java    # Chrono background scheduling framework
└── TimeConverters.java           # Time string parsing and calculation utilities
```

---

### ⚙️ Core Engineering Modules

1. **The Ghost Engine (`NotificationScheduler.java`)** Calculates raw temporal deltas by inspecting the relational `alarms` table for upcoming courses. It hooks into the Android OS `AlarmManager` using the high-precision `setAlarmClock()` API to guarantee absolute scheduling delivery, bypassing system-level battery optimizations or doze restrictions.

2. **Full-Screen Intrusive Waking Service (`AlarmRingActivity.java`)** An activity configured to bypass device keyguards. It forces an immutable foreground panel to display when an alarm fires, locking interface focus until explicitly handled.

3. **Asynchronous Dismiss Loop (`AlarmReceiver.handleDismiss()`)** To handle edge cases where a user accidentally minimizes or background-tasks the application during a ringing state, a decoupled `BroadcastReceiver` hooks directly into a persistent, high-priority (`IMPORTANCE_HIGH`) ongoing notification. Users can safely shut down background audio streams and vibration routines directly from the status bar shade.

---

### 💻 Key Implementation Snippets

#### Dynamic Multi-Alarm Scheduling Loop
```java
List<AlarmEntity> alarms = RoomDB.getInstance(context).mainDAO().getAlarmsForLecture(lecture.getId());
for (AlarmEntity alarm : alarms) {
    if (alarm.isActive()) {
        Calendar calendar = calculateAlarmTime(lecture, alarm.getTriggerOffsetMinutes());
        PendingIntent pendingIntent = getPendingIntent(lecture, "ALARM", alarm.getTriggerOffsetMinutes(), alarm.getId());
        
        // Registering with system framework for precise wake-up execution
        AlarmManager.AlarmClockInfo alarmClockInfo = new AlarmManager.AlarmClockInfo(calendar.getTimeInMillis(), pendingIntent);
        alarmManager.setAlarmClock(alarmClockInfo, pendingIntent);
    }
}
```

#### High-Priority Intent Binding & Channel Setup
```java
// Prepare Dismiss Action for Background Receiver Notification
Intent dismissIntent = new Intent(context, AlarmReceiver.class);
dismissIntent.setAction(ACTION_DISMISS_ALARM);
PendingIntent dismissPendingIntent = PendingIntent.getBroadcast(
        context, id + 1, dismissIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

// Notification Channel Setup for Lock Screen Penetration
NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Alarms", NotificationManager.IMPORTANCE_HIGH);
channel.setLockscreenVisibility(NotificationCompat.VISIBILITY_PUBLIC);

// Binding properties to build the ongoing background service channel
NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
        .setCategory(NotificationCompat.CATEGORY_ALARM)
        .setFullScreenIntent(fullScreenPendingIntent, true) // Launches AlarmRingActivity directly
        .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Dismiss", dismissPendingIntent);
```
