package com.dennyy.oldschoolcompanion.fragments.calculators;

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
import com.dennyy.oldschoolcompanion.viewhandlers.CombatCalculatorViewHandler;

public class CombatCalculatorFragment extends BaseFragment {

    private static final String HISCORES_DATA_KEY = "hiscores_data_key";
    private static final String HISCORES_RSN_DATA_KEY = "hiscores_rsn_data_key";
    private static final String HISCORES_TYPE_KEY = "hiscores_type_key";
    private static final String HISCORES_WAS_REQUESTING_KEY = "hiscores_wasrequesting_key";

    private CombatCalculatorViewHandler combatCalculatorViewHandler;

    public CombatCalculatorFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        view = inflater.inflate(R.layout.combat_calculator_layout, container, false);
        return view;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        toolbarTitle.setText(getResources().getString(R.string.combat_calculator));

        combatCalculatorViewHandler = new CombatCalculatorViewHandler(getActivity(), view);
        if (savedInstanceState != null) {
            combatCalculatorViewHandler.hiscoresData = savedInstanceState.getString(HISCORES_DATA_KEY);
            combatCalculatorViewHandler.selectedRsn = savedInstanceState.getString(HISCORES_RSN_DATA_KEY);
            combatCalculatorViewHandler.selectedHiscoreType = HiscoreType.fromValue(savedInstanceState.getInt(HISCORES_TYPE_KEY));
            if (savedInstanceState.getBoolean(HISCORES_WAS_REQUESTING_KEY)) {
                combatCalculatorViewHandler.updateUser();
            }

            else if (combatCalculatorViewHandler.hiscoresData != null) {
                view.findViewById(R.id.cmb_calc_data_layout).setVisibility(View.VISIBLE);
                combatCalculatorViewHandler.handleHiscoresData(combatCalculatorViewHandler.hiscoresData);
            }
            combatCalculatorViewHandler.updateIndicators();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (combatCalculatorViewHandler != null) {
            outState.putString(HISCORES_DATA_KEY, combatCalculatorViewHandler.hiscoresData);
            outState.putString(HISCORES_RSN_DATA_KEY, combatCalculatorViewHandler.selectedRsn);
            outState.putInt(HISCORES_TYPE_KEY, combatCalculatorViewHandler.selectedHiscoreType.getValue());
            outState.putBoolean(HISCORES_WAS_REQUESTING_KEY, combatCalculatorViewHandler.wasRequesting());
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_refresh_only, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                if (combatCalculatorViewHandler.allowUpdateUser())
                    combatCalculatorViewHandler.updateUser();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        combatCalculatorViewHandler.cancelRunningTasks();
    }
}
