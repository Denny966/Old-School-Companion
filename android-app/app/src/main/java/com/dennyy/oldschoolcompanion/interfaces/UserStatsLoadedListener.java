package com.dennyy.oldschoolcompanion.interfaces;

import com.dennyy.oldschoolcompanion.models.Hiscores.UserStats;

public interface UserStatsLoadedListener {
    void onUserStatsLoaded(UserStats userStats);

    void onUserStatsLoadFailed();
}