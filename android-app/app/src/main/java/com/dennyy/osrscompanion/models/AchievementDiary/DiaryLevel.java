package com.dennyy.osrscompanion.models.AchievementDiary;

import com.dennyy.osrscompanion.enums.DiaryType;

import java.util.ArrayList;

public class DiaryLevel {
    public DiaryType diaryType;
    public ArrayList<DiaryRequirement> missingRequirements = new ArrayList<>();

    public boolean canComplete() {
        return missingRequirements.size() < 1;
    }
}
