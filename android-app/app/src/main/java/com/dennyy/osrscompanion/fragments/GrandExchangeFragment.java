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

public class GrandExchangeFragment extends BaseFragment implements GrandExchangeViewHandler.ItemsLoadedCallback {

    // bundle keys & fields
    public static final String JSONITEM = "JSONITEM";
    public static final String ADAPTERINDEX = "ADAPTERINDEX";
    public static final String GEITEMDATA = "GEITEMDATA";
    public static final String GEUPDATEDATA = "GEUPDATEDATA";
    public static final String GEGRAPHDATA = "GEGRAPHDATA";
    public static final String GEGRAPHSELECTIONDATA = "GEGRAPHSELECTIONDATA";
    public static final String GESEARCHITEMDATA = "GESEARCHITEMDATA";
    public static final String WASREQUESTING = "WASREQUESTING";
    public static final String OSBUDDYDATA = "OSBUDDYDATA";


    private GrandExchangeViewHandler grandExchangeViewHandler;
    private View view;

    public GrandExchangeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(JSONITEM, grandExchangeViewHandler.jsonItem);
        outState.putSerializable(GEITEMDATA, grandExchangeViewHandler.geItemData);
        outState.putString(GEUPDATEDATA, grandExchangeViewHandler.geupdateData);
        outState.putString(GEGRAPHDATA, grandExchangeViewHandler.geGraphData);
        outState.putInt(GEGRAPHSELECTIONDATA, grandExchangeViewHandler.currentSelectedDays.getDays());
        outState.putInt(ADAPTERINDEX, grandExchangeViewHandler.selectedAdapterIndex);
        outState.putSerializable(GESEARCHITEMDATA, grandExchangeViewHandler.adapter.getItems());
        outState.putBoolean(WASREQUESTING, grandExchangeViewHandler.wasRequesting());
        outState.putString(OSBUDDYDATA, grandExchangeViewHandler.osBuddyItemData);
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
        grandExchangeViewHandler = new GrandExchangeViewHandler(getActivity(), view);
        grandExchangeViewHandler.setItemsLoadedCallback(new GrandExchangeViewHandler.ItemsLoadedCallback() {
            @Override
            public void onItemsLoaded(ArrayList<JsonItem> ignored) {
                loadFragment(savedInstanceState);
            }

            @Override
            public void onLoadError() {

            }
        });
    }

    private void loadFragment(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
            grandExchangeViewHandler.jsonItem = (JsonItem) savedInstanceState.getSerializable(JSONITEM);
            grandExchangeViewHandler.selectedAdapterIndex = savedInstanceState.getInt(ADAPTERINDEX);
            grandExchangeViewHandler.geItemData = savedInstanceState.getString(GEITEMDATA);
            grandExchangeViewHandler.geupdateData = savedInstanceState.getString(GEUPDATEDATA);
            grandExchangeViewHandler.geGraphData = savedInstanceState.getString(GEGRAPHDATA);
            grandExchangeViewHandler.osBuddyItemData = savedInstanceState.getString(OSBUDDYDATA);
            grandExchangeViewHandler.searchAdapterItems = (ArrayList<JsonItem>) savedInstanceState.getSerializable(GESEARCHITEMDATA);
            grandExchangeViewHandler.currentSelectedDays = GeGraphDays.fromDays(savedInstanceState.getInt(GEGRAPHSELECTIONDATA));
            grandExchangeViewHandler.adapter.updateItems(grandExchangeViewHandler.searchAdapterItems);
            if (savedInstanceState.getBoolean(WASREQUESTING)) {
                grandExchangeViewHandler.updateItem();
            }
            else if (grandExchangeViewHandler.geItemData != null && grandExchangeViewHandler.geupdateData != null && grandExchangeViewHandler.geGraphData != null) {
                grandExchangeViewHandler.reloadData();
            }
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        grandExchangeViewHandler.cancelVolleyRequests();
    }

    @Override
    public void onItemsLoaded(ArrayList<JsonItem> items) {

    }

    @Override
    public void onLoadError() {

    }
}
