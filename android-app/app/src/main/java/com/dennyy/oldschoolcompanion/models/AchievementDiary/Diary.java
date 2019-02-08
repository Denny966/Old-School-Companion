package com.dennyy.oldschoolcompanion.models.AchievementDiary;

import com.dennyy.oldschoolcompanion.enums.DiaryType;
import com.dennyy.oldschoolcompanion.helpers.Constants;

import java.util.ArrayList;

public class Diary {
    public DiaryType diaryType;
    public ArrayList<DiaryRequirement> requirements = new ArrayList<>();
    public ArrayList<String> questRequirements = new ArrayList<>();

    public ArrayList<MissingRequirement> getMissingRequirements(String[] stats) {
        ArrayList<MissingRequirement> missingRequirements = new ArrayList<>();
        if (stats.length < Constants.REQUIRED_STATS_LENGTH)
        {
            return null;
        }
        for (DiaryRequirement requirement : requirements) {
            String[] line = stats[requirement.skillId].split(",");
            int level = Integer.parseInt(line[1]);
            if (level < requirement.requiredLevel) {
                missingRequirements.add(new MissingRequirement(requirement.skill, requirement.requiredLevel, level));
            }
        }
        return missingRequirements;
    }
}
