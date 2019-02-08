package com.dennyy.oldschoolcompanion.models.General;

import java.util.ArrayList;

public class Tiles extends ArrayList<TileData> {

    public Tiles() {
    }

    public Tiles(ArrayList<TileData> list) {
        super(list);
    }

    public TileData getById(long id) {
        for (TileData tileData : this) {
            if (tileData.id == id) {
                return tileData;
            }
        }
        return null;
    }
}
