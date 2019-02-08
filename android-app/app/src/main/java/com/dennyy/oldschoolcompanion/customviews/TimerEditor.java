package com.dennyy.oldschoolcompanion.customviews;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dennyy.oldschoolcompanion.R;
import com.dennyy.oldschoolcompanion.helpers.Utils;
import com.dennyy.oldschoolcompanion.interfaces.TimerEditorListener;
import com.dennyy.oldschoolcompanion.models.Timers.Timer;


public class TimerEditor extends LinearLayout implements View.OnClickListener {

    private TextView titleTextView;
    private TextView descriptionTextView;
    private EditText hoursEditText;
    private EditText minutesEditText;
    private EditText secondsEditText;
    private CheckBox repeatedCheckbox;
    private TextView editTimerActive;
    private TextView timerNotifyText;
    private TimerEditorListener listener;

    public TimerEditor(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.timer_editor, this);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        findViewById(R.id.timer_save).setOnClickListener(this);
        findViewById(R.id.timer_cancel).setOnClickListener(this);
        findViewById(R.id.timer_repeat_checkbox_container).setOnClickListener(this);
        titleTextView = findViewById(R.id.timer_editor_title_edittext);
        descriptionTextView = findViewById(R.id.timer_editor_desc_exittext);
        hoursEditText = findViewById(R.id.timer_hours);
        minutesEditText = findViewById(R.id.timer_minutes);
        secondsEditText = findViewById(R.id.timer_seconds);
        repeatedCheckbox = findViewById(R.id.timer_repeat_checkbox);
        editTimerActive = findViewById(R.id.timer_edit_active);
        timerNotifyText = findViewById(R.id.timer_notify_text);
    }

    public void setListener(TimerEditorListener listener) {
        this.listener = listener;
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.timer_repeat_checkbox_container) {
            repeatedCheckbox.setChecked(!repeatedCheckbox.isChecked());
            timerNotifyText.setText(getResources().getString(repeatedCheckbox.isChecked() ? R.string.timer_notify_every : R.string.timer_notify_after));
        }
        if (listener == null)
            return;
        if (id == R.id.timer_save) {
            listener.onTimerEditorSave(
                    titleTextView.getText().toString(),
                    descriptionTextView.getText().toString(),
                    Utils.tryParseInt(hoursEditText.getText().toString(), 0),
                    Utils.tryParseInt(minutesEditText.getText().toString(), 0),
                    Utils.tryParseInt(secondsEditText.getText().toString(), 0),
                    repeatedCheckbox.isChecked());
        }
        else if (id == R.id.timer_cancel) {
            clear();
            listener.onTimerEditorCancel();
        }
    }

    public void clear() {
        setTag(null);
        titleTextView.setText(null);
        descriptionTextView.setText(null);
        hoursEditText.setText(null);
        minutesEditText.setText(null);
        secondsEditText.setText(null);
        repeatedCheckbox.setChecked(false);
    }

    public void setContent(Timer timer) {
        int hours = timer.interval / 3600;
        int secondsLeft = timer.interval - hours * 3600;
        int minutes = secondsLeft / 60;
        int seconds = secondsLeft - minutes * 60;
        setTag(timer.id);
        titleTextView.setText(timer.title);
        descriptionTextView.setText(timer.description);
        hoursEditText.setText(String.valueOf(hours));
        minutesEditText.setText(String.valueOf(minutes));
        secondsEditText.setText(String.valueOf(seconds));
        repeatedCheckbox.setChecked(timer.isRepeating);
        editTimerActive.setVisibility(timer.isActive() ? VISIBLE : GONE);
        timerNotifyText.setText(getResources().getString(repeatedCheckbox.isChecked() ? R.string.timer_notify_every : R.string.timer_notify_after));
    }
}
