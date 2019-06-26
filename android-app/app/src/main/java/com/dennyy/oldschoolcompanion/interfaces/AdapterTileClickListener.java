package com.dennyy.oldschoolcompanion.interfaces;

import com.dennyy.oldschoolcompanion.models.General.TileData;

public interface AdapterTileClickListener {
    void onTileClick(TileData tileData);
    void onEditButtonClick(TileData tileData);
    void onDeleteButtonClick(TileData tileData);
}