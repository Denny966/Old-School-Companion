package com.dennyy.oldschoolcompanion.asynctasks;

import android.content.Context;
import android.os.AsyncTask;
import com.dennyy.oldschoolcompanion.database.AppDb;
import com.dennyy.oldschoolcompanion.interfaces.NpcListeners;

import java.lang.ref.WeakReference;

public abstract class NpcAsyncTasks {

    public static class InsertNpcData extends AsyncTask<Void, Void, Void> {
        private WeakReference<Context> weakContext;
        private NpcListeners.UpdateListener callback;
        private String npcName;
        private String npcData;

        public InsertNpcData(final Context context, String npcName, String npcData, NpcListeners.UpdateListener callback) {
            this.weakContext = new WeakReference<>(context);
            this.npcName = npcName;
            this.npcData = npcData;
            this.callback = callback;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            Context context = weakContext.get();
            if (context != null) {
                AppDb.getInstance(context).insertOrUpdateNpc(npcName, npcData);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (callback == null) return;
            callback.onActionFinished();
        }
    }

    public static class GetNpcData extends AsyncTask<Void, Void, String> {
        private WeakReference<Context> weakContext;
        private NpcListeners.NpcLoadedListener callback;
        private String npcName;

        public GetNpcData(final Context context, String npcName, NpcListeners.NpcLoadedListener callback) {
            this.weakContext = new WeakReference<>(context);
            this.npcName = npcName;
            this.callback = callback;
        }

        @Override
        protected String doInBackground(Void... voids) {
            Context context = weakContext.get();
            String data = null;
            if (context != null) {
                data = AppDb.getInstance(context).getNpcData(npcName);
            }
            return data;
        }

        @Override
        protected void onPostExecute(String data) {
            if (data == null) {
                callback.onNpcLoadFailed(npcName);
            }
            else {
                callback.onNpcLoaded(npcName, data);
            }
        }
    }

    public static class DeleteMonsterData extends AsyncTask<Void, Void, Void> {
        private WeakReference<Context> weakContext;
        private String npcName;

        public DeleteMonsterData(final Context context, String npcName) {
            this.weakContext = new WeakReference<>(context);
            this.npcName = npcName;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            Context context = weakContext.get();
            if (context != null) {
                AppDb.getInstance(context).deleteMonsterData(npcName);
            }
            return null;
        }
    }
}