package com.dennyy.osrscompanion.fragments;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.dennyy.osrscompanion.R;
import com.dennyy.osrscompanion.enums.GeGraphDays;
import com.dennyy.osrscompanion.layouthandlers.GrandExchangeViewHandler;
import com.dennyy.osrscompanion.models.GrandExchange.JsonItem;

import java.util.ArrayList;

public class GrandExchangeFragment extends BaseFragment {

    // bundle keys & fields
    public static final String JSON_ITEM = "JSON_ITEM";
    public static final String ADAPTER_INDEX = "ADAPTER_INDEX";
    public static final String GE_ITEM_DATA = "GE_ITEM_DATA";
    public static final String GE_UPDATE_DATA = "GE_UPDATE_DATA";
    public static final String GE_GRAPH_DATA = "GE_GRAPH_DATA";
    public static final String GE_GRAPH_SELECTION_DATA = "GE_GRAPH_SELECTION_DATA";
    public static final String GE_SEARCH_ITEM_DATA = "GE_SEARCH_ITEM_DATA";
    public static final String WAS_REQUESTING_GE = "WAS_REQUESTING_GE";
    public static final String WAS_REQUESTING_GEUPDATE = "WAS_REQUESTING_GEUPDATE";
    public static final String WAS_REQUESTING_GEGRAPHS = "WAS_REQUESTING_GEGRAPHS";
    public static final String WAS_REQUESTING_OSBUDDY = "WAS_REQUESTING_OSBUDDY";
    public static final String OSBUDDY_DATA = "OSBUDDY_DATA";


    private GrandExchangeViewHandler grandExchangeViewHandler;
    private View view;

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
                    grandExchangeViewHandler.updateItem();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        toolbarTitle.setText(getResources().getString(R.string.grandexchange));

        grandExchangeViewHandler = new GrandExchangeViewHandler(getActivity(), view, new GrandExchangeViewHandler.ItemsLoadedCallback() {
            @Override
            public void onItemsLoaded(ArrayList<JsonItem> ignored) {
                loadFragment(savedInstanceState);
            }

            @Override
            public void onLoadError() { }
        });
    }

    private void loadFragment(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
            grandExchangeViewHandler.jsonItem = (JsonItem) savedInstanceState.getSerializable(JSON_ITEM);
            grandExchangeViewHandler.selectedAdapterIndex = savedInstanceState.getInt(ADAPTER_INDEX);
            grandExchangeViewHandler.geItemData = savedInstanceState.getString(GE_ITEM_DATA);
            grandExchangeViewHandler.geupdateData = savedInstanceState.getString(GE_UPDATE_DATA);
            grandExchangeViewHandler.geGraphData = savedInstanceState.getString(GE_GRAPH_DATA);
            grandExchangeViewHandler.osBuddyItemData = savedInstanceState.getString(OSBUDDY_DATA);
            grandExchangeViewHandler.searchAdapterItems = (ArrayList<JsonItem>) savedInstanceState.getSerializable(GE_SEARCH_ITEM_DATA);
            grandExchangeViewHandler.currentSelectedDays = GeGraphDays.fromDays(savedInstanceState.getInt(GE_GRAPH_SELECTION_DATA));
            grandExchangeViewHandler.wasRequestingGe = savedInstanceState.getBoolean(WAS_REQUESTING_GE);
            grandExchangeViewHandler.wasRequestingGegraph = savedInstanceState.getBoolean(WAS_REQUESTING_GEGRAPHS);
            grandExchangeViewHandler.wasRequestingGeupdate = savedInstanceState.getBoolean(WAS_REQUESTING_GEUPDATE);
            grandExchangeViewHandler.wasRequestingOsBuddy = savedInstanceState.getBoolean(WAS_REQUESTING_OSBUDDY);
            grandExchangeViewHandler.adapter.updateItems(grandExchangeViewHandler.searchAdapterItems);

            grandExchangeViewHandler.reloadOnOrientationChanged();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(JSON_ITEM, grandExchangeViewHandler.jsonItem);
        outState.putSerializable(GE_ITEM_DATA, grandExchangeViewHandler.geItemData);
        outState.putString(GE_UPDATE_DATA, grandExchangeViewHandler.geupdateData);
        outState.putString(GE_GRAPH_DATA, grandExchangeViewHandler.geGraphData);
        outState.putInt(GE_GRAPH_SELECTION_DATA, grandExchangeViewHandler.currentSelectedDays.getDays());
        outState.putInt(ADAPTER_INDEX, grandExchangeViewHandler.selectedAdapterIndex);
        outState.putSerializable(GE_SEARCH_ITEM_DATA, grandExchangeViewHandler.adapter.getItems());
        outState.putString(OSBUDDY_DATA, grandExchangeViewHandler.osBuddyItemData);
        outState.putBoolean(WAS_REQUESTING_GE, grandExchangeViewHandler.wasRequestingGe);
        outState.putBoolean(WAS_REQUESTING_GEGRAPHS, grandExchangeViewHandler.wasRequestingGegraph);
        outState.putBoolean(WAS_REQUESTING_GEUPDATE, grandExchangeViewHandler.wasRequestingGeupdate);
        outState.putBoolean(WAS_REQUESTING_OSBUDDY, grandExchangeViewHandler.wasRequestingOsBuddy);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        grandExchangeViewHandler.cancelVolleyRequests();
    }
}
