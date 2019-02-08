package com.dennyy.oldschoolcompanion.models.SkillCalculator;

public class SkillDataBonus {
    public final String name;
    public final float value;

    public SkillDataBonus(String name, float value) {
        this.name = name;
        this.value = value;
    }

    public boolean isEmpty() {
        return this.value == 0;
    }
}
