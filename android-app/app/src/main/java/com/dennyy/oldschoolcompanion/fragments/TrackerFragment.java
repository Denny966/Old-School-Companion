package com.dennyy.oldschoolcompanion.fragments;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.dennyy.oldschoolcompanion.R;
import com.dennyy.oldschoolcompanion.enums.TrackDurationType;
import com.dennyy.oldschoolcompanion.viewhandlers.TrackerViewHandler;
import com.dennyy.oldschoolcompanion.models.Tracker.TrackData;

import java.util.HashMap;

public class TrackerFragment extends BaseFragment {

    private final String TRACK_DATA_KEY = "TRACKDATAKEY";
    private final String TRACK_RSN_KEY = "TRACKRSNKEY";
    private final String TRACK_PERIOD_KEY = "TRACKPERIODKEY";
    private final String WASREQUESTING = "wasrequestingtrack";

    private TrackerViewHandler trackerViewHandler;
    private View view;

    public TrackerFragment() {
        // Required empty public constructor
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(TRACK_DATA_KEY, trackerViewHandler.trackData);
        outState.putSerializable(TRACK_RSN_KEY, defaultRsn);
        outState.putInt(TRACK_PERIOD_KEY, trackerViewHandler.durationType.getValue());
        outState.putBoolean(WASREQUESTING, trackerViewHandler.wasRequesting());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.tracker_layout, container, false);
        return view;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        toolbarTitle.setText(getResources().getString(R.string.tracker));
        trackerViewHandler = new TrackerViewHandler(getActivity(), view);
        if (savedInstanceState == null) {
            return;
        }

        trackerViewHandler.trackData = (HashMap<TrackDurationType, TrackData>) savedInstanceState.getSerializable(TRACK_DATA_KEY);
        trackerViewHandler.durationType = TrackDurationType.fromValue(savedInstanceState.getInt(TRACK_PERIOD_KEY));
        if (savedInstanceState.getBoolean(WASREQUESTING)) {
            trackerViewHandler.updateUser();
        }
        else if (trackerViewHandler.trackData != null && !trackerViewHandler.trackData.isEmpty() && trackerViewHandler.durationType != null) {
            trackerViewHandler.updateIndicators();
            getActivity().findViewById(R.id.tracker_data_layout).setVisibility(View.VISIBLE);
            TrackData trackData = trackerViewHandler.trackData.get(trackerViewHandler.durationType);
            if (trackData != null) {
                trackerViewHandler.handleTrackData(trackData.data);
            }
        }

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_tracker, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_tracker_refresh:
                if (trackerViewHandler.allowUpdateUser())
                    trackerViewHandler.updateUser();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        trackerViewHandler.cancelRunningTasks();
    }
}
