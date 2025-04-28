package com.example.questapplication;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import androidx.appcompat.widget.AppCompatEditText;

public class ClearableEditText extends AppCompatEditText {

    private static final int DRAWABLE_END = 2;

    public ClearableEditText(Context context) {
        super(context);
    }

    public ClearableEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ClearableEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP &&
                getCompoundDrawables()[DRAWABLE_END] != null &&
                event.getRawX() >= (getRight() - getCompoundDrawables()[DRAWABLE_END].getBounds().width())) {
            setText("");
            performClick(); // trigger accessibility event
            return true;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean performClick() {
        return super.performClick(); // formally override for accessibility
    }
}

