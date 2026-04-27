package com.example.myschedule;

import android.content.Context;

import com.example.myschedule.database.RoomDB;

import java.util.ArrayList;
import java.util.List;

public class ScheduleData {
    public static ArrayList<Lecture> getLectures(Context context) {
        RoomDB database = RoomDB.getInstance(context);
        List<Lecture> lectures = database.mainDAO().getAll();
        return new ArrayList<>(lectures);
    }
}