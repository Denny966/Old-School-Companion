package com.dennyy.oldschoolcompanion.models.GrandExchange;

import java.util.ArrayList;

public class GeHistory extends ArrayList<GeHistoryEntry> {
    public GeHistory() {

    }

    public GeHistory(ArrayList<GeHistoryEntry> entries) {
        super(entries);
    }

    public void toggleFavorite(String itemId, boolean isFavorite) {
        for (GeHistoryEntry entry : this) {
            if (entry.itemId.equals(itemId)) {
                entry.setFavorite(isFavorite);
                break;
            }
        }
    }

    public boolean isFavorite(String itemId) {
        for (GeHistoryEntry geHistoryEntry : this) {
            if (geHistoryEntry.isFavorite() && geHistoryEntry.itemId.equals(itemId)) {
                return true;
            }
        }
        return false;
    }
}
