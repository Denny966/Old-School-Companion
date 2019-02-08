package com.dennyy.oldschoolcompanion.models.GrandExchange;


import com.dennyy.oldschoolcompanion.helpers.Utils;

public class GrandExchangeUpdateData {
    public String data;
    public long dateModified;

    public boolean hasData() {
        return !Utils.isNullOrEmpty(data);
    }
}
