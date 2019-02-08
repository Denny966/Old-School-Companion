package com.dennyy.oldschoolcompanion.models.GrandExchange;

public class GeHistoryEntry {
    public final String itemId;
    public final String itemName;
    private boolean isFavorite;

    public GeHistoryEntry(String itemId, String itemName, boolean isFavorite) {
        this.itemId = itemId;
        this.itemName = itemName;
        this.isFavorite = isFavorite;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }
}
