package com.dennyy.oldschoolcompanion.asynctasks;

import android.content.Context;
import android.os.AsyncTask;

import com.dennyy.oldschoolcompanion.database.AppDb;
import com.dennyy.oldschoolcompanion.interfaces.OSRSNewsLoadedListener;
import com.dennyy.oldschoolcompanion.models.OSRSNews.OSRSNewsDTO;

import java.lang.ref.WeakReference;

public class OSRSNewsTask extends AsyncTask<Void, Void, Void> {
    private WeakReference<Context> context;
    private WeakReference<OSRSNewsLoadedListener> callback;
    private OSRSNewsDTO osrsNewsDTO;

    public OSRSNewsTask(final Context context, final OSRSNewsLoadedListener callback) {
        this.context = new WeakReference<>(context);
        this.callback = new WeakReference<>(callback);
    }

    @Override
    protected Void doInBackground(Void... voids) {
        osrsNewsDTO = AppDb.getInstance(context.get()).getOSRSNews();
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        if (callback.get() != null) {
            callback.get().onOSRSNewsLoaded(osrsNewsDTO);
        }
    }
}
