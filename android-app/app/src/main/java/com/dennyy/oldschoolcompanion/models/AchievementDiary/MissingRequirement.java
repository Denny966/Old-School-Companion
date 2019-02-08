package com.dennyy.oldschoolcompanion.models.AchievementDiary;

public class MissingRequirement {
    public final String skill;
    public final int requiredLevel;
    public final int currentLevel;


    public MissingRequirement(String skill, int requiredLevel, int currentLevel) {
        this.skill = skill;
        this.requiredLevel = requiredLevel;
        this.currentLevel = currentLevel;
    }

    public int getDifference() {
        return requiredLevel - currentLevel;
    }
}
