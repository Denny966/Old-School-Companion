package com.dennyy.oldschoolcompanion.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.dennyy.oldschoolcompanion.R;
import com.dennyy.oldschoolcompanion.viewhandlers.AlchOverviewViewHandler;

public class AlchOverviewFragment extends BaseFragment {
    private static final String NPC_NAME_KEY = "npc_name_key";

    private AlchOverviewViewHandler alchOverviewViewHandler;

    public AlchOverviewFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        view = inflater.inflate(R.layout.alch_overview_layout, container, false);
        return view;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        toolbarTitle.setText(getResources().getString(R.string.alch_overview));

        alchOverviewViewHandler = new AlchOverviewViewHandler(getActivity(), view, false);
        if (savedInstanceState != null) {
        }
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

    }

    @Override
    public void onDetach() {
        super.onDetach();
        alchOverviewViewHandler.cancelRunningTasks();
    }
}