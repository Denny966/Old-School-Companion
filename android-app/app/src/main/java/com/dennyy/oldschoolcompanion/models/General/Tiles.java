package com.dennyy.oldschoolcompanion.models.General;

import java.util.ArrayList;
import java.util.ListIterator;

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

    public void removeCustomTiles() {
        ListIterator<TileData> iterator = listIterator();
        while (iterator.hasNext()) {
            if (iterator.next().isCustomTile) {
                iterator.remove();
            }
        }
    }

    public void removeById(long id) {
        ListIterator<TileData> iterator = listIterator();
        while (iterator.hasNext()) {
            if (iterator.next().id == id) {
                iterator.remove();
            }
        }
    }
}
