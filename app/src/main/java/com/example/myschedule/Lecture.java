package com.example.myschedule;

import java.time.LocalTime;

public abstract class Lecture
{
    private String code, name, prof, section,credit, day;
    private LocalTime Starttime, Endtime;
    private boolean isNotified = false;


     public Lecture(String code, String name, String prof, String section, String credit, String day, LocalTime Starttime, LocalTime Endtime){
         this.code = code;
         this.name = name;
         this.prof = prof;
         this.section = section;
         this.credit = credit;
         this.day = day;
         this.Starttime = Starttime;
         this.Endtime = Endtime;
     }

     public String getCode(){
         return code;
     }

     public String getName(){
         return name;
     }

     public String getProf(){
         return prof;
     }

     public String getSection(){
         return section;
     }

     public String getDay(){
         return day;
     }

     public LocalTime getStarttime(){
         return Starttime;
     }

    public LocalTime getEndtime(){
         return Endtime;
     }


     public String getCredit(){
         return credit;
     }

     public boolean getIsNotified(){
         return isNotified;
     }

     public void setIsNotified(boolean notified)
     {
         isNotified = notified;
     }
}
