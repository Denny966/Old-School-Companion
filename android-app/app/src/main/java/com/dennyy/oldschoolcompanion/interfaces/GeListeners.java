package com.dennyy.oldschoolcompanion.interfaces;

import com.dennyy.oldschoolcompanion.models.GrandExchange.GeHistory;
import com.dennyy.oldschoolcompanion.models.GrandExchange.GrandExchangeData;
import com.dennyy.oldschoolcompanion.models.GrandExchange.GrandExchangeUpdateData;
import com.dennyy.oldschoolcompanion.models.GrandExchange.ItemData;

public class GeListeners {
    private GeListeners() {
    }

    public interface GeHistoryLoadedListener {
        void onGeHistoryLoaded(GeHistory geHistory);

        void onGeHistoryLoadFailed();
    }

    public interface ItemDataLoadedListener {
        void onGeItemDataLoaded(GrandExchangeData grandExchangeData, boolean isCacheExpired);

        void onGeItemDataContextError();

        void onGeItemDataLoadFailed();
    }

    public interface CompleteItemDataLoadedListener {
        void onItemDataLoaded(ItemData itemData);

        void onItemDataLoadFailed();
    }

    public interface GeUpdateLoadedListener {
        void onGeUpdateLoaded(GrandExchangeUpdateData grandExchangeUpdateData, boolean cacheExpired);

        void onGeUpdateContextError();

        void onGeUpdateLoadFailed();
    }


    public interface GraphDataLoadedListener {
        void onGeGraphDataLoaded(String graphData, boolean isCacheExpired);

        void onGeGraphDataContextError();

        void onGeGraphDataLoadFailed();
    }
}
