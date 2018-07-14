package com.dennyy.osrscompanion.customviews;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dennyy.osrscompanion.R;


public class LineIndicatorButton extends LinearLayout {

    private String buttonText;
    private int indicatorColor;
    private int inactiveIndicatorColor;
    private boolean active;

    private LinearLayout buttonIndicatorLayout;

    public LineIndicatorButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.LineIndicatorButton, 0, 0);
        try {
            buttonText = ta.getString(R.styleable.LineIndicatorButton_buttonText);
            indicatorColor = ta.getColor(R.styleable.LineIndicatorButton_indicatorColor, getResources().getColor(R.color.button_indicator_active));
            inactiveIndicatorColor = ta.getColor(R.styleable.LineIndicatorButton_inactiveIndicatorColor, getResources().getColor(R.color.button_indicator_inactive));
            active = ta.getBoolean(R.styleable.LineIndicatorButton_active, false);
        }
        finally {
            ta.recycle();
        }
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.line_indicator_button, this);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        TextView buttonTextView = findViewById(R.id.lineindicatorbutton_text);
        buttonIndicatorLayout = findViewById(R.id.lineindicatorbutton_indicator);

        buttonTextView.setText(buttonText);
        setActive(active);
    }

    public void setActive(boolean active) {
        setIndicatorColor(active ? indicatorColor : inactiveIndicatorColor);
        this.active = active;
    }

    public boolean isActive() {
        return active;
    }

    private void setIndicatorColor(int color) {
        Drawable background = buttonIndicatorLayout.getBackground();
        if (background instanceof ShapeDrawable) {
            ((ShapeDrawable) background).getPaint().setColor(color);
        }
        else if (background instanceof GradientDrawable) {
            ((GradientDrawable) background).setColor(color);
        }
        else if (background instanceof ColorDrawable) {
            ((ColorDrawable) background).setColor(color);
        }
    }
}