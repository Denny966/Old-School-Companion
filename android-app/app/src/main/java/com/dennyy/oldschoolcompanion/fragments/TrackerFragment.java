package com.dennyy.oldschoolcompanion.fragments;


import android.os.Bundle;
import android.view.*;
import com.dennyy.oldschoolcompanion.R;
import com.dennyy.oldschoolcompanion.viewhandlers.TrackerViewHandler;

public class TrackerFragment extends BaseFragment {

    private TrackerViewHandler trackerViewHandler;

    public TrackerFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        view = inflater.inflate(R.layout.tracker_layout, container, false);
        return view;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        toolbarTitle.setText(getResources().getString(R.string.tracker));

        trackerViewHandler = new TrackerViewHandler(getActivity(), view);
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
