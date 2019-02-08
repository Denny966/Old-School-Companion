package com.dennyy.oldschoolcompanion.interfaces;

import com.dennyy.oldschoolcompanion.models.SkillCalculator.SkillData;

public interface ActionsLoadListener {
    void onActionsLoaded(SkillData skillData);

    void onActionsLoadFailed();
}