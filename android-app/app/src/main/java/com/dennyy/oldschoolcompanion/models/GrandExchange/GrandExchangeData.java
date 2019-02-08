package com.dennyy.oldschoolcompanion.models.GrandExchange;

import com.dennyy.oldschoolcompanion.helpers.Utils;

public class GrandExchangeData {
    public String itemId;
    public String data;
    public long dateModified;

    public boolean hasData() {
        return !Utils.isNullOrEmpty(itemId);
    }
}