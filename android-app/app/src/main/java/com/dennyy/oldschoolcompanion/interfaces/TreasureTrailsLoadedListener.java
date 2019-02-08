package com.dennyy.oldschoolcompanion.interfaces;

import com.dennyy.oldschoolcompanion.models.TreasureTrails.TreasureTrails;

public interface TreasureTrailsLoadedListener {
    void onTreasureTrailsLoaded(TreasureTrails treasureTrails);

    void onTreasureTrailsLoadError();
}
