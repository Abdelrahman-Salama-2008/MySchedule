package com.example.myschedule.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import com.example.myschedule.Lecture;

@Entity(tableName = "alarms",
        foreignKeys = @ForeignKey(entity = Lecture.class,
                parentColumns = "id",
                childColumns = "lecture_id",
                onDelete = ForeignKey.CASCADE))
public class AlarmEntity {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "lecture_id")
    private int lectureId;

    @ColumnInfo(name = "trigger_offset_minutes")
    private int triggerOffsetMinutes;

    @ColumnInfo(name = "is_active")
    private boolean isActive = true;

    public AlarmEntity(int lectureId, int triggerOffsetMinutes) {
        this.lectureId = lectureId;
        this.triggerOffsetMinutes = triggerOffsetMinutes;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getLectureId() { return lectureId; }
    public void setLectureId(int lectureId) { this.lectureId = lectureId; }

    public int getTriggerOffsetMinutes() { return triggerOffsetMinutes; }
    public void setTriggerOffsetMinutes(int triggerOffsetMinutes) { this.triggerOffsetMinutes = triggerOffsetMinutes; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
}