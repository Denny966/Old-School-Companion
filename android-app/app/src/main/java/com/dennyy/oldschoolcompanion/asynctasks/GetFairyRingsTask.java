package com.dennyy.oldschoolcompanion.asynctasks;

import android.content.Context;
import android.os.AsyncTask;

import com.dennyy.oldschoolcompanion.helpers.Logger;
import com.dennyy.oldschoolcompanion.helpers.Utils;
import com.dennyy.oldschoolcompanion.interfaces.FairyRingsLoadedListener;
import com.dennyy.oldschoolcompanion.models.FairyRings.FairyRing;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class GetFairyRingsTask extends AsyncTask<Void, Void, ArrayList<FairyRing>> {
    private WeakReference<Context> weakContext;
    private FairyRingsLoadedListener callback;

    public GetFairyRingsTask(final Context context, final FairyRingsLoadedListener callback) {
        this.weakContext = new WeakReference<>(context);
        this.callback = callback;
    }

    @Override
    protected ArrayList<FairyRing> doInBackground(Void... voids) {
        ArrayList<FairyRing> fairyRings = new ArrayList<>();
        Context context = weakContext.get();
        if (context == null) {
            return fairyRings;
        }
        try {
            String fairyRingsString = Utils.readFromAssets(context, "fairyrings.json");
            JSONArray fairyRingJsonArray = new JSONArray(fairyRingsString);
            for (int i = 0; i < fairyRingJsonArray.length(); i++) {
                JSONObject jsonObject = fairyRingJsonArray.getJSONObject(i);
                FairyRing fairyRing = new FairyRing();
                fairyRing.code = jsonObject.getString("code");
                fairyRing.location = jsonObject.getString("location");
                fairyRing.pointsOfInterest = jsonObject.getString("points of interest");
                fairyRings.add(fairyRing);
            }
        }
        catch (JSONException ex) {
            Logger.log(ex);
        }
        return fairyRings;
    }


    @Override
    protected void onPostExecute(ArrayList<FairyRing> items) {
        if (items.size() > 0) {
            callback.onFairyRingsLoaded(items);
        }
        else {
            callback.onFairyRingsLoadError();
        }
    }
}