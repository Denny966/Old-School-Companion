package com.dennyy.oldschoolcompanion.models.CustomTile;

public class CustomTile {
    public final int id;
    public final String name;
    public final int sortOrder;
    public final String url;

    public CustomTile(int id, String name, int sortOrder, String url) {
        this.id = id;
        this.name = name;
        this.sortOrder = sortOrder;
        this.url = url;
    }
}
