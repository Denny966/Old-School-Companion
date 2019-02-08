package com.dennyy.oldschoolcompanion.asynctasks;

import android.content.Context;
import android.os.AsyncTask;
import com.android.volley.VolleyError;
import com.dennyy.oldschoolcompanion.database.AppDb;
import com.dennyy.oldschoolcompanion.helpers.Constants;
import com.dennyy.oldschoolcompanion.helpers.Logger;
import com.dennyy.oldschoolcompanion.helpers.Utils;
import com.dennyy.oldschoolcompanion.interfaces.OSBuddySummaryLoadedListener;
import com.dennyy.oldschoolcompanion.models.OSBuddy.OSBuddySummary;
import com.dennyy.oldschoolcompanion.models.OSBuddy.OSBuddySummaryItem;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Iterator;

public class GetOSBuddyExchangeSummaryTask extends AsyncTask<Void, Void, HashMap<String, OSBuddySummaryItem>> {
    public static final String OSBUDDY_SUMMARY_REQUEST_TAG = "osbuddy_summary_request_tag";
    private WeakReference<Context> weakContext;
    private OSBuddySummaryLoadedListener callback;
    private long dateModified;

    public GetOSBuddyExchangeSummaryTask(final Context context, final OSBuddySummaryLoadedListener callback) {
        this.weakContext = new WeakReference<>(context);
        this.callback = callback;
    }

    @Override
    protected HashMap<String, OSBuddySummaryItem> doInBackground(Void... voids) {
        Context context = weakContext.get();
        if (context == null) {
            return null;
        }
        HashMap<String, OSBuddySummaryItem> content = new HashMap<>();
        OSBuddySummary summary = AppDb.getInstance(context).getOSBuddyExchangeSummary();
        if (summary != null) {
            dateModified = summary.dateModified;
            try {
                content = parseOSBuddySummary(summary.data);
            }
            catch (JSONException ex) {
                Logger.log(ex, "failed to parse osbuddy data from database", summary.data);
            }
        }
        return content;
    }

    @Override
    protected void onPostExecute(final HashMap<String, OSBuddySummaryItem> content) {
        final Context context = weakContext.get();
        if (context == null || content == null) {
            callback.onOsBuddySummaryContextError();
            return;
        }
        final boolean cacheExpired = Math.abs(System.currentTimeMillis() - dateModified) > Constants.OSBUDDY_SUMMARY_CACHE_DURATION;
        if (content.isEmpty() || cacheExpired) {
            Utils.getString(Constants.OSBUDDY_EXCHANGE_SUMMARY_URL, OSBUDDY_SUMMARY_REQUEST_TAG, new Utils.VolleyCallback() {
                @Override
                public void onSuccess(String result) {
                    try {
                        content.clear();
                        content.putAll(parseOSBuddySummary(result));
                    }
                    catch (JSONException ex) {
                        Logger.log(ex, "failed to parse osbuddy data from web", result);
                        callback.onOsBuddySummaryLoadFailed(ex);
                        return;
                    }
                    new WriteOSBuddyExchangeSummaryTask(context, result).execute();
                    callback.onOsBuddySummaryLoaded(content, dateModified, cacheExpired);
                }

                @Override
                public void onError(VolleyError error) {
                    if (!content.isEmpty()) {
                        callback.onOsBuddySummaryLoaded(content, dateModified, cacheExpired);
                    }
                    else {
                        callback.onOsBuddySummaryLoadFailed(error);
                    }
                }

                @Override
                public void always() {
                }
            });
        }
        else {
            callback.onOsBuddySummaryLoaded(content, dateModified, false);
        }
    }

    public static HashMap<String, OSBuddySummaryItem> parseOSBuddySummary(String osBuddyItemData) throws JSONException {
        HashMap<String, OSBuddySummaryItem> summaryItems = new HashMap<>();
        JSONObject jsonObject = new JSONObject(osBuddyItemData);
        Iterator itemLimitsIterator = jsonObject.keys();
        while (itemLimitsIterator.hasNext()) {
            String itemId = (String) itemLimitsIterator.next();
            JSONObject summaryJson = jsonObject.getJSONObject(itemId);
            int id = summaryJson.getInt("id");
            String name = summaryJson.getString("name");
            boolean members = summaryJson.getBoolean("members");
            int buyPrice = summaryJson.getInt("buy_average");
            int sellPrice = summaryJson.getInt("sell_average");
            int storePrice = summaryJson.getInt("sp");
            OSBuddySummaryItem summaryItem = new OSBuddySummaryItem(id, name, members, buyPrice, sellPrice, storePrice);
            summaryItems.put(itemId, summaryItem);
        }
        return summaryItems;
    }
}