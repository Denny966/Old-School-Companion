package com.dennyy.oldschoolcompanion.interfaces;

import com.dennyy.oldschoolcompanion.models.FairyRings.FairyRing;

import java.util.ArrayList;

public interface FairyRingsLoadedListener {
    void onFairyRingsLoaded(ArrayList<FairyRing> fairyRings);

    void onFairyRingsLoadError();
}
