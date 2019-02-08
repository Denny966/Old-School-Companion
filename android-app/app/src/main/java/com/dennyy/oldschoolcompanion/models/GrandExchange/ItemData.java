package com.dennyy.oldschoolcompanion.models.GrandExchange;

import com.dennyy.oldschoolcompanion.models.OSBuddy.OSBuddySummaryItem;

import java.util.HashMap;

public class ItemData {
    public final GrandExchangeData geData;
    public final GrandExchangeGraphData graphData;
    public final GrandExchangeUpdateData geUpdate;
    public final HashMap<String, OSBuddySummaryItem> osbSummary;

    public ItemData(GrandExchangeData geData, GrandExchangeGraphData graphData, GrandExchangeUpdateData geUpdate, HashMap<String, OSBuddySummaryItem> osbSummary) {
        this.geData = geData;
        this.graphData = graphData;
        this.geUpdate = geUpdate;
        this.osbSummary = osbSummary;
    }
}
