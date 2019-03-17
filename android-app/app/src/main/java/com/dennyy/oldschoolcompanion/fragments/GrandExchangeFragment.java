package com.dennyy.oldschoolcompanion.fragments;

import android.os.Bundle;
import android.view.*;
import com.dennyy.oldschoolcompanion.R;
import com.dennyy.oldschoolcompanion.helpers.Utils;
import com.dennyy.oldschoolcompanion.interfaces.JsonItemsLoadedListener;
import com.dennyy.oldschoolcompanion.models.GrandExchange.JsonItem;
import com.dennyy.oldschoolcompanion.viewhandlers.GrandExchangeViewHandler;

import java.util.HashMap;

public class GrandExchangeFragment extends BaseFragment {

    public static final String ITEM_ID_KEY = "GE_ITEM_ID";

    private GrandExchangeViewHandler grandExchangeViewHandler;

    public GrandExchangeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        view = inflater.inflate(R.layout.grand_exchange_layout, container, false);
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_ge, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_ge_refresh:
                if (grandExchangeViewHandler.allowUpdateItem())
                    grandExchangeViewHandler.updateItem(grandExchangeViewHandler.getItemId(), false);
                return true;
            case R.id.action_ge_history:
                grandExchangeViewHandler.toggleGeData();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        toolbarTitle.setText(getResources().getString(R.string.grandexchange));

        grandExchangeViewHandler = new GrandExchangeViewHandler(getActivity(), view, false, new JsonItemsLoadedListener() {
            @Override
            public void onJsonItemsLoaded(HashMap<String, JsonItem> ignored) {
                loadFragment(savedInstanceState);
            }

            @Override
            public void onJsonItemsLoadError() {
            }
        });
    }

    private void loadFragment(Bundle savedInstanceState) {
        if (savedInstanceState == null) return;
        String itemId = savedInstanceState.getString(ITEM_ID_KEY);
        if (!Utils.isNullOrEmpty(itemId)) {
            grandExchangeViewHandler.updateItem(itemId, true);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (grandExchangeViewHandler != null) {
            outState.putString(ITEM_ID_KEY, grandExchangeViewHandler.getItemId());
        }
    }

    @Override
    public boolean onBackClick() {
        if (grandExchangeViewHandler.isGeDataVisible()) {
            grandExchangeViewHandler.toggleGeData(false);
            return true;
        }
        return super.onBackClick();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        grandExchangeViewHandler.cancelRunningTasks();
    }
}