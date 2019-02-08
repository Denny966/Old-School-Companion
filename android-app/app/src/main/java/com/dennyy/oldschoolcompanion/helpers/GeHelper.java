package com.dennyy.oldschoolcompanion.helpers;

import android.content.Context;
import com.dennyy.oldschoolcompanion.asynctasks.GeAsyncTasks;
import com.dennyy.oldschoolcompanion.models.GrandExchange.GrandExchangeItem;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;

public abstract class GeHelper {
    public static final HashMap<String, Integer> BUY_LIMITS = new HashMap<>();

    public static void init(Context context) {
        if (!BUY_LIMITS.isEmpty()) {
            return;
        }
        new GeAsyncTasks.GetGeLimits(context).execute();
    }

    public static HashMap<String, Integer> getItemLimits(Context context) throws JSONException {
        HashMap<String, Integer> itemLimits = new HashMap<>();
        String itemLimitsJson = Utils.readFromAssets(context, "ge_limits.json");
        JSONObject jsonObject = new JSONObject(itemLimitsJson);
        Iterator itemLimitsIterator = jsonObject.keys();
        while (itemLimitsIterator.hasNext()) {
            String itemId = (String) itemLimitsIterator.next();
            int limit = jsonObject.getInt(itemId);
            itemLimits.put(itemId, limit);
        }
        return itemLimits;
    }

    public static GrandExchangeItem getItemFromJson(String id, int limit, JSONObject jsonItem) throws JSONException {
        GrandExchangeItem geItem = new GrandExchangeItem();

        geItem.id = id;
        geItem.name = jsonItem.getString("name");
        geItem.description = jsonItem.getString("description");
        geItem.members = jsonItem.getBoolean("members");
        geItem.limit = limit;
        geItem.price = (int) RsUtils.revkmbt(jsonItem.getJSONObject("current").getString("price").replace(",", ""));
        geItem.change = RsUtils.revkmbt(jsonItem.getJSONObject("today").getString("price").replace(",", ""));
        geItem.changePercent = RsUtils.getGEPercentChange(geItem.price, geItem.change);
        geItem.day30changePercent = RsUtils.revkmbt(jsonItem.getJSONObject("day30").getString("change").replace("%", ""));
        geItem.day30change = RsUtils.getGEPriceChange(geItem.price, geItem.day30changePercent);
        geItem.day90changePercent = RsUtils.revkmbt(jsonItem.getJSONObject("day90").getString("change").replace("%", ""));
        geItem.day90change = RsUtils.getGEPriceChange(geItem.price, geItem.day90changePercent);
        geItem.day180changePercent = RsUtils.revkmbt(jsonItem.getJSONObject("day180").getString("change").replace("%", ""));
        geItem.day180change = RsUtils.getGEPriceChange(geItem.price, geItem.day180changePercent);

        return geItem;
    }
}
