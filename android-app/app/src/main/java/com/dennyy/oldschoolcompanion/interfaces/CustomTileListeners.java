package com.dennyy.oldschoolcompanion.interfaces;

import com.dennyy.oldschoolcompanion.models.CustomTile.CustomTile;

import java.util.List;

public abstract class CustomTileListeners {
    public interface CustomTileListener {
        void onCustomTilesLoaded(List<CustomTile> tiles);
        void onCustomTilesLoadFailed();
        void always();
    }
}
