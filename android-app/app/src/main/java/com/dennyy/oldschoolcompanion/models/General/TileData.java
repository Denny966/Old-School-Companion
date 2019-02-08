package com.dennyy.oldschoolcompanion.models.General;

import android.graphics.drawable.Drawable;

public class TileData implements Comparable<TileData> {
    public final long id;
    public final String text;
    public final Drawable drawable;

    private int sortOrder;

    public TileData(long id, String text, Drawable drawable) {
        this.id = id;
        this.text = text;
        this.drawable = drawable;
        this.sortOrder = Integer.MAX_VALUE;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    @Override
    public int compareTo(TileData tileData) {
        return Integer.compare(this.sortOrder, tileData.sortOrder);
    }
}