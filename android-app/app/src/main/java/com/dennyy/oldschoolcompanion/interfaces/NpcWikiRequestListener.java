package com.dennyy.oldschoolcompanion.interfaces;

public interface NpcWikiRequestListener {
    void onWikiRequestStart();
    void onWikiRequestError(Exception e);
    void onWikiRequestEnd(boolean hasResults);
}
