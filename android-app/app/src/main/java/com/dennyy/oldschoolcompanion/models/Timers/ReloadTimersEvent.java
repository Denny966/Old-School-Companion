package com.dennyy.oldschoolcompanion.models.Timers;

import com.dennyy.oldschoolcompanion.enums.ReloadTimerSource;

public class ReloadTimersEvent {
    public final ReloadTimerSource source;

    public ReloadTimersEvent(ReloadTimerSource source) {
        this.source = source;
    }
}