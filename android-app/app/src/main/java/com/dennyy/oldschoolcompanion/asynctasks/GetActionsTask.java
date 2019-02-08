package com.dennyy.oldschoolcompanion.asynctasks;

import android.content.Context;
import android.os.AsyncTask;

import com.dennyy.oldschoolcompanion.enums.SkillType;
import com.dennyy.oldschoolcompanion.helpers.Logger;
import com.dennyy.oldschoolcompanion.helpers.Utils;
import com.dennyy.oldschoolcompanion.interfaces.ActionsLoadListener;
import com.dennyy.oldschoolcompanion.models.SkillCalculator.SkillData;
import com.dennyy.oldschoolcompanion.models.SkillCalculator.SkillDataAction;
import com.dennyy.oldschoolcompanion.models.SkillCalculator.SkillDataBonus;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class GetActionsTask extends AsyncTask<Void, Void, SkillData> {
    private WeakReference<Context> weakContext;
    private ActionsLoadListener callback;
    private String dataFileName;
    private SkillType skillType;

    public GetActionsTask(final Context context, SkillType skillType, String dataFileName, final ActionsLoadListener callback) {
        this.weakContext = new WeakReference<>(context);
        this.dataFileName = dataFileName;
        this.callback = callback;
        this.skillType = skillType;
    }

    @Override
    protected SkillData doInBackground(Void... voids) {
        ArrayList<SkillDataBonus> bonuses = new ArrayList<>();
        ArrayList<SkillDataAction> actions = new ArrayList<>();
        Context context = weakContext.get();
        if (context == null) {
            return null;
        }
        try {
            String actionsString = Utils.readFromAssets(context, dataFileName);
            JSONObject jsonObject = new JSONObject(actionsString);
            if (jsonObject.has("bonuses")) {
                JSONArray bonusesArray = jsonObject.getJSONArray("bonuses");
                for (int i = 0; i < bonusesArray.length(); i++) {
                    JSONObject bonusObject = bonusesArray.getJSONObject(i);
                    String name = bonusObject.getString("name");
                    float value = (float) bonusObject.getDouble("value");
                    bonuses.add(new SkillDataBonus(name, value));
                }
            }

            JSONArray actionsArray = jsonObject.getJSONArray("actions");
            for (int i = 0; i < actionsArray.length(); i++) {
                JSONObject actionsObject = actionsArray.getJSONObject(i);
                String name = actionsObject.getString("name");
                int level = actionsObject.getInt("level");
                double exp = actionsObject.getDouble("xp");
                boolean ignoreBonus = actionsObject.has("ignoreBonus") && actionsObject.getBoolean("ignoreBonus");
                actions.add(new SkillDataAction(skillType, name, level, exp, ignoreBonus));

            }

            final boolean isCombat = SkillType.isCombat(skillType, SkillType.PRAYER);
            Collections.sort(actions, new Comparator<SkillDataAction>() {
                @Override
                public int compare(SkillDataAction o1, SkillDataAction o2) {
                    int lvlCompare = Integer.compare(o1.level, o2.level);
                    if (lvlCompare != 0) {
                        return lvlCompare;
                    }
                    else if (isCombat) {
                        return o1.name.compareToIgnoreCase(o2.name);
                    }
                    else {
                        int expCompare = Double.compare(o1.exp, o2.exp);
                        if (expCompare != 0) {
                            return expCompare;
                        }
                        else {
                            return o1.name.compareToIgnoreCase(o2.name);
                        }
                    }
                }
            });
        }
        catch (Exception ex) {
            Logger.log(ex);
            return null;
        }

        SkillData skillData = new SkillData(bonuses, actions);
        return skillData;
    }

    @Override
    protected void onPostExecute(SkillData skillData) {
        if (skillData == null) {
            callback.onActionsLoadFailed();
        }
        else {
            callback.onActionsLoaded(skillData);
        }
    }
}