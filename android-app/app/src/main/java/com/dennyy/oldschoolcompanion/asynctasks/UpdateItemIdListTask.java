package com.dennyy.oldschoolcompanion.asynctasks;

import android.content.Context;
import android.os.AsyncTask;

import com.dennyy.oldschoolcompanion.enums.ItemIdListUpdateResult;
import com.dennyy.oldschoolcompanion.helpers.Constants;
import com.dennyy.oldschoolcompanion.helpers.Logger;
import com.dennyy.oldschoolcompanion.helpers.RsUtils;
import com.dennyy.oldschoolcompanion.helpers.Utils;
import com.dennyy.oldschoolcompanion.interfaces.ItemIdListResultListener;

import org.json.JSONException;

import java.lang.ref.WeakReference;
import java.text.ParseException;
import java.util.Date;

public class UpdateItemIdListTask extends AsyncTask<Void, Void, ItemIdListUpdateResult> {
    private WeakReference<Context> context;
    private ItemIdListResultListener callback;
    private String downloadedItemIdListJson;

    public UpdateItemIdListTask(final Context context, final String downloadedItemIdListJson, final ItemIdListResultListener callback) {
        this.context = new WeakReference<>(context);
        this.downloadedItemIdListJson = downloadedItemIdListJson;
        this.callback = callback;
    }

    @Override
    protected ItemIdListUpdateResult doInBackground(Void... voids) {
        try {
            String existingItemIdList = Utils.readFromFile(context.get(), Constants.ITEMIDLIST_FILE_NAME);
            if (Utils.isNullOrEmpty(existingItemIdList)) {
                existingItemIdList = downloadedItemIdListJson;
                String assetsItemIdList = Utils.readFromAssets(context.get(), "names.json");
                Date existingFileDate = RsUtils.getDateFromItemIdList(existingItemIdList);
                Date assetsDate = RsUtils.getDateFromItemIdList(assetsItemIdList);
                if (assetsDate.before(existingFileDate)) {
                    Utils.writeToFile(context.get(), Constants.ITEMIDLIST_FILE_NAME, downloadedItemIdListJson);
                    return ItemIdListUpdateResult.SUCCESS;
                }
                else {
                    Utils.writeToFile(context.get(), Constants.ITEMIDLIST_FILE_NAME, assetsItemIdList);
                    return ItemIdListUpdateResult.UP_TO_DATE;
                }
            }
            Date fileDate = RsUtils.getDateFromItemIdList(existingItemIdList);
            Date resultDate = RsUtils.getDateFromItemIdList(downloadedItemIdListJson);

            if (fileDate.before(resultDate)) {
                Utils.writeToFile(context.get(), Constants.ITEMIDLIST_FILE_NAME, downloadedItemIdListJson);
                return ItemIdListUpdateResult.SUCCESS;
            }
        }
        catch (JSONException | ParseException ex) {
            Logger.log(ex);
            Utils.writeToFile(context.get(), Constants.ITEMIDLIST_FILE_NAME, "");
            return ItemIdListUpdateResult.ERROR;
        }
        return ItemIdListUpdateResult.UP_TO_DATE;
    }

    @Override
    protected void onPostExecute(ItemIdListUpdateResult result) {
        switch (result) {
            case SUCCESS:
                callback.onItemsUpdated();
                break;
            case UP_TO_DATE:
                callback.onItemsNotUpdated();
                break;
            default:
                callback.onError();
                break;
        }
    }
}
