package com.dennyy.oldschoolcompanion.interfaces;

import com.dennyy.oldschoolcompanion.models.GrandExchange.JsonItem;

import java.util.HashMap;

public interface JsonItemsLoadedListener {
    void onJsonItemsLoaded(HashMap<String, JsonItem> items);

    void onJsonItemsLoadError();
}