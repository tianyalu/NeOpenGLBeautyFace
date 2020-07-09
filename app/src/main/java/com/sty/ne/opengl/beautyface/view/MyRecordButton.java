package com.sty.ne.opengl.beautyface.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.appcompat.widget.AppCompatTextView;

public class MyRecordButton extends AppCompatTextView {
    private OnRecordListener mListener;

    public MyRecordButton(Context context) {
        super(context);
    }

    public MyRecordButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(mListener == null) {
            return false;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                setPressed(true);
                mListener.onStartRecording();
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                setPressed(false);
                mListener.onStopRecording();
                break;
        }
        return true;
    }

    public void setOnRecordListener(OnRecordListener listener) {
        this.mListener = listener;
    }

    public interface OnRecordListener {
        void onStartRecording();
        void onStopRecording();
    }
}
