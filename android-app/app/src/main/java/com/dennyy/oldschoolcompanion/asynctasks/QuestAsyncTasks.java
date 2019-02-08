package com.dennyy.oldschoolcompanion.asynctasks;

import android.content.Context;
import android.os.AsyncTask;
import com.dennyy.oldschoolcompanion.database.AppDb;
import com.dennyy.oldschoolcompanion.helpers.Logger;
import com.dennyy.oldschoolcompanion.helpers.Utils;
import com.dennyy.oldschoolcompanion.interfaces.QuestListeners;
import com.dennyy.oldschoolcompanion.models.General.Quest;
import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;

public abstract class QuestAsyncTasks {

    public static class QuestLoadTask extends AsyncTask<String, Void, ArrayList<Quest>> {
        private WeakReference<Context> weakContext;
        private QuestListeners.LoadedListener questsLoadedListener;

        public QuestLoadTask(Context context, QuestListeners.LoadedListener questsLoadedListener) {
            this.weakContext = new WeakReference<>(context);
            this.questsLoadedListener = questsLoadedListener;
        }

        @Override
        protected ArrayList<Quest> doInBackground(String... params) {
            Context context = weakContext.get();
            if (context == null) {
                return null;
            }
            ArrayList<Quest> quests = new ArrayList<>();
            try {
                HashSet<String> hashSet = AppDb.getInstance(context).getQuestCompletions();
                JSONArray array = new JSONArray(Utils.readFromAssets(context, "quests.json"));
                for (int i = 0; i < array.length(); i++) {
                    JSONObject obj = array.getJSONObject(i);
                    String questName = obj.getString("name");
                    Quest quest = new Quest(questName,
                                            obj.getString("url"),
                                            obj.getString("runehqurl"),
                                            obj.getInt("difficulty"),
                                            obj.getInt("length"),
                                            obj.getBoolean("p2p"),
                                            obj.getInt("qp"));
                    quest.setCompleted(hashSet.contains(questName));
                    quests.add(quest);
                }

                Collections.sort(quests, new Comparator<Quest>() {
                    @Override
                    public int compare(Quest quest1, Quest quest2) {
                        return quest1.name.compareTo(quest2.name);
                    }
                });
            }
            catch (Exception ex) {
                Logger.log(ex);
                return null;
            }
            return quests;
        }

        @Override
        protected void onPostExecute(ArrayList<Quest> quests) {
            if (quests == null) {
                questsLoadedListener.onQuestsLoadError();
            }
            else {
                questsLoadedListener.onQuestsLoaded(quests);
            }
        }
    }

    public static class InsertOrUpdateQuestCompletionTask extends AsyncTask<Void, Void, Void> {
        private WeakReference<Context> weakContext;
        private String questName;
        private boolean isCompleted;

        public InsertOrUpdateQuestCompletionTask(final Context context, String questName, boolean isCompleted) {
            this.weakContext = new WeakReference<>(context);
            this.questName = questName;
            this.isCompleted = isCompleted;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            Context context = weakContext.get();
            if (context != null) {
                AppDb.getInstance(context).insertOrUpdateQuestCompletion(questName, isCompleted);
            }
            return null;
        }
    }
}