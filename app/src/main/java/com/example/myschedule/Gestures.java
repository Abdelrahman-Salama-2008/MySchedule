package com.example.myschedule;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

public class Gestures implements View.OnTouchListener
{

    private final GestureDetector gestureDetector;

    // 1. Constructor that takes a Context (so we can pass it to the detector)
    public Gestures(Context context) {
        gestureDetector = new GestureDetector(context, new GesturesListener());
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }

    private final class GesturesListener extends GestureDetector.SimpleOnGestureListener
    {

        private int SWIPE_THRESHOLD = 100;
        private int SWIPE_VELOCITY_THRESHOLD = 100;

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY)
        {
            boolean result = false;

            float xDiff = e2.getX() - e1.getX();
            float yDiff = e2.getY() - e1.getY();

            if(Math.abs(xDiff) > Math.abs(yDiff) && Math.abs(xDiff) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD)
            {
                if(xDiff > 0)
                {
                    onSwipeRight();
                }
                else
                {
                    onSwipeLeft();
                }
                result = true;
            }
            return result;
        }


    }
        public void onSwipeRight() {
        }

        public void onSwipeLeft() {
        }
}