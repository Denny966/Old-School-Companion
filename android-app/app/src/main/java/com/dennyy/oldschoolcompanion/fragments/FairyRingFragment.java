package com.dennyy.oldschoolcompanion.fragments;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dennyy.oldschoolcompanion.R;
import com.dennyy.oldschoolcompanion.viewhandlers.FairyRingViewHandler;

public class FairyRingFragment extends BaseFragment {

    private static final String FAIRY_RING_INDEX_KEY = "fairy_ring_index_key";

    private FairyRingViewHandler fairyRingViewHandler;

    public FairyRingFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        view = inflater.inflate(R.layout.fairy_ring_layout, container, false);

        return view;
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        toolbarTitle.setText(getResources().getString(R.string.fairy_rings));

        fairyRingViewHandler = new FairyRingViewHandler(getActivity(), view);
        if (savedInstanceState != null) {
            fairyRingViewHandler.selectedIndex = savedInstanceState.getInt(FAIRY_RING_INDEX_KEY);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        fairyRingViewHandler.cancelRunningTasks();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(FAIRY_RING_INDEX_KEY, fairyRingViewHandler.selectedIndex);
    }
}