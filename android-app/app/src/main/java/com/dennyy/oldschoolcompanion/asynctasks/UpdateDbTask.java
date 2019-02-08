package com.dennyy.oldschoolcompanion.asynctasks;

import android.content.Context;
import android.os.AsyncTask;

import com.dennyy.oldschoolcompanion.database.AppDb;
import com.dennyy.oldschoolcompanion.helpers.Logger;

import java.lang.ref.WeakReference;

// ensure database is updated when using in the app, https://stackoverflow.com/questions/3163845/is-the-onupgrade-method-ever-called
public class UpdateDbTask extends AsyncTask<Void, Void, Void> {
    private WeakReference<Context> weakContext;

    public UpdateDbTask(final Context context) {
        this.weakContext = new WeakReference<>(context);
    }

    @Override
    protected Void doInBackground(Void... voids) {
        Context context = weakContext.get();
        if (context != null) {
            try {
                AppDb.getInstance(context).getWritableDatabase();
            }
            catch (Exception ex) {
                Logger.log(ex);
            }
        }
        return null;
    }
}