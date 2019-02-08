package com.dennyy.oldschoolcompanion.interfaces;

import com.dennyy.oldschoolcompanion.models.General.Quest;

import java.util.ArrayList;

public abstract class QuestListeners {

    public interface AdapterClickListener {
        void onQuestClick(Quest quest);

        void onQuestDoneClick(Quest quest, boolean isCompleted);
    }

    public interface LoadedListener {
        void onQuestsLoaded(ArrayList<Quest> loadedQuests);

        void onQuestsLoadError();
    }
}
