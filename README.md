# MySchedule

## Project Overview
MySchedule is a high-performance, context-aware scheduling application architected specifically for university students. Unlike generic calendar apps, MySchedule focuses on the "Active Window"—providing a real-time dashboard that tracks lecture proximity, automates notifications, and facilitates a seamless transition between physical classrooms and virtual learning environments.

Engineered to solve the friction of manual timetable management, the app combines a Real-time Live Dashboard with a Gesture-based Weekly Explorer. This provides students with immediate answers to "Where do I need to be right now?" and "How much time do I have left?" 

The application features a sophisticated UI engine that handles theme transitions without the industry-standard "white flash" and a robust data layer capable of ingesting complex schedules via AI-generated JSON.

## Comprehensive Features

### Core Mechanics & Data Management
* **AI-Powered Ingestion:** Support for JSON-based schedule imports. Users can utilize a pre-defined AI prompt to convert raw university syllabus text into structured JSON for instant bulk import.
* **Full CRUD Implementation:** Complete Create, Read, Update, and Delete capabilities for lectures via a streamlined interface.
* **Smart Data Persistence:** Utilizing Room Persistence Library for structured SQL storage and SharedPreferences for lightweight state management (e.g., theme and navigation state).

### UI & UX Excellence
* **Live Dashboard:** A dynamic "Now/Next" view that calculates time remaining in the current lecture or the countdown to the next using the `java.time` API.
* **Explorer View with Gesture Navigation:** A full weekly view supporting high-fidelity swipe gestures (Left/Right) to navigate between days, implemented with custom touch listener logic.
* **Zero-Flash Theme Engine:** A proprietary Snapshot Overlay system that captures the screen state to provide seamless transitions between Light and Dark modes.
* **Optimized Animations:** Reduced XML transition durations (Fade: 250ms, Slide: 200ms) to ensure the UI feels snappy and responsive.

### Intelligence & Connectivity
* **Exact Alarm Notification System:** Leverages `AlarmManager` and `BroadcastReceiver` to trigger precise reminders 10, 30, or 60 minutes before lectures.
* **External Intent Handling:** Integrated `Intent.ACTION_VIEW` logic to safely launch virtual classroom links (Zoom/Teams/Meet) with robust error handling and user confirmation dialogs.

## Technical Architecture

### The Snapshot Overlay Logic
To solve the standard Android issue where `setNightMode` causes a jarring visual flash during Activity recreation:
1. **State Capture:** The app utilizes `Canvas` to draw the current `DecorView` into a `Bitmap` before the theme change.
2. **Persistence:** The bitmap is stored in a static memory cache (`MainActivity.screenshot`).
3. **Overlay Injection:** Upon `onCreate`, the app immediately injects an `ImageView` at the top of the window hierarchy.
4. **Cross-Fade:** A programmatic alpha animation (400ms) fades the old UI snapshot out, revealing the newly themed layout beneath it.

### Chronological Sorting Engine
The app implements a custom `Comparator` to handle time strings and ensure chronological integrity:
```java
lectures.sort((lecture1, lecture2) -> {
    String time1 = lecture1.getStarttime();
    String time2 = lecture2.getStarttime();
    try {
        LocalTime t1 = TimeConverters.convertTime(time1);
        LocalTime t2 = TimeConverters.convertTime(time2);
        return t1.compareTo(t2);
    } catch (Exception e) {
        return 0;
    }
});
```

## Engineering Challenges & Solutions

### 1. Context-Aware Navigation State
* **Challenge:** When the theme is toggled in `ExplorerActivity`, the Activity is recreated. Without specific logic, the app would either always reset to "Monday" or always reset to "Today," losing the user's place if they were looking at a different day.
* **Solution:** Implemented a conditional check that distinguishes between a fresh entry from the Main Activity and an internal recreation (like a theme change):
```java
// Only reset to "Today" if entering fresh from the Main Activity
if (getIntent().getBooleanExtra("isFromMain", false) && savedInstanceState == null) {
    for (int i = 0; i < days.length; i++) {
        if (days[i].equalsIgnoreCase(today.toString())) {
            index = i;
            break;
        }
    }
} else {
    // Otherwise, maintain the day the user was already viewing
    index = savedDay; 
}
```

### 2. Race Conditions in Animation Listeners
* **Challenge:** Rapidly swiping through days in the Explorer view caused animation overlaps and double-triggering of the day-swapping logic. 
* **Solution:** Introduced an `isAnimating` semaphore (boolean flag). This prevents input spam from initiating new transitions until the current `AnimationListener` has successfully called `onAnimationEnd`, ensuring UI stability.

### 3. Memory Management with Bitmaps
* **Challenge:** Storing full-screen screenshots for theme transitions can lead to `OutOfMemoryError` on lower-end devices. 
* **Solution:** Implemented strict nullification of the static `Bitmap` reference (`MainActivity.screenshot = null`) immediately following the `onAnimationEnd` callback. This signals the Garbage Collector to reclaim memory as soon as the transition is no longer visible.

## Installation & Setup

1. **Clone the Repository**
```bash
git clone https://github.com/Abdelrahman-Salama-2008/MySchedule.git
```
2. **Open in Android Studio**
   * Select File > Open and navigate to the project root.
3. **Requirements**
   * Minimum SDK: API 26 (Oreo).
   * Gradle Version: 8.x.
4. **Run the App**
   * Connect an emulator or physical device and click Run 'app'.
