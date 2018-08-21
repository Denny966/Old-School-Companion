package com.dennyy.osrscompanion.fragments;

import android.content.res.Configuration;
import android.os.Bundle;

import com.dennyy.osrscompanion.models.General.TileData;

import java.util.ArrayList;

public abstract class BaseTileFragment extends BaseFragment {
    protected abstract void initializeTiles();

    private int portraitColumns;
    private int landscapeColumns;
    protected int currentColumns;
    protected ArrayList<TileData> tiles;

    public BaseTileFragment(int portraitColumns, int landscapeColumns) {
        this.portraitColumns = portraitColumns;
        this.landscapeColumns = landscapeColumns;
        this.tiles = new ArrayList<>();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        int currentOrientation = getResources().getConfiguration().orientation;
        if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            currentColumns = landscapeColumns;
        }
        else {
            currentColumns = portraitColumns;
        }
    }
}
