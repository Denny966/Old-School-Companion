package com.dennyy.oldschoolcompanion.interfaces;

public interface ItemIdListResultListener {
    void onItemsUpdated();

    void onItemsNotUpdated();

    void onError();
}