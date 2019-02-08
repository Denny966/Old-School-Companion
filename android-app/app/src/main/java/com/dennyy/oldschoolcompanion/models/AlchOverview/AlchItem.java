package com.dennyy.oldschoolcompanion.models.AlchOverview;

public class AlchItem implements Comparable<AlchItem> {
    public final String id;
    public final String name;
    public final boolean isMembers;
    public final int buyPrice;
    public final int highAlchValue;
    public final int lowAlchValue;
    public final int buyLimit;

    public boolean expanded;

    public AlchItem(String id, String name, boolean isMembers, int buyPrice, int lowAlchValue, int highAlchValue, Integer buyLimit) {
        this.id = id;
        this.name = name;
        this.isMembers = isMembers;
        this.buyPrice = buyPrice;
        this.lowAlchValue = lowAlchValue;
        this.highAlchValue = highAlchValue;
        this.buyLimit = buyLimit == null ? -1 : buyLimit;
    }

    public int getHighAlchProfit(int natureRunePrice) {
        return highAlchValue - (buyPrice + natureRunePrice);
    }


    public int getLowAlchProfit(int natureRunePrice) {
        return lowAlchValue - (buyPrice + natureRunePrice);
    }

    @Override
    public int compareTo(AlchItem o) {
        return o.getHighAlchProfit(0) - getHighAlchProfit(0);
    }
}
