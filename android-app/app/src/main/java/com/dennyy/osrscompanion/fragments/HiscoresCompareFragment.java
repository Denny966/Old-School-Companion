package com.dennyy.osrscompanion.fragments;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.dennyy.osrscompanion.R;
import com.dennyy.osrscompanion.enums.CompareMode;
import com.dennyy.osrscompanion.enums.HiscoreMode;
import com.dennyy.osrscompanion.helpers.Utils;
import com.dennyy.osrscompanion.layouthandlers.HiscoresCompareViewHandler;
import com.dennyy.osrscompanion.models.Hiscores.UserStats;

public class HiscoresCompareFragment extends BaseFragment {

    private static final String COMPARE_P1_DATA_KEY = "COMAPREP1DATAKEY";
    private static final String COMPARE_P2_DATA_KEY = "COMAPREP2DATAKEY";
    private static final String COMPARE_HISCORE_TYPE_KEY = "COMAPREP1DATADAY";
    private static final String COMPARE_TYPE_KEY = "COMPARETYPEKEY";
    private static final String COMPARE_WAS_REQUESTING_KEY = "COMPARETYPEKEY";


    private HiscoresCompareViewHandler hiscoresCompareViewHandler;
    private View view;


    public HiscoresCompareFragment() {
        // Required empty public constructor
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(COMPARE_P1_DATA_KEY, hiscoresCompareViewHandler.playerOneStats);
        outState.putSerializable(COMPARE_P2_DATA_KEY, hiscoresCompareViewHandler.playerTwoStats);
        outState.putInt(COMPARE_HISCORE_TYPE_KEY, hiscoresCompareViewHandler.selectedHiscore.getValue());
        outState.putInt(COMPARE_TYPE_KEY, hiscoresCompareViewHandler.selectedComparison.getValue());
        outState.putBoolean(COMPARE_WAS_REQUESTING_KEY, hiscoresCompareViewHandler.wasRequesting());
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        toolbarTitle.setText(getResources().getString(R.string.hiscores));

        hiscoresCompareViewHandler = new HiscoresCompareViewHandler(getActivity(), view);
        if (savedInstanceState != null) {
            hiscoresCompareViewHandler.playerOneStats = (UserStats) savedInstanceState.getSerializable(COMPARE_P1_DATA_KEY);
            hiscoresCompareViewHandler.playerTwoStats = (UserStats) savedInstanceState.getSerializable(COMPARE_P2_DATA_KEY);
            hiscoresCompareViewHandler.selectedHiscore = HiscoreMode.fromValue(savedInstanceState.getInt(COMPARE_HISCORE_TYPE_KEY));
            hiscoresCompareViewHandler.selectedComparison = CompareMode.fromValue(savedInstanceState.getInt(COMPARE_TYPE_KEY));
            if (savedInstanceState.getBoolean(COMPARE_WAS_REQUESTING_KEY)) {
                hiscoresCompareViewHandler.getPlayerOneStats();
            }
            else if (hiscoresCompareViewHandler.playerOneStats != null && hiscoresCompareViewHandler.playerTwoStats != null) {
                hiscoresCompareViewHandler.handleHiscoresData(hiscoresCompareViewHandler.playerOneStats.rsn, hiscoresCompareViewHandler.playerOneStats.stats, hiscoresCompareViewHandler.playerTwoStats.rsn, hiscoresCompareViewHandler.playerTwoStats.stats);
                hiscoresCompareViewHandler.updateIndicators();
                getActivity().findViewById(R.id.hiscores_compare_data_layout).setVisibility(View.VISIBLE);
            }
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.hiscores_compare_layout, container, false);


        return view;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_compare_hiscores, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        hiscoresCompareViewHandler.cancelVolleyRequests();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_compare_hiscores_refresh:
                if (hiscoresCompareViewHandler.allowUpdateUser()) {
                    Utils.hideKeyboard(getActivity(), getActivity().findViewById(R.id.hiscores_compare_rsn_2));
                    hiscoresCompareViewHandler.getPlayerOneStats();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


}
