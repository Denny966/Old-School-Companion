package com.dennyy.oldschoolcompanion.customviews;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;

import com.dennyy.oldschoolcompanion.R;

public class DelayedAutoCompleteTextView extends AutoCompleteTextView {

    public static final int DEFAULT_DELAY = 500;
    private int autoCompleteDelayMs;
    private boolean overrideDismiss;
    private final Handler handler = new Handler();
    private Runnable runnable;

    public DelayedAutoCompleteTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.DelayedAutoCompleteTextView, 0, 0);
        setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        try {
            autoCompleteDelayMs = ta.getInt(R.styleable.DelayedAutoCompleteTextView_autoCompleteDelay, DEFAULT_DELAY);
        }
        finally {
            ta.recycle();
        }
    }

    public void forceDismissDropdown() {
        super.dismissDropDown();
    }

    @Override
    public void dismissDropDown() {
        if (!overrideDismiss) {
            super.dismissDropDown();
        }
    }

    public void setOverrideDismiss(boolean shouldOverride) {
        overrideDismiss = shouldOverride;
    }

    public void setAutoCompleteDelayMs(int autoCompleteDelayMs) {
        this.autoCompleteDelayMs = autoCompleteDelayMs;
    }

    @Override
    protected void performFiltering(final CharSequence text, final int keyCode) {
        handler.removeCallbacks(runnable);
        runnable = new Runnable() {
            @Override
            public void run() {
                DelayedAutoCompleteTextView.super.performFiltering(text, keyCode);
            }
        };
        handler.postDelayed(runnable, autoCompleteDelayMs);
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && isPopupShowing()) {
            forceDismissDropdown();
            return true;
        }

        return super.onKeyPreIme(keyCode, event);
    }
}