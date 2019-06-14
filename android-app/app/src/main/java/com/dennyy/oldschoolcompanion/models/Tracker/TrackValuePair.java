package com.dennyy.oldschoolcompanion.models.Tracker;

import android.text.Html;
import com.dennyy.oldschoolcompanion.helpers.Utils;

public class TrackValuePair {
    public final long currentValue;
    public final int gains;

    public TrackValuePair(String currentValue, String gains) {
        this.currentValue = Long.parseLong(currentValue.replace(",", ""));
        this.gains = Utils.tryParseInt(Html.fromHtml(gains.replace(",", "")).toString(), 0);
    }
}