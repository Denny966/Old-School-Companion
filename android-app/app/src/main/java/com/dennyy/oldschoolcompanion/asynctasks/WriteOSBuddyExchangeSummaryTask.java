package com.dennyy.oldschoolcompanion.asynctasks;

import android.content.Context;
import android.os.AsyncTask;

import com.dennyy.oldschoolcompanion.database.AppDb;
import com.dennyy.oldschoolcompanion.helpers.Utils;

import java.lang.ref.WeakReference;

public class WriteOSBuddyExchangeSummaryTask extends AsyncTask<Void, Void, Void> {
    private WeakReference<Context> weakContext;
    private String content;

    public WriteOSBuddyExchangeSummaryTask(final Context context, String content) {
        this.weakContext = new WeakReference<>(context);
        this.content = content;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        Context context = weakContext.get();
        if (context != null && !Utils.isNullOrEmpty(content)) {
            AppDb.getInstance(context).insertOrUpdateOSBuddyExchangeSummaryData(content);
        }
        return null;
    }
}
