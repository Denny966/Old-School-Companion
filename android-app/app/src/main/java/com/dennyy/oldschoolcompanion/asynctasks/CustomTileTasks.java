package com.dennyy.oldschoolcompanion.asynctasks;

import android.content.Context;
import android.os.AsyncTask;
import com.dennyy.oldschoolcompanion.database.AppDb;
import com.dennyy.oldschoolcompanion.interfaces.CustomTileListeners;
import com.dennyy.oldschoolcompanion.models.CustomTile.CustomTile;

import java.lang.ref.WeakReference;
import java.util.List;

public abstract class CustomTileTasks {

    public static class InsertOrUpdate extends AsyncTask<Void, Void, List<CustomTile>> {
        private WeakReference<Context> weakContext;
        private long id;
        private String name;
        private int sortOrder;
        private String url;
        private CustomTileListeners.CustomTileListener callback;

        public InsertOrUpdate(final Context context, long id, String name, int sortOrder, String url, CustomTileListeners.CustomTileListener callback) {
            this.weakContext = new WeakReference<>(context);
            this.id = id;
            this.name = name;
            this.sortOrder = sortOrder;
            this.url = url;
            this.callback = callback;
        }

        @Override
        protected List<CustomTile> doInBackground(Void... voids) {
            Context context = weakContext.get();
            List<CustomTile> tiles = null;
            if (context != null) {
                AppDb.getInstance(context).insertOrUpdateCustomTile(id, name, sortOrder, url);
                tiles = AppDb.getInstance(context).getCustomTiles();
            }
            return tiles;
        }

        @Override
        protected void onPostExecute(List<CustomTile> tiles) {
            if (callback == null) return;
            if (tiles == null) {
                callback.onCustomTilesLoadFailed();
            }
            else {
                callback.onCustomTilesLoaded(tiles);
            }
        }
    }

    public static class Delete extends AsyncTask<Void, Void, List<CustomTile>> {
        private WeakReference<Context> weakContext;
        private long id;
        private CustomTileListeners.CustomTileListener callback;

        public Delete(final Context context, long id, CustomTileListeners.CustomTileListener callback) {
            this.weakContext = new WeakReference<>(context);
            this.id = id;
            this.callback = callback;
        }

        @Override
        protected List<CustomTile> doInBackground(Void... voids) {
            Context context = weakContext.get();
            List<CustomTile> tiles = null;
            if (context != null) {
                AppDb.getInstance(context).deleteCustomTile(id);
                tiles = AppDb.getInstance(context).getCustomTiles();
            }
            return tiles;
        }

        @Override
        protected void onPostExecute(List<CustomTile> tiles) {
            if (callback == null) return;
            if (tiles == null) {
                callback.onCustomTilesLoadFailed();
            }
            else {
                callback.onCustomTilesLoaded(tiles);
            }
        }
    }

    public static class Get extends AsyncTask<Void, Void, List<CustomTile>> {
        private WeakReference<Context> weakContext;
        private CustomTileListeners.CustomTileListener callback;

        public Get(final Context context, CustomTileListeners.CustomTileListener callback) {
            this.weakContext = new WeakReference<>(context);
            this.callback = callback;
        }

        @Override
        protected List<CustomTile> doInBackground(Void... voids) {
            Context context = weakContext.get();
            List<CustomTile> tiles = null;
            if (context != null) {
                tiles = AppDb.getInstance(context).getCustomTiles();
            }
            return tiles;
        }

        @Override
        protected void onPostExecute(List<CustomTile> tiles) {
            if (callback == null) return;
            if (tiles == null) {
                callback.onCustomTilesLoadFailed();
                callback.always();
            }
            else {
                callback.onCustomTilesLoaded(tiles);
                callback.always();
            }
        }
    }
}