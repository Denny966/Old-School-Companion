package com.dennyy.oldschoolcompanion.broadcastreceivers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.dennyy.oldschoolcompanion.BuildConfig;
import com.dennyy.oldschoolcompanion.R;
import com.dennyy.oldschoolcompanion.helpers.Logger;
import com.dennyy.oldschoolcompanion.helpers.TimerIntentHelper;
import com.dennyy.oldschoolcompanion.helpers.Utils;
import com.dennyy.oldschoolcompanion.models.Timers.Timer;

import static android.app.PendingIntent.FLAG_ONE_SHOT;

public class TimerReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Timer timer = TimerIntentHelper.GetTimerFromExtras(intent.getExtras());
        if (timer == null) {
            return;
        }
        PendingIntent cancelPendingIntent = TimerIntentHelper.CreateIntent(context, timer, CancelTimerReceiver.class, FLAG_ONE_SHOT);
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, BuildConfig.APPLICATION_ID)
                .setSmallIcon(R.drawable.baseline_timer_white_24dp)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.oldschoolcompanion))
                .setContentTitle(timer.title)
                .setTicker(Utils.isNullOrEmpty(timer.description) ? timer.title : timer.description)
                .setContentText(timer.description)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        if (timer.isRepeating && manager != null) {
            builder.addAction(0, context.getString(R.string.timer_disable), cancelPendingIntent);
            PendingIntent pendingIntent = TimerIntentHelper.CreateIntent(context, timer, TimerReceiver.class, PendingIntent.FLAG_UPDATE_CURRENT);
            manager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + timer.getDelayMs(), pendingIntent);
        }
        else {
            PendingIntent restartPendingIntent = TimerIntentHelper.CreateIntent(context, timer, RestartTimerReceiver.class, FLAG_ONE_SHOT);
            builder.addAction(0, context.getString(R.string.timer_restart), restartPendingIntent);
            try {
                cancelPendingIntent.send();
            }
            catch (PendingIntent.CanceledException e) {
                Logger.log(e);
            }
        }
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(timer.id, builder.build());
    }
}