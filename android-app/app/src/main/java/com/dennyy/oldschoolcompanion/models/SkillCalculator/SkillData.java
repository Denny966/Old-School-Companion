package com.dennyy.oldschoolcompanion.models.SkillCalculator;

import java.util.ArrayList;

public class SkillData {
    public final ArrayList<SkillDataBonus> bonuses;
    public final ArrayList<SkillDataAction> actions;

    public SkillData(ArrayList<SkillDataBonus> bonuses, ArrayList<SkillDataAction> actions) {
        this.bonuses = bonuses;
        this.actions = actions;
    }

    public boolean hasBonuses() {
        return bonuses.size() > 0;
    }
}
