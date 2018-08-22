package com.dennyy.osrscompanion.fragments;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.dennyy.osrscompanion.R;
import com.dennyy.osrscompanion.layouthandlers.FairyRingViewHandler;

public class FairyRingFragment extends BaseFragment {

    private static final String FAIRY_RING_INDEX_KEY = "fairy_ring_index_key";

    private FairyRingViewHandler fairyRingViewHandler;
    private View view;


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
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        if (savedInstanceState != null) {
            fairyRingViewHandler.selectedIndex = savedInstanceState.getInt(FAIRY_RING_INDEX_KEY);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        fairyRingViewHandler.cancelVolleyRequests();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(FAIRY_RING_INDEX_KEY, fairyRingViewHandler.selectedIndex);
    }
}