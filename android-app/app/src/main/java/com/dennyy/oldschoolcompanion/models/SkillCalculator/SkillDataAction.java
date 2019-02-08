package com.dennyy.oldschoolcompanion.models.SkillCalculator;

import com.dennyy.oldschoolcompanion.enums.SkillType;

public class SkillDataAction {
    public final SkillType skillType;
    public final String name;
    public final int level;
    public final double exp;
    public final boolean ignoreBonus;

    public SkillDataAction(SkillType skillType, String name, int level, double exp, boolean ignoreBonus) {
        this.skillType = skillType;
        this.name = name;
        this.level = level;
        this.exp = exp;
        this.ignoreBonus = ignoreBonus;
    }

    public String getFormattedName() {
        if (SkillType.isCombat(skillType, SkillType.PRAYER)) {
            String[] split = name.split("_");
            if (split.length > 1) {
                return String.format("%s (%s)", split[0], split[1]);
            }
        }
        return name;
    }
}
