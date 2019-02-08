package com.dennyy.oldschoolcompanion.interfaces;

import com.dennyy.oldschoolcompanion.models.OSBuddy.OSBuddySummaryItem;

import java.util.HashMap;

public interface OSBuddySummaryLoadedListener {
    void onOsBuddySummaryLoaded(HashMap<String, OSBuddySummaryItem> content, long dateModified, boolean cacheExpired);

    void onOsBuddySummaryContextError();

    void onOsBuddySummaryLoadFailed(Exception ex);
}
