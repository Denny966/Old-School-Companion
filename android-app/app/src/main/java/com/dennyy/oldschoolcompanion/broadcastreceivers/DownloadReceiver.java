package com.dennyy.oldschoolcompanion.broadcastreceivers;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.dennyy.oldschoolcompanion.BuildConfig;
import com.dennyy.oldschoolcompanion.R;
import com.dennyy.oldschoolcompanion.helpers.Constants;
import com.dennyy.oldschoolcompanion.models.Worldmap.WorldmapDownloadedEvent;

import org.greenrobot.eventbus.EventBus;

public class DownloadReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        long worldmapId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (prefs.getLong(Constants.WORLDMAP_DOWNLOAD_KEY, -1) == worldmapId) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, BuildConfig.APPLICATION_ID)
                    .setSmallIcon(R.drawable.download)
                    .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.oldschoolcompanion))
                    .setAutoCancel(true)
                    .setContentTitle(context.getResources().getString(R.string.worldmap))
                    .setContentText(context.getResources().getString(R.string.download_worldmap_complete))
                    .setTicker(context.getResources().getString(R.string.download_worldmap_complete))
                    .setDefaults(NotificationCompat.DEFAULT_ALL)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.notify(Constants.WORLDMAP_NOTIFICATION_ID, builder.build());
            SharedPreferences.Editor editor = prefs.edit();
            editor.remove(Constants.WORLDMAP_DOWNLOAD_KEY);
            editor.apply();
            EventBus.getDefault().post(new WorldmapDownloadedEvent());
        }
    }
}
