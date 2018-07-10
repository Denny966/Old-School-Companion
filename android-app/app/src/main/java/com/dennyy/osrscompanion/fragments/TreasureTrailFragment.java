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
import com.dennyy.osrscompanion.helpers.Utils;
import com.dennyy.osrscompanion.layouthandlers.TreasureTrailViewHandler;
import com.dennyy.osrscompanion.models.TreasureTrails.TreasureTrail;

import java.util.ArrayList;

public class TreasureTrailFragment extends BaseFragment {

    public static final String CLUE_ADAPTER_INDEX_KEY = "CLUE_ADAPTER_INDEX_KEY";
    public static final String CLUE_ADAPTER_ITEMS_KEY = "CLUE_ADAPTER_ITEMS_KEY";
    public static final String CLUE_DATA_KEY = "CLUE_DATA_KEY";
    public static final String CLUE_COORDS_KEY = "CLUE_COORDS_KEY";
    public static final String CLUE_USER_LOADED_IMG_KEY = "CLUE_USER_LOADED_IMG_KEY";


    private TreasureTrailViewHandler treasureTrailViewHandler;
    private View view;

    public TreasureTrailFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        view = inflater.inflate(R.layout.clue_scroll_layout, container, false);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_treasure_trails, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_tt_info:
                Utils.showDialog(getActivity(), getActivity().getString(R.string.treasure_trails), getActivity().getResources().getString(R.string.tt_dialog_info));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        toolbarTitle.setText(getResources().getString(R.string.clue_scrolls));

        treasureTrailViewHandler = new TreasureTrailViewHandler(getActivity(), view, new TreasureTrailViewHandler.TreasureTrailsLoadedCallback() {
            @Override
            public void onLoaded(ArrayList<TreasureTrail> ignored) {
                loadFragment(savedInstanceState);
            }

            @Override
            public void onLoadError() {

            }
        });
    }

    private void loadFragment(Bundle savedInstanceState) {
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        if (savedInstanceState != null) {
            ArrayList<TreasureTrail> adapterItems = (ArrayList<TreasureTrail>) savedInstanceState.getSerializable(CLUE_ADAPTER_ITEMS_KEY);
            treasureTrailViewHandler.clueCoords = savedInstanceState.getString(CLUE_COORDS_KEY);
            treasureTrailViewHandler.treasureTrail = (TreasureTrail) savedInstanceState.getSerializable(CLUE_DATA_KEY);
            treasureTrailViewHandler.selectedAdapterIndex = savedInstanceState.getInt(CLUE_ADAPTER_INDEX_KEY);
            treasureTrailViewHandler.adapter.updateItems(adapterItems);
            if (treasureTrailViewHandler.treasureTrail != null) {
                treasureTrailViewHandler.reloadData();
            }
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        treasureTrailViewHandler.cancelVolleyRequests();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(CLUE_COORDS_KEY, treasureTrailViewHandler.clueCoords);
        outState.putSerializable(CLUE_DATA_KEY, treasureTrailViewHandler.treasureTrail);
        outState.putInt(CLUE_ADAPTER_INDEX_KEY, treasureTrailViewHandler.selectedAdapterIndex);
        outState.putSerializable(CLUE_ADAPTER_ITEMS_KEY, treasureTrailViewHandler.adapter.getItems());
    }

}

