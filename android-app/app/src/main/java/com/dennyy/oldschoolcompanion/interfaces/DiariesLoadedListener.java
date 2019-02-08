package com.dennyy.oldschoolcompanion.interfaces;

import com.dennyy.oldschoolcompanion.models.AchievementDiary.DiariesMap;

public interface DiariesLoadedListener {
    void onDiariesLoaded(DiariesMap diariesMap);

    void onDiariesLoadError();
}
