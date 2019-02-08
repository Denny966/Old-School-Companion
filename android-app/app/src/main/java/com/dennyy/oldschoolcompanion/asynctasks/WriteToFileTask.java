package com.dennyy.oldschoolcompanion.asynctasks;

import android.content.Context;
import android.os.AsyncTask;

import com.dennyy.oldschoolcompanion.helpers.Utils;

import java.lang.ref.WeakReference;

public class WriteToFileTask extends AsyncTask<Void, Void, Void> {
    private WeakReference<Context> weakContext;
    private String fileName;
    private String content;

    public WriteToFileTask(final Context context, String fileName, String content) {
        this.weakContext = new WeakReference<>(context);
        this.fileName = fileName;
        this.content = content;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        Context context = weakContext.get();
        if (context != null) {
            Utils.writeToFile(context, fileName, content);
        }
        return null;
    }
}