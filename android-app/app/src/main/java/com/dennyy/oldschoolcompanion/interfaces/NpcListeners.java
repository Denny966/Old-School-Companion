package com.dennyy.oldschoolcompanion.interfaces;

public class NpcListeners {
    private NpcListeners() {
    }

    public interface UpdateListener {
        void onActionFinished();
    }

    public interface NpcLoadedListener {
        void onNpcLoaded(String npcName, String data);

        void onNpcLoadFailed(String npcName);
    }
}
