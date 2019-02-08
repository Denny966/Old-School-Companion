package com.dennyy.oldschoolcompanion.interfaces;

import com.dennyy.oldschoolcompanion.models.Timers.Timer;

import java.util.ArrayList;

public interface TimersLoadedListener {
    void onTimersLoaded(ArrayList<Timer> timers);

    void onTimersLoadFailed();
}
