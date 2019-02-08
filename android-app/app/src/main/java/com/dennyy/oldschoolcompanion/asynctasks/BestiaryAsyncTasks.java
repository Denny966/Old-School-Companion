package com.dennyy.oldschoolcompanion.asynctasks;

import android.content.Context;
import android.os.AsyncTask;
import com.dennyy.oldschoolcompanion.database.AppDb;
import com.dennyy.oldschoolcompanion.interfaces.BestiaryListeners;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public abstract class BestiaryAsyncTasks {
    public static class GetHistory extends AsyncTask<Void, Void, ArrayList<String>> {
        private WeakReference<Context> weakContext;
        private BestiaryListeners.GetHistoryListener callback;

        public GetHistory(final Context context, BestiaryListeners.GetHistoryListener callback) {
            this.weakContext = new WeakReference<>(context);
            this.callback = callback;
        }

        @Override
        protected ArrayList<String> doInBackground(Void... voids) {
            Context context = weakContext.get();
            ArrayList<String> bestiaryHistory = null;
            if (context != null) {
                bestiaryHistory = AppDb.getInstance(context).getBestiaryHistory();
            }
            return bestiaryHistory;
        }

        @Override
        protected void onPostExecute(ArrayList<String> bestiaryHistory) {
            if (bestiaryHistory != null) {
                callback.onBestiaryHistoryLoaded(bestiaryHistory);
            }
        }
    }

    public static class InsertHistory extends AsyncTask<Void, Void, ArrayList<String>> {
        private WeakReference<Context> weakContext;
        private String monsterName;
        private BestiaryListeners.GetHistoryListener callback;

        public InsertHistory(final Context context, String monsterName, BestiaryListeners.GetHistoryListener callback) {
            this.weakContext = new WeakReference<>(context);
            this.monsterName = monsterName;
            this.callback = callback;
        }

        @Override
        protected ArrayList<String> doInBackground(Void... voids) {
            Context context = weakContext.get();
            ArrayList<String> bestiaryHistory = null;
            if (context != null) {
                AppDb.getInstance(context).insertBestiaryHistory(monsterName);
                bestiaryHistory = AppDb.getInstance(context).getBestiaryHistory();
            }
            return bestiaryHistory;
        }

        @Override
        protected void onPostExecute(ArrayList<String> bestiaryHistory) {
            if (bestiaryHistory != null) {
                callback.onBestiaryHistoryLoaded(bestiaryHistory);
            }
        }
    }

    public static class ClearHistory extends AsyncTask<Void, Void, Void> {
        private WeakReference<Context> weakContext;

        public ClearHistory(final Context context) {
            this.weakContext = new WeakReference<>(context);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            Context context = weakContext.get();

            if (context != null) {
                AppDb.getInstance(context).clearBestiaryHistory();
            }
            return null;
        }
    }
}
