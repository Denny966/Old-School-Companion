package com.dennyy.oldschoolcompanion.models.Timers;

import java.io.Serializable;

public class Timer implements Serializable {
    public int id;
    public String title;
    public String description;
    public int interval;
    public boolean isRepeating;

    private boolean isActive;

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public long getDelayMs() {
        return this.interval * 1000;
    }
}
