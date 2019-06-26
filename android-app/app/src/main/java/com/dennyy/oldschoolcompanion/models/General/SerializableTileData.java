package com.dennyy.oldschoolcompanion.models.General;

import java.io.Serializable;

public class SerializableTileData implements Serializable {
    public final long id;
    public final String name;
    public final boolean isCustomTile;
    public final int sortOrder;
    public final String url;

    public SerializableTileData(TileData tileData) {
        this.id = tileData.id;
        this.name = tileData.name;
        this.isCustomTile = tileData.isCustomTile;
        this.sortOrder = tileData.getSortOrder();
        this.url = tileData.getUrl();
    }
}
