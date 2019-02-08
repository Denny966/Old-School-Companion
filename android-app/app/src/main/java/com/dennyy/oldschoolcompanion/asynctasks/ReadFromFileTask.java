package com.dennyy.oldschoolcompanion.asynctasks;

import android.content.Context;
import android.os.AsyncTask;

import com.dennyy.oldschoolcompanion.helpers.Utils;
import com.dennyy.oldschoolcompanion.interfaces.ContentLoadedListener;

import java.lang.ref.WeakReference;

public class ReadFromFileTask extends AsyncTask<Void, Void, String> {
    private WeakReference<Context> weakContext;
    private ContentLoadedListener callback;
    private String fileName;

    public ReadFromFileTask(final Context context, String fileName, final ContentLoadedListener callback) {
        this.weakContext = new WeakReference<>(context);
        this.callback = callback;
        this.fileName = fileName;
    }

    @Override
    protected String doInBackground(Void... voids) {
        String content = "";
        Context context = weakContext.get();
        if (context != null) {
            content = Utils.readFromFile(context, fileName);
        }
        return content;
    }

    @Override
    protected void onPostExecute(String content) {
        callback.onContentLoaded(content);
    }
}
