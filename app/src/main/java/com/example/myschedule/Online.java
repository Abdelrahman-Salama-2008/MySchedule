package com.example.myschedule;

import java.time.LocalTime;

public class Online extends Lecture {

    public Online(String code, String name, String prof, String section, String credit, String day, LocalTime Starttime, LocalTime Endtime) {
        super(code, name, prof, section, credit, day, Starttime, Endtime);
    }
}
