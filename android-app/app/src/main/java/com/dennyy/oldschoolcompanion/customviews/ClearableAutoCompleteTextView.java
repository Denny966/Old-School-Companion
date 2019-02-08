package com.dennyy.oldschoolcompanion.customviews;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.dennyy.oldschoolcompanion.R;

public class ClearableAutoCompleteTextView extends RelativeLayout implements TextWatcher, View.OnClickListener {
    private Button clearButton;
    private DelayedAutoCompleteTextView textView;
    private String hint;
    private int threshold;
    private int autoCompleteDelay;
    private boolean overrrideDismiss;

    public ClearableAutoCompleteTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.ClearableAutoCompleteTextView, 0, 0);
        try {
            hint = ta.getString(R.styleable.ClearableAutoCompleteTextView_hint);
            threshold = ta.getInt(R.styleable.ClearableAutoCompleteTextView_threshold, 3);
            overrrideDismiss = ta.getBoolean(R.styleable.ClearableAutoCompleteTextView_overrideDismiss, false);
            autoCompleteDelay = ta.getInt(R.styleable.ClearableAutoCompleteTextView_clearableAutoCompleteDelay, DelayedAutoCompleteTextView.DEFAULT_DELAY);
        }
        finally {
            ta.recycle();
        }
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.clearable_autocompletetextview, this);
    }


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        clearButton = findViewById(R.id.autocomplete_textview_clear_button);
        clearButton.setOnClickListener(this);
        textView = findViewById(R.id.delayed_autocomplete_textview);
        textView.setAutoCompleteDelayMs(autoCompleteDelay);
        textView.setHint(hint);
        textView.addTextChangedListener(this);
        textView.setOverrideDismiss(overrrideDismiss);
        setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                textView.setThreshold(threshold);
                return false;
            }
        });
    }

    public DelayedAutoCompleteTextView getAutoCompleteTextView() {
        return textView;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        if (s.toString().trim().length() == 0) {
            clearButton.setVisibility(GONE);
        }
        else {
            clearButton.setVisibility(VISIBLE);
        }
    }

    @Override
    public void onClick(View v) {
        textView.setText("");
    }
}
