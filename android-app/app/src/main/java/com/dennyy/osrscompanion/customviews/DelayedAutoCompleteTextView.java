package com.dennyy.osrscompanion.customviews;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.CountDownTimer;
import android.util.AttributeSet;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;

import com.dennyy.osrscompanion.R;

public class DelayedAutoCompleteTextView extends AutoCompleteTextView {
    private CountDownTimer autoCompleteTimer;
    private int autoCompleteDelayMs;

    public DelayedAutoCompleteTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.DelayedAutoCompleteTextView, 0, 0);
        setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        try {
            autoCompleteDelayMs = ta.getInt(R.styleable.DelayedAutoCompleteTextView_autoCompleteDelay, 500);
        }
        finally {
            ta.recycle();
        }
    }

    @Override
    protected void performFiltering(final CharSequence text, final int keyCode) {
        if (autoCompleteTimer != null) {
            autoCompleteTimer.cancel();
        }
        autoCompleteTimer = new CountDownTimer(autoCompleteDelayMs, autoCompleteDelayMs) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                DelayedAutoCompleteTextView.super.performFiltering(text, keyCode);
            }
        }.start();
    }


}