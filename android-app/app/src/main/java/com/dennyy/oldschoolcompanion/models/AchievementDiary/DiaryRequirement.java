package com.dennyy.oldschoolcompanion.models.AchievementDiary;

public class DiaryRequirement {
    public final int skillId;
    public final String skill;
    public final int requiredLevel;

    public DiaryRequirement(int skillId, String skill, int requiredLevel) {
        this.skillId = skillId;
        this.skill = skill;
        this.requiredLevel = requiredLevel;
    }
}
