package com.example.myschedule;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.io.Serializable;

@Entity(tableName = "LectureDetails")
public class Lecture implements Serializable{
    @ColumnInfo(name = "id")
    @PrimaryKey(autoGenerate = true)
    private int id = 0;
    @ColumnInfo(name = "code")
    private String code;

    @ColumnInfo(name = "name")
    private String name;

    @ColumnInfo(name = "prof")
    private String prof;

    @ColumnInfo(name = "section")
    private String section;

    @ColumnInfo(name = "credit")
    private String credit;

    @ColumnInfo(name = "day")
    private String day;

    @ColumnInfo(name = "starttime")
    private String starttime;

    @ColumnInfo(name = "endtime")
    private String endtime;

    @ColumnInfo(name = "room")
    private String room;

    @ColumnInfo(name = "wantsNotification")
    private boolean wantsNotification;

    @ColumnInfo(name = "reminderMinutes")
    private int reminderMinutes;

    public Lecture(String code, String name, String prof, String section, String credit, String day, String starttime, String endtime, String room, boolean wantsNotification,int reminderMinutes) {
        this.code = code;
        this.name = name;
        this.prof = prof;
        this.section = section;
        this.credit = credit;
        this.day = day;
        this.starttime = starttime;
        this.endtime = endtime;

        this.room = room;
        this.wantsNotification = wantsNotification;
        this.reminderMinutes = reminderMinutes;
    }

    public Lecture() {
    }

    public boolean getWantsNotification()
    {
        return wantsNotification;
    }

    public void setWantsNotification(boolean wantsNotification)
    {
        this.wantsNotification = wantsNotification;
    }



    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProf() {
        return prof;
    }

    public void setProf(String prof) {
        this.prof = prof;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public String getCredit() {
        return credit;
    }

    public void setCredit(String credit) {
        this.credit = credit;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getStarttime() {
        return starttime;
    }

    public void setStarttime(String starttime) {
        this.starttime = starttime;
    }

    public String getEndtime() {
        return endtime;
    }

    public void setEndtime(String endtime) {
        this.endtime = endtime;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getReminderMinutes() {
        return reminderMinutes;
    }

    public void setReminderMinutes(int reminderMinutes) {
        this.reminderMinutes = reminderMinutes;
    }
}
