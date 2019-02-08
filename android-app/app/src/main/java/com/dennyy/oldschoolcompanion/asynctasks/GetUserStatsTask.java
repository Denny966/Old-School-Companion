package com.dennyy.oldschoolcompanion.asynctasks;

import android.content.Context;
import android.os.AsyncTask;

import com.dennyy.oldschoolcompanion.database.AppDb;
import com.dennyy.oldschoolcompanion.enums.HiscoreType;
import com.dennyy.oldschoolcompanion.helpers.Logger;
import com.dennyy.oldschoolcompanion.interfaces.UserStatsLoadedListener;
import com.dennyy.oldschoolcompanion.models.Hiscores.UserStats;

import java.lang.ref.WeakReference;

public class GetUserStatsTask extends AsyncTask<Void, Void, UserStats> {
    private WeakReference<Context> weakContext;
    private UserStatsLoadedListener listener;
    private String rsn;
    private HiscoreType hiscoreType;

    public GetUserStatsTask(final Context context, String rsn, HiscoreType hiscoreType, final UserStatsLoadedListener listener) {
        this.weakContext = new WeakReference<>(context);
        this.rsn = rsn;
        this.hiscoreType = hiscoreType;
        this.listener = listener;
    }

    @Override
    protected UserStats doInBackground(Void... voids) {
        Context context = weakContext.get();
        if (context == null) {
            return null;
        }
        UserStats userStats = null;
        try {
            userStats = AppDb.getInstance(context).getUserStats(rsn, hiscoreType);
        }
        catch (Exception ex) {
            Logger.log(ex);
        }
        return userStats;
    }

    @Override
    protected void onPostExecute(UserStats userStats) {
        if (userStats == null) {
            listener.onUserStatsLoadFailed();
        }
        else {
            listener.onUserStatsLoaded(userStats);
        }
    }
}