package com.dennyy.oldschoolcompanion.models.AchievementDiary;

import com.dennyy.oldschoolcompanion.enums.DiaryType;

import java.util.ArrayList;

public class Diaries extends ArrayList<Diary> {
    public Diary getByType(DiaryType diaryType) {
        for (Diary diary : this) {
            if (diary.diaryType == diaryType) {
                return diary;
            }
        }
        return null;
    }
}
