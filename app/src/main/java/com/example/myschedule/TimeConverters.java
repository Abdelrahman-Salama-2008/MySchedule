package com.example.myschedule;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class TimeConverters {

    static DateTimeFormatter myFormat = DateTimeFormatter.ofPattern("h:mm a");
    public static String convertTime(LocalTime time) {
        return time.format(myFormat).toString();
    }

    public static LocalTime convertTime(String time) {
        return LocalTime.parse(time, myFormat);
    }

}
