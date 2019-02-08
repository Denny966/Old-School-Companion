package com.dennyy.oldschoolcompanion.asynctasks;

import android.content.Context;
import android.os.AsyncTask;
import com.android.volley.VolleyError;
import com.dennyy.oldschoolcompanion.database.AppDb;
import com.dennyy.oldschoolcompanion.helpers.Constants;
import com.dennyy.oldschoolcompanion.helpers.GeHelper;
import com.dennyy.oldschoolcompanion.helpers.Logger;
import com.dennyy.oldschoolcompanion.helpers.Utils;
import com.dennyy.oldschoolcompanion.interfaces.GeListeners;
import com.dennyy.oldschoolcompanion.models.GrandExchange.*;
import com.dennyy.oldschoolcompanion.models.OSBuddy.OSBuddySummary;
import com.dennyy.oldschoolcompanion.models.OSBuddy.OSBuddySummaryItem;
import org.json.JSONException;

import java.lang.ref.WeakReference;
import java.util.HashMap;

public abstract class GeAsyncTasks {

    public static class InsertOrUpdateGeData extends AsyncTask<Void, Void, Void> {
        private WeakReference<Context> weakContext;
        private String itemId;
        private String itemData;

        public InsertOrUpdateGeData(final Context context, String itemId, String itemData) {
            this.weakContext = new WeakReference<>(context);
            this.itemId = itemId;
            this.itemData = itemData;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            Context context = weakContext.get();
            if (context != null) {
                AppDb.getInstance(context).insertOrUpdateGrandExchangeData(itemId, itemData);
            }
            return null;
        }
    }

    public static class InsertOrUpdateGeHistory extends AsyncTask<Void, Void, GeHistory> {
        private WeakReference<Context> weakContext;
        private GeListeners.GeHistoryLoadedListener callback;
        private String itemId;
        private String itemName;
        private boolean isFavorite;

        public InsertOrUpdateGeHistory(final Context context, String itemId, String itemName, boolean isFavorite, GeListeners.GeHistoryLoadedListener callback) {
            this.weakContext = new WeakReference<>(context);
            this.itemId = itemId;
            this.itemName = itemName;
            this.isFavorite = isFavorite;
            this.callback = callback;
        }

        @Override
        protected GeHistory doInBackground(Void... voids) {
            Context context = weakContext.get();
            GeHistory geHistory = null;
            if (context != null) {
                AppDb.getInstance(context).insertOrUpdateGeHistory(itemId, itemName, isFavorite);
                geHistory = AppDb.getInstance(context).getGeHistory();
            }
            return geHistory;
        }

        @Override
        protected void onPostExecute(GeHistory geHistory) {
            if (callback == null) return;
            if (geHistory == null) {
                callback.onGeHistoryLoadFailed();
            }
            else {
                callback.onGeHistoryLoaded(geHistory);
            }
        }
    }

    public static class GetHistory extends AsyncTask<Void, Void, GeHistory> {
        private WeakReference<Context> weakContext;
        private GeListeners.GeHistoryLoadedListener callback;
        private boolean clearHistory;

        public GetHistory(final Context context, boolean clearHistory, GeListeners.GeHistoryLoadedListener callback) {
            this.weakContext = new WeakReference<>(context);
            this.clearHistory = clearHistory;
            this.callback = callback;
        }

        @Override
        protected GeHistory doInBackground(Void... voids) {
            Context context = weakContext.get();
            GeHistory geHistory = null;
            if (context != null) {
                geHistory = AppDb.getInstance(context).getGeHistory(clearHistory);
            }
            return geHistory;
        }

        @Override
        protected void onPostExecute(GeHistory geHistory) {
            if (geHistory == null) {
                callback.onGeHistoryLoadFailed();
            }
            else {
                callback.onGeHistoryLoaded(geHistory);
            }
        }
    }

    public static class GetItemData extends AsyncTask<Void, Void, GrandExchangeData> {
        public static final String GRAND_EXCHANGE_REQUEST_TAG = "grand_exchange_request_tag";

        private WeakReference<Context> weakContext;
        private GeListeners.ItemDataLoadedListener callback;
        private String itemId;
        private boolean forceCacheReload;

        public GetItemData(final Context context, String itemId, GeListeners.ItemDataLoadedListener callback) {
            this(context, itemId, false, callback);
        }

        public GetItemData(final Context context, String itemId, boolean forceCacheReload, GeListeners.ItemDataLoadedListener callback) {
            this.weakContext = new WeakReference<>(context);
            this.itemId = itemId;
            this.forceCacheReload = forceCacheReload;
            this.callback = callback;
        }

        @Override
        protected GrandExchangeData doInBackground(Void... voids) {
            Context context = weakContext.get();
            if (context == null) {
                return null;
            }
            GrandExchangeData geData = AppDb.getInstance(context).getGrandExchangeData(itemId);
            return geData == null ? new GrandExchangeData() : geData;
        }

        @Override
        protected void onPostExecute(final GrandExchangeData data) {
            final Context context = weakContext.get();
            if (context == null || data == null) {
                callback.onGeItemDataContextError();
                return;
            }
            final boolean cacheExpired = Math.abs(System.currentTimeMillis() - data.dateModified) > Constants.GE_CACHE_DURATION;
            if (!data.hasData() || cacheExpired || forceCacheReload) {
                Utils.getString(Constants.GE_ITEM_URL + itemId, GRAND_EXCHANGE_REQUEST_TAG, new Utils.VolleyCallback() {
                    @Override
                    public void onSuccess(String result) {
                        data.itemId = itemId;
                        data.data = result;
                        new GeAsyncTasks.InsertOrUpdateGeData(context, itemId, result).execute();
                        callback.onGeItemDataLoaded(data, cacheExpired);
                    }

                    @Override
                    public void onError(VolleyError error) {
                        if (data.hasData()) {
                            callback.onGeItemDataLoaded(data, cacheExpired);
                        }
                        else {
                            callback.onGeItemDataLoadFailed();
                        }
                    }

                    @Override
                    public void always() {
                    }
                });
            }
            else {
                callback.onGeItemDataLoaded(data, false);
            }
        }
    }

    public static class GetCompleteItemData extends AsyncTask<Void, Void, ItemData> {
        private WeakReference<Context> weakContext;
        private GeListeners.CompleteItemDataLoadedListener callback;
        private String itemId;

        public GetCompleteItemData(final Context context, String itemId, GeListeners.CompleteItemDataLoadedListener callback) {
            this.weakContext = new WeakReference<>(context);
            this.itemId = itemId;
            this.callback = callback;
        }

        @Override
        protected ItemData doInBackground(Void... voids) {
            Context context = weakContext.get();
            ItemData itemData = null;
            if (context != null) {
                GrandExchangeData geData = AppDb.getInstance(context).getGrandExchangeData(itemId);
                GrandExchangeGraphData graphData = AppDb.getInstance(context).getGrandExchangeGraphData(itemId);
                GrandExchangeUpdateData geUpdateData = AppDb.getInstance(context).getGrandExchangeUpdateData();
                HashMap<String, OSBuddySummaryItem> summaryMap = new HashMap<>();
                OSBuddySummary summary = AppDb.getInstance(context).getOSBuddyExchangeSummary();
                if (summary != null) {
                    try {
                        summaryMap = GetOSBuddyExchangeSummaryTask.parseOSBuddySummary(summary.data);
                    }
                    catch (JSONException ex) {
                        Logger.log(ex, "failed to restore osbuddysummary from savedinstancestate", summary.data);
                    }
                }
                itemData = new ItemData(geData, graphData, geUpdateData, summaryMap);
            }
            return itemData;
        }

        @Override
        protected void onPostExecute(ItemData data) {
            if (data == null) {
                callback.onItemDataLoadFailed();
            }
            else {
                callback.onItemDataLoaded(data);
            }
        }
    }

    public static class GetGeUpdate extends AsyncTask<Void, Void, GrandExchangeUpdateData> {

        public static final String GEUPDATE_REQUEST_TAG = "grandexchangeupdaterequest";
        private WeakReference<Context> weakContext;
        private GeListeners.GeUpdateLoadedListener callback;
        private boolean forceCacheReload;

        public GetGeUpdate(final Context context, GeListeners.GeUpdateLoadedListener callback) {
            this(context, false, callback);
        }

        public GetGeUpdate(final Context context, boolean forceCacheReload, GeListeners.GeUpdateLoadedListener callback) {
            this.weakContext = new WeakReference<>(context);
            this.forceCacheReload = forceCacheReload;
            this.callback = callback;
        }

        @Override
        protected GrandExchangeUpdateData doInBackground(Void... voids) {
            Context context = weakContext.get();
            if (context == null) {
                return null;
            }
            GrandExchangeUpdateData geUpdateData = AppDb.getInstance(context).getGrandExchangeUpdateData();

            return geUpdateData == null ? new GrandExchangeUpdateData() : geUpdateData;
        }

        @Override
        protected void onPostExecute(final GrandExchangeUpdateData data) {
            final Context context = weakContext.get();
            if (context == null || data == null) {
                callback.onGeUpdateContextError();
                return;
            }

            final boolean cacheExpired = Math.abs(System.currentTimeMillis() - data.dateModified) > Constants.GE_UPDATE_CACHE_DURATION;
            if (!data.hasData() || cacheExpired || forceCacheReload) {
                Utils.getString(Constants.GE_UPDATE_URL, GEUPDATE_REQUEST_TAG, new Utils.VolleyCallback() {
                    @Override
                    public void onSuccess(String result) {
                        data.data = result;
                        new GeAsyncTasks.InsertGeUpdate(context, result).execute();
                        callback.onGeUpdateLoaded(data, cacheExpired);
                    }

                    @Override
                    public void onError(VolleyError error) {
                        if (data.hasData()) {
                            callback.onGeUpdateLoaded(data, cacheExpired);
                        }
                        else {
                            callback.onGeUpdateLoadFailed();
                        }
                    }

                    @Override
                    public void always() {
                    }
                });
            }
            else {
                callback.onGeUpdateLoaded(data, false);
            }
        }
    }

    public static class InsertGeUpdate extends AsyncTask<Void, Void, Void> {
        private WeakReference<Context> weakContext;
        private String geUpdateData;

        public InsertGeUpdate(final Context context, String geUpdateData) {
            this.weakContext = new WeakReference<>(context);
            this.geUpdateData = geUpdateData;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            Context context = weakContext.get();
            if (context != null) {
                AppDb.getInstance(context).updateGrandExchangeUpdateData(geUpdateData);
            }
            return null;
        }
    }

    public static class GetGeLimits extends AsyncTask<String, Void, HashMap<String, Integer>> {
        private WeakReference<Context> weakContext;

        public GetGeLimits(Context context) {
            this.weakContext = new WeakReference<>(context);
        }

        @Override
        protected HashMap<String, Integer> doInBackground(String... params) {
            HashMap<String, Integer> allItems = new HashMap<>();
            Context context = weakContext.get();
            if (context == null) {
                return allItems;
            }
            try {
                allItems = GeHelper.getItemLimits(context);
            }
            catch (JSONException ex) {
                Logger.log(ex);
            }
            return allItems;
        }

        @Override
        protected void onPostExecute(HashMap<String, Integer> items) {
            GeHelper.BUY_LIMITS.putAll(items);
        }
    }

    public static class GetGeGraphData extends AsyncTask<Void, Void, GrandExchangeGraphData> {
        public static final String GEGRAPH_REQUEST_TAG = "grandexchangegraphrequest";

        private WeakReference<Context> weakContext;
        private GeListeners.GraphDataLoadedListener callback;
        private String itemId;
        private boolean forceCacheReload;

        public GetGeGraphData(final Context context, String itemId, GeListeners.GraphDataLoadedListener callback) {
            this(context, itemId, false, callback);
        }

        public GetGeGraphData(final Context context, String itemId, boolean forceCacheReload, GeListeners.GraphDataLoadedListener callback) {
            this.weakContext = new WeakReference<>(context);
            this.callback = callback;
            this.itemId = itemId;
            this.forceCacheReload = forceCacheReload;
        }

        @Override
        protected GrandExchangeGraphData doInBackground(Void... voids) {
            Context context = weakContext.get();
            if (context == null) {
                return null;
            }
            GrandExchangeGraphData graphData = AppDb.getInstance(context).getGrandExchangeGraphData(itemId);
            return graphData == null ? new GrandExchangeGraphData() : graphData;
        }

        @Override
        protected void onPostExecute(final GrandExchangeGraphData graphData) {
            final Context context = weakContext.get();
            if (context == null || graphData == null) {
                callback.onGeGraphDataContextError();
                return;
            }
            final boolean cacheExpired = Math.abs(System.currentTimeMillis() - graphData.dateModified) > Constants.GE_CACHE_DURATION;
            if (!graphData.hasData() || cacheExpired || forceCacheReload) {
                Utils.getString(Constants.GE_GRAPH_URL(itemId), GEGRAPH_REQUEST_TAG, new Utils.VolleyCallback() {
                    @Override
                    public void onSuccess(String result) {
                        AppDb.getInstance(context).insertOrUpdateGrandExchangeGraphData(itemId, result);
                        callback.onGeGraphDataLoaded(result, cacheExpired);
                    }

                    @Override
                    public void onError(VolleyError error) {
                        if (graphData.hasData()) {
                            callback.onGeGraphDataLoaded(graphData.data, cacheExpired);
                        }
                        else {
                            callback.onGeGraphDataLoadFailed();
                        }
                    }

                    @Override
                    public void always() {
                    }
                });
            }
            else {
                callback.onGeGraphDataLoaded(graphData.data, false);
            }
        }
    }
}