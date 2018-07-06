package com.dennyy.osrscompanion.customviews;


import android.app.AlertDialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.dennyy.osrscompanion.R;


public class SeekBarPreference extends DialogPreference implements SeekBar.OnSeekBarChangeListener, OnClickListener {

    private SeekBar mSeekBar;
    private TextView mValueText;
    private Context mContext;

    private String dialogMessage, suffix;
    private int defaultValue, max, min, inc, mValue = 0;

    public SeekBarPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.SeekBarPreference, 0, 0);
        try {
            dialogMessage = ta.getString(R.styleable.SeekBarPreference_dialogMessage);
            suffix = ta.getString(R.styleable.SeekBarPreference_suffix);
            defaultValue = ta.getInt(R.styleable.SeekBarPreference_defValue, 50);
            max = ta.getInt(R.styleable.SeekBarPreference_max, 100);
            min = ta.getInt(R.styleable.SeekBarPreference_min, 0);
            inc = ta.getInt(R.styleable.SeekBarPreference_inc, 10);
        }
        finally {
            ta.recycle();
        }

        if ((max - min) / inc % 1 > 0)
            throw new IllegalArgumentException("Increment value is not divisible between max and min");
    }

    @Override
    protected View onCreateDialogView() {

        LinearLayout.LayoutParams params;
        LinearLayout layout = new LinearLayout(mContext);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(6, 6, 6, 6);

        TextView mSplashText = new TextView(mContext);
        mSplashText.setPadding(30, 10, 30, 10);
        if (dialogMessage != null)
            mSplashText.setText(dialogMessage);
        layout.addView(mSplashText);

        mValueText = new TextView(mContext);
        mValueText.setGravity(Gravity.CENTER_HORIZONTAL);
        mValueText.setTextSize(32);
        mValueText.setTextColor(mContext.getResources().getColor(R.color.text));
        params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.FILL_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        layout.addView(mValueText, params);

        mSeekBar = new SeekBar(mContext);
        mSeekBar.setOnSeekBarChangeListener(this);
        layout.addView(mSeekBar, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        if (shouldPersist())
            mValue = getPersistedInt(defaultValue);

        mSeekBar.setMax(max / inc - (min / inc));
        mSeekBar.setProgress(mValue/inc);

        return layout;
    }

    @Override
    protected void onBindDialogView(View v) {
        super.onBindDialogView(v);
        mSeekBar.setMax(max / inc - (min / inc));
        mSeekBar.setProgress(mValue/inc);
    }

    @Override
    protected void onSetInitialValue(boolean restore, Object defaultValue) {
        super.onSetInitialValue(restore, defaultValue);
        if (restore)
            mValue = shouldPersist() ? getPersistedInt(this.defaultValue) : 0;
        else
            mValue = (Integer) defaultValue;
    }

    @Override
    public void onProgressChanged(SeekBar seek, int value, boolean fromTouch) {
        String t = String.valueOf((value + (min / inc)) * 10);
        mValueText.setText(suffix == null ? t : t.concat(" " + suffix));
    }

    @Override
    public void onStartTrackingTouch(SeekBar seek) {}

    @Override
    public void onStopTrackingTouch(SeekBar seek) {}

    public void setMax(int max) { this.max = max; }

    public int getMax() { return max; }

    public void setProgress(int progress) {
        mValue = progress;
        if (mSeekBar != null)
            mSeekBar.setProgress(progress);
    }

    public int getProgress() { return mValue; }

    @Override
    public void showDialog(Bundle state) {

        super.showDialog(state);

        Button positiveButton = ((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_POSITIVE);
        positiveButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        if (shouldPersist()) {

            mValue = mSeekBar.getProgress();
            persistInt(mSeekBar.getProgress());
            callChangeListener(Integer.valueOf(mSeekBar.getProgress()));
        }

        getDialog().dismiss();
    }
}