package com.dennyy.oldschoolcompanion.models.Tracker;

import android.text.Html;

public class TrackValuePair {
    public final long currentValue;
    public final int gains;

    public TrackValuePair(String currentValue, String gains) {
        this.currentValue = Long.parseLong(currentValue.replace(",", ""));
        this.gains = Integer.parseInt(Html.fromHtml(gains.replace(",", "")).toString());
    }
}