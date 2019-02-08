package com.dennyy.oldschoolcompanion.fragments.hiscores;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.dennyy.oldschoolcompanion.R;
import com.dennyy.oldschoolcompanion.enums.HiscoreType;
import com.dennyy.oldschoolcompanion.fragments.BaseFragment;
import com.dennyy.oldschoolcompanion.viewhandlers.HiscoresLookupViewHandler;


public class HiscoresLookupFragment extends BaseFragment {

    private static final String HISCORES_DATA_KEY = "hiscoresdatakey";
    private static final String HISCORES_RSN_DATA_KEY = "hiscoresrsndatakey";
    private static final String HISCORES_TYPE_KEY = "hiscorestypekey";
    private static final String HISCORES_WAS_REQUESTING_KEY = "hiscoreswasrequestingkey";

    private HiscoresLookupViewHandler hiscoresLookupViewHandler;

    public HiscoresLookupFragment() {
        // Required empty public constructor
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(HISCORES_DATA_KEY, hiscoresLookupViewHandler.hiscoresData);
        outState.putSerializable(HISCORES_RSN_DATA_KEY, defaultRsn);
        outState.putInt(HISCORES_TYPE_KEY, hiscoresLookupViewHandler.selectedHiscore.getValue());
        outState.putBoolean(HISCORES_WAS_REQUESTING_KEY, hiscoresLookupViewHandler.wasRequesting());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        view = inflater.inflate(R.layout.hiscores_lookup_layout, container, false);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        toolbarTitle.setText(getResources().getString(R.string.hiscore_lookup));
        hiscoresLookupViewHandler = new HiscoresLookupViewHandler(getActivity(), view);
        if (savedInstanceState != null) {
            defaultRsn = savedInstanceState.getString(HISCORES_RSN_DATA_KEY);
            hiscoresLookupViewHandler.hiscoresData = savedInstanceState.getString(HISCORES_DATA_KEY);
            hiscoresLookupViewHandler.selectedHiscore = HiscoreType.fromValue(savedInstanceState.getInt(HISCORES_TYPE_KEY));
            if (savedInstanceState.getBoolean(HISCORES_WAS_REQUESTING_KEY)) {
                hiscoresLookupViewHandler.updateUser();
            }

            else if (hiscoresLookupViewHandler.hiscoresData != null) {
                getActivity().findViewById(R.id.hiscores_data_layout).setVisibility(View.VISIBLE);
                hiscoresLookupViewHandler.handleHiscoresData(hiscoresLookupViewHandler.hiscoresData);
                hiscoresLookupViewHandler.updateIndicators();
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_hiscores, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_hiscores_refresh:
                if (hiscoresLookupViewHandler.allowUpdateUser())
                    hiscoresLookupViewHandler.updateUser();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        hiscoresLookupViewHandler.cancelRunningTasks();
    }
}