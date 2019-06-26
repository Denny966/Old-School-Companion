package com.dennyy.oldschoolcompanion.models.General;

import android.graphics.drawable.Drawable;

public class TileData implements Comparable<TileData> {
    public final long id;
    public final String name;
    public final Drawable drawable;
    public final boolean isCustomTile;

    private int sortOrder;
    private String url;

    public TileData(long id, String name, Drawable drawable, boolean isCustomTile) {
        this.id = id;
        this.name = name;
        this.drawable = drawable;
        this.isCustomTile = isCustomTile;
    }

    public TileData(long id, String text, Drawable drawable) {
        this(id, text, drawable, false);
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public int compareTo(TileData tileData) {
        return Integer.compare(this.sortOrder, tileData.sortOrder);
    }
}