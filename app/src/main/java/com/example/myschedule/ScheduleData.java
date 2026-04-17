package com.example.myschedule;

import java.time.LocalTime;
import java.util.ArrayList;

public class ScheduleData {
    private static ArrayList<Lecture> lectures;

    public static ArrayList<Lecture> getLectures() {
        if (lectures == null) {
            lectures = new ArrayList<>();
            lectures.add(new Attend("Mth1402","Discrete Math", "Mohammed Alghamdi", "2", "3", "SUNDAY", LocalTime.of(10,0), LocalTime.of(11,50), "25-1.026"));
            lectures.add(new Attend("SE1201","Foundations of Software Engineering", "Tariq Kashmeery", "4", "3", "SUNDAY", LocalTime.of(13,0), LocalTime.of(14,50), "25-1.025"));
            lectures.add(new Attend("ELIE1202","English(1)", "Saleh Alghamdi", "12", "4", "MONDAY", LocalTime.of(8,0), LocalTime.of(11,50), "25-1.024"));
            lectures.add(new Attend("SE1101","OOP Development", "Abdulhadi Al-Eidaroos", "2", "3", "MONDAY", LocalTime.of(13,0), LocalTime.of(14,50), "25-1.026"));
            lectures.add(new Attend("MTH1181","Calculus", "Saber Mansour", "2", "4", "MONDAY", LocalTime.of(16,0), LocalTime.of(17,50), "15-1.006"));
            lectures.add(new Attend("ELIE1202","English (3)(4)", "Saleh Alghamdi", "12", "4", "TUESDAY", LocalTime.of(13,0), LocalTime.of(17,50), "25-1.024"));
            lectures.add(new Attend("SE1101","OOP Development(practical)", "Abdulhadi Al-Eidaroos", "2", "3", "TUESDAY", LocalTime.of(18,0), LocalTime.of(19,50), "12-1.016"));
            lectures.add(new Online("QR2102","Memorize Quran", "Habiballah Alsulami", "64", "2", "WEDNESDAY", LocalTime.of(13,0), LocalTime.of(14,50)));
            lectures.add(new Online("ELIE1202","English (2)", "Naseem Alotaibi", "12", "4", "WEDNESDAY", LocalTime.of(16,0), LocalTime.of(16,50)));
            lectures.add(new Online("CUR1101","University Skills", "Fatimah Amrain", "188", "0", "WEDNESDAY", LocalTime.of(20,0), LocalTime.of(21,50)));
            lectures.add(new Attend("MTH1402","Discrete Math (practical)", "Ismail Daghistani", "3", "3", "THURSDAY", LocalTime.of(8,0), LocalTime.of(9,50), "25-0.022"));
            lectures.add(new Attend("MTH1181","Calculus", "Saber Mansour", "2", "4", "THURSDAY", LocalTime.of(10,0), LocalTime.of(11,50), "15-1.016"));
            lectures.add(new Attend("SE1201","Foundation of Software Engineering", "Tariq Kashmeery", "4", "3", "THURSDAY",LocalTime.of(13,0), LocalTime.of(13,50), "25-1.025"));

        }
        return lectures;
    }
}