package com.dennyy.oldschoolcompanion.interfaces;

import com.dennyy.oldschoolcompanion.models.Timers.Timer;

public interface AdapterTimerClickListener {
    void onStartClick(Timer timer);
    void onEditClick(Timer timer);
    void onConfirmDeleteClick(Timer timer);
}
