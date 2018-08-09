package com.dennyy.osrscompanion.models.AchievementDiary;

public class DiaryRequirement {
    public String skill;
    public int currentLevel;
    public int requiredLevel;

    public DiaryRequirement(String skill, int currentLevel, int requiredLevel) {
        this.skill = skill;
        this.currentLevel = currentLevel;
        this.requiredLevel = requiredLevel;
    }

    public int getDifference() {
        return requiredLevel - currentLevel;
    }
}
