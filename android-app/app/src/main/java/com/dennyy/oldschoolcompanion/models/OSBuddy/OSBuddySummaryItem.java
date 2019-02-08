package com.dennyy.oldschoolcompanion.models.OSBuddy;

import static com.dennyy.oldschoolcompanion.helpers.Constants.HIGH_ALCHEMY_CONSTANT;
import static com.dennyy.oldschoolcompanion.helpers.Constants.LOW_ALCHEMY_CONSTANT;

public class OSBuddySummaryItem {
    public final int id;
    public final String name;
    public final boolean members;
    public final int buyPrice;
    public final int sellPrice;
    public final int storePrice;

    public OSBuddySummaryItem(int id, String name, boolean members, int buyPrice, int sellPrice, int storePrice) {
        this.id = id;
        this.name = name;
        this.members = members;
        this.buyPrice = buyPrice;
        this.sellPrice = sellPrice;
        this.storePrice = storePrice;
    }

    public int getHighAlchValue() {
        return (int) Math.floor(this.storePrice * HIGH_ALCHEMY_CONSTANT);
    }

    public int getLowAlchValue() {
        return (int) Math.floor(this.storePrice * LOW_ALCHEMY_CONSTANT);
    }
}
