package com.dennyy.oldschoolcompanion.viewhandlers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.view.View;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Toast;

import com.dennyy.oldschoolcompanion.R;
import com.dennyy.oldschoolcompanion.adapters.TimersAdapter;
import com.dennyy.oldschoolcompanion.asynctasks.GetTimersTask;
import com.dennyy.oldschoolcompanion.broadcastreceivers.CancelTimerReceiver;
import com.dennyy.oldschoolcompanion.broadcastreceivers.TimerReceiver;
import com.dennyy.oldschoolcompanion.customviews.TimerEditor;
import com.dennyy.oldschoolcompanion.database.AppDb;
import com.dennyy.oldschoolcompanion.enums.ReloadTimerSource;
import com.dennyy.oldschoolcompanion.helpers.Logger;
import com.dennyy.oldschoolcompanion.helpers.TimerIntentHelper;
import com.dennyy.oldschoolcompanion.helpers.Utils;
import com.dennyy.oldschoolcompanion.interfaces.AdapterTimerClickListener;
import com.dennyy.oldschoolcompanion.interfaces.TimerEditorListener;
import com.dennyy.oldschoolcompanion.interfaces.TimersLoadedListener;
import com.dennyy.oldschoolcompanion.models.Timers.ReloadTimersEvent;
import com.dennyy.oldschoolcompanion.models.Timers.Timer;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;

import static android.app.PendingIntent.FLAG_ONE_SHOT;

public class TimersViewHandler extends BaseViewHandler implements TimerEditorListener, TimersLoadedListener, AdapterTimerClickListener, View.OnClickListener {

    private ListView timersListView;
    private TimersAdapter timersAdapter;
    private TimerEditor timerEditor;
    private ScrollView timerEditorScrollView;
    private ReloadTimerSource reloadTimerSource;

    public TimersViewHandler(Context context, View view, boolean isFloatingView) {
        super(context, view);

        timersListView = view.findViewById(R.id.timers_listview);
        reloadTimerSource = isFloatingView ? ReloadTimerSource.FLOATINTG_VIEW : ReloadTimerSource.FRAGMENT;
        if (isFloatingView) {
            view.findViewById(R.id.timers_navbar).setVisibility(View.VISIBLE);
            view.findViewById(R.id.add_timer).setOnClickListener(this);
        }
        initEditor();
        reloadTimers();
    }

    private void initEditor() {
        timerEditor = view.findViewById(R.id.timer_editor);
        timerEditorScrollView = view.findViewById(R.id.timer_editor_scrollview);
        timerEditor.setListener(this);
    }

    public void openAddTimerView() {
        timersListView.setVisibility(View.GONE);
        timerEditorScrollView.setVisibility(View.VISIBLE);
    }

    public void reloadTimers() {
        new GetTimersTask(context, this).execute();
    }

    @Override
    public void onTimersLoaded(ArrayList<Timer> timers) {
        if (timersAdapter == null) {
            timersAdapter = new TimersAdapter(context, timers, this);
            timersListView.setAdapter(timersAdapter);
        }
        else {
            timersAdapter.updateList(timers);
        }
    }

    @Override
    public void onTimersLoadFailed() {
        showToast(getString(R.string.unexpected_error_try_reopen), Toast.LENGTH_LONG);
    }

    @Override
    public void onTimerEditorSave(String title, String description, int hours, int minutes, int seconds, boolean repeated) {
        int total = hours + minutes + seconds;
        if (total < 1) {
            showToast(getString(R.string.timer_invalid_interval), Toast.LENGTH_SHORT);
            return;
        }
        else if (Utils.isNullOrEmpty(title)) {
            showToast(getString(R.string.timer_empty_title), Toast.LENGTH_SHORT);
            return;
        }
        Timer timer = new Timer();
        timer.id = timerEditor.getTag() == null ? 0 : (int) timerEditor.getTag();
        timer.title = title;
        timer.description = description;
        timer.interval = hours * 3600 + minutes * 60 + seconds;
        timer.isRepeating = repeated;
        AppDb.getInstance(context).insertOrUpdateTimer(timer);
        EventBus.getDefault().post(new ReloadTimersEvent(ReloadTimerSource.ANY));
        onTimerEditorCancel();
        hideKeyboard();
    }

    @Override
    public void onTimerEditorCancel() {
        timerEditor.clear();
        timerEditorScrollView.setVisibility(View.GONE);
        timersListView.setVisibility(View.VISIBLE);
    }

    public boolean isTimerEditorOpen() {
        return timerEditorScrollView.getVisibility() == View.VISIBLE;
    }

    @Override
    public void cancelRunningTasks() {

    }

    @Override
    public boolean wasRequesting() {
        return false;
    }

    @Override
    public void onStartClick(Timer timer) {
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (timersAdapter == null || manager == null) {
            showToast(getString(R.string.unexpected_error_try_reopen), Toast.LENGTH_SHORT);
            return;
        }
        PendingIntent pendingIntent = TimerIntentHelper.CreateIntent(context, timer, TimerReceiver.class, PendingIntent.FLAG_UPDATE_CURRENT);
        if (timer.isActive()) {
            cancelTimer(timer);
        }
        else {
            manager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + timer.getDelayMs(), pendingIntent);
        }

        timer.setActive(!timer.isActive());
        timersAdapter.notifyDataSetChanged();
        EventBus.getDefault().post(new ReloadTimersEvent(reloadTimerSource));
    }

    @Override
    public void onEditClick(Timer timer) {
        if (timersAdapter == null) {
            showToast(getString(R.string.unexpected_error_try_reopen), Toast.LENGTH_SHORT);
            return;
        }
        timerEditor.setContent(timer);
        openAddTimerView();
    }

    @Override
    public void onConfirmDeleteClick(Timer timer) {
        cancelTimer(timer);
        AppDb.getInstance(context).deleteTimer(timer.id);
        EventBus.getDefault().post(new ReloadTimersEvent(reloadTimerSource));
    }

    private void cancelTimer(Timer timer) {
        PendingIntent cancelPendingIntent = TimerIntentHelper.CreateIntent(context, timer, CancelTimerReceiver.class, FLAG_ONE_SHOT);
        try {
            cancelPendingIntent.send();
        }
        catch (PendingIntent.CanceledException e) {
            Logger.log(e);
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.add_timer) {
            openAddTimerView();
        }
    }
}