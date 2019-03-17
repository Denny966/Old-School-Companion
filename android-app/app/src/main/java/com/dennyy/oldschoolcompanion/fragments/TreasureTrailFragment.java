package com.dennyy.oldschoolcompanion.fragments;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.dennyy.oldschoolcompanion.R;
import com.dennyy.oldschoolcompanion.interfaces.TreasureTrailsLoadedListener;
import com.dennyy.oldschoolcompanion.models.TreasureTrails.TreasureTrail;
import com.dennyy.oldschoolcompanion.models.TreasureTrails.TreasureTrails;
import com.dennyy.oldschoolcompanion.viewhandlers.TreasureTrailViewHandler;

public class TreasureTrailFragment extends BaseFragment {

    public static final String CLUE_DATA_KEY = "CLUE_DATA_KEY";

    private TreasureTrailViewHandler treasureTrailViewHandler;
    private View view;

    public TreasureTrailFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        view = inflater.inflate(R.layout.treasure_trails_layout, container, false);

        return view;
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        toolbarTitle.setText(getResources().getString(R.string.treasure_trails));

        treasureTrailViewHandler = new TreasureTrailViewHandler(getActivity(), view, false, new TreasureTrailsLoadedListener() {
            @Override
            public void onTreasureTrailsLoaded(TreasureTrails treasureTrails) {
                loadFragment(savedInstanceState);
            }

            @Override
            public void onTreasureTrailsLoadError() {
            }
        });
    }

    private void loadFragment(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            treasureTrailViewHandler.treasureTrail = (TreasureTrail) savedInstanceState.getSerializable(CLUE_DATA_KEY);
            treasureTrailViewHandler.reloadData();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_treasure_trails, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_tt_main:
            case R.id.action_tt_maps:
            case R.id.action_tt_puzzles:
                if (treasureTrailViewHandler != null) {
                    treasureTrailViewHandler.setNavbarIconActive(id, true);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        treasureTrailViewHandler.cancelRunningTasks();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (treasureTrailViewHandler != null) {
            outState.putSerializable(CLUE_DATA_KEY, treasureTrailViewHandler.treasureTrail);
        }
    }

    @Override
    public boolean onBackClick() {
        if (treasureTrailViewHandler != null && treasureTrailViewHandler.expandedImageViewVisible()) {
            treasureTrailViewHandler.hideExpandedImageView();
            return true;
        }
        return super.onBackClick();
    }
}