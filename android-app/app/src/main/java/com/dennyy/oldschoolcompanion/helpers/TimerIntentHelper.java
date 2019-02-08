package com.dennyy.oldschoolcompanion.helpers;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.dennyy.oldschoolcompanion.models.Timers.Timer;

public class TimerIntentHelper {

    private static final String ID_TAG = "timer_id";
    private static final String TITLE_TAG = "timer_title";
    private static final String DESC_TAG = "timer_desc";
    private static final String INTERVAL_TAG = "timer_interval";
    private static final String REPEATING_TAG = "timer_repeating";

    public static PendingIntent CreateIntent(Context context, Timer timer, Class receiverName, int flag) {
        Intent intent = new Intent(context, receiverName);
        intent.putExtra(ID_TAG, timer.id);
        intent.putExtra(TITLE_TAG, timer.title);
        intent.putExtra(DESC_TAG, timer.description);
        intent.putExtra(INTERVAL_TAG, timer.interval);
        intent.putExtra(REPEATING_TAG, timer.isRepeating);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, timer.id, intent, flag);
        return pendingIntent;
    }

    public static Timer GetTimerFromExtras(Bundle extras) {
        if (extras == null) {
            return null;
        }
        int id = extras.getInt(ID_TAG, -1);
        if (id == -1) {
            return null;
        }
        Timer timer = new Timer();
        timer.id = id;
        timer.title = extras.getString(TITLE_TAG);
        timer.description = extras.getString(DESC_TAG);
        timer.interval = extras.getInt(INTERVAL_TAG);
        timer.isRepeating = extras.getBoolean(REPEATING_TAG);
        return timer;
    }
}