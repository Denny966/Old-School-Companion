package com.dennyy.oldschoolcompanion.interfaces;

import java.util.ArrayList;

public abstract class BestiaryListeners {

    public interface GetHistoryListener {
        void onBestiaryHistoryLoaded(ArrayList<String> monsters);
    }

    public interface BestiaryAdapterListener {
        void onClickMonsterName(String name);
    }
}
