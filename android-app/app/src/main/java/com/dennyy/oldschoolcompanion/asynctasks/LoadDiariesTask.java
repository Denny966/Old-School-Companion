package com.dennyy.oldschoolcompanion.asynctasks;

import android.content.Context;
import android.os.AsyncTask;

import com.dennyy.oldschoolcompanion.enums.DiaryType;
import com.dennyy.oldschoolcompanion.helpers.Logger;
import com.dennyy.oldschoolcompanion.helpers.Utils;
import com.dennyy.oldschoolcompanion.interfaces.DiariesLoadedListener;
import com.dennyy.oldschoolcompanion.models.AchievementDiary.Diaries;
import com.dennyy.oldschoolcompanion.models.AchievementDiary.DiariesMap;
import com.dennyy.oldschoolcompanion.models.AchievementDiary.Diary;
import com.dennyy.oldschoolcompanion.models.AchievementDiary.DiaryRequirement;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.List;

public class LoadDiariesTask extends AsyncTask<String, Void, DiariesMap> {
    private WeakReference<Context> weakContext;
    private DiariesLoadedListener diariesLoadedListener;

    public LoadDiariesTask(Context context, DiariesLoadedListener diariesLoadedListener) {
        this.weakContext = new WeakReference<>(context);
        this.diariesLoadedListener = diariesLoadedListener;
    }

    @Override
    protected DiariesMap doInBackground(String... params) {
        DiariesMap diariesMap = new DiariesMap();
        Context context = weakContext.get();
        if (context == null) {
            return diariesMap;
        }
        try {
            DiaryType[] levelMap = new DiaryType[]{ DiaryType.EASY, DiaryType.MEDIUM, DiaryType.HARD, DiaryType.ELITE };
            JSONObject diariesJson = new JSONObject(Utils.readFromAssets(context, "diary_reqs.json"));
            Iterator diariesIterator = diariesJson.keys();
            while (diariesIterator.hasNext()) {
                String diaryName = (String) diariesIterator.next();
                JSONObject diaryJson = diariesJson.getJSONObject(diaryName);
                Diaries diaries = new Diaries();
                for (int j = 0; j < 4; j++) {
                    Iterator skills = diaryJson.keys();
                    Diary diary = new Diary();
                    diary.diaryType = levelMap[j];
                    int i = 1;
                    while (skills.hasNext()) {
                        String skillName = (String) skills.next();
                        int reqLvl = diaryJson.getJSONArray(skillName).getInt(j);
                        if (reqLvl > 1) {
                            DiaryRequirement diaryRequirement = new DiaryRequirement(i, skillName, reqLvl);
                            diary.requirements.add(diaryRequirement);
                        }
                        i++;
                        if (!skills.hasNext()) {
                            diaries.add(diary);
                        }
                    }
                }
                diariesMap.put(diaryName, diaries);
            }

            JSONObject diariesQuestsJson = new JSONObject(Utils.readFromAssets(context, "diary_quest_reqs.json"));
            Iterator diariesQuestsIterator = diariesQuestsJson.keys();
            while (diariesQuestsIterator.hasNext()) {
                String diaryName = (String) diariesQuestsIterator.next();
                JSONArray diaryQuestsArray = diariesQuestsJson.getJSONArray(diaryName);
                for (int i = 0; i < 4; i++) {
                    JSONArray quests = (JSONArray) diaryQuestsArray.get(i);
                    List<String> list = Utils.jsonArrayToList(String.class, quests);
                    DiaryType diaryType = levelMap[i];
                    Diaries diaries = diariesMap.get(diaryName);
                    Diary diary = diaries.getByType(diaryType);
                    diary.questRequirements.addAll(list);
                }
            }
        }
        catch (Exception ex) {
            Logger.log(ex);
        }
        return diariesMap;
    }

    @Override
    protected void onPostExecute(DiariesMap diariesMap) {
        if (diariesMap.size() > 0) {
            diariesLoadedListener.onDiariesLoaded(diariesMap);
        }
        else {
            diariesLoadedListener.onDiariesLoadError();
        }
    }
}
