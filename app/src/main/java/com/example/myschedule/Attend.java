package com.example.myschedule;
import java.time.LocalTime;


public class Attend extends Lecture {
    private String room;

    public Attend(String code, String name, String prof, String section, String credit, String day, LocalTime Starttime, LocalTime Endtime, String room) {
        super(code, name, prof, section, credit, day, Starttime, Endtime);
        this.room = room;
    }

    public String getRoom(){
        return room;
    }
}
