package com.dennyy.osrscompanion.fragments;

import android.content.res.Configuration;
import android.os.Bundle;

public abstract class BaseTileFragment extends BaseFragment {
    protected abstract void initializeTiles();

    private int portraitColumns;
    private int landscapeColumns;
    protected int currentColumns;

    public BaseTileFragment(int portraitColumns, int landscapeColumns) {
        this.portraitColumns = portraitColumns;
        this.landscapeColumns = landscapeColumns;
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
