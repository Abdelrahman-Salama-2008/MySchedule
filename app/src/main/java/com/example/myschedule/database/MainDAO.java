package com.example.myschedule.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import static androidx.room.OnConflictStrategy.REPLACE;

import com.example.myschedule.Lecture;
import com.example.myschedule.database.AlarmEntity;

import java.util.List;

@Dao
public interface MainDAO {
    @Insert(onConflict = REPLACE)//add
    long insert(Lecture lecture);

    @Delete
    void delete(Lecture lecture);

    @Query("SELECT * FROM LectureDetails")
    List<Lecture> getAll();

    @Query("DELETE FROM LectureDetails")
    void deleteAll();

    @Query("UPDATE LectureDetails SET wantsNotification = :wantsNotification WHERE id = :id")
    void updateNotification(int id, boolean wantsNotification);

    @Query("UPDATE LectureDetails SET isAlarmEnabled = :isAlarmEnabled WHERE id = :id")
    void updateAlarm(int id, boolean isAlarmEnabled);

    @Update
    void update(Lecture lecture);

    // Alarm related queries
    @Insert(onConflict = REPLACE)
    void insertAlarm(AlarmEntity alarm);

    @Delete
    void deleteAlarm(AlarmEntity alarm);

    @Query("SELECT * FROM alarms WHERE lecture_id = :lectureId")
    List<AlarmEntity> getAlarmsForLecture(int lectureId);

    @Query("SELECT * FROM alarms WHERE is_active = 1")
    List<AlarmEntity> getAllActiveAlarms();
    
    @Query("DELETE FROM alarms WHERE lecture_id = :lectureId")
    void deleteAlarmsForLecture(int lectureId);
}