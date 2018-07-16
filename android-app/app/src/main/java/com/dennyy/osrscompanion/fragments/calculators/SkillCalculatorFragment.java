package com.dennyy.osrscompanion.fragments.calculators;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.dennyy.osrscompanion.R;
import com.dennyy.osrscompanion.enums.HiscoreType;
import com.dennyy.osrscompanion.fragments.BaseFragment;
import com.dennyy.osrscompanion.helpers.Utils;
import com.dennyy.osrscompanion.layouthandlers.SkillCalculatorViewHandler;

public class SkillCalculatorFragment extends BaseFragment {
    private static final String HISCORE_TYPE_KEY = "hiscore_type_key";
    private static final String HISCORE_DATA_KEY = "hiscore_data_key";
    private static final String SKILL_KEY = "selected_skill_type_key";
    private static final String FROM_LEVEL_KEY = "from_level_key";
    private static final String TO_LEVEL_KEY = "to_level_key";
    private static final String FROM_EXP_KEY = "from_exp_key";
    private static final String TO_EXP_KEY = "to_exp_key";
    private static final String WAS_REQUESTING_KEY = "was_requesting_key";

    private SkillCalculatorViewHandler skillCalculatorViewHandler;
    private View view;


    public SkillCalculatorFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        view = inflater.inflate(R.layout.skill_calculator_layout, container, false);
        return view;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        toolbarTitle.setText(getResources().getString(R.string.skill_calculator));

        skillCalculatorViewHandler = new SkillCalculatorViewHandler(getActivity(), view);
        if (savedInstanceState != null) {
            skillCalculatorViewHandler.selectedHiscoreType = HiscoreType.fromValue(savedInstanceState.getInt(HISCORE_TYPE_KEY));
            skillCalculatorViewHandler.hiscoresData = savedInstanceState.getString(HISCORE_DATA_KEY);
            skillCalculatorViewHandler.selectedSkillId = savedInstanceState.getInt(SKILL_KEY);
            skillCalculatorViewHandler.fromLvl = savedInstanceState.getInt(FROM_LEVEL_KEY);
            skillCalculatorViewHandler.toLvl = savedInstanceState.getInt(TO_LEVEL_KEY);
            skillCalculatorViewHandler.fromExp = savedInstanceState.getInt(FROM_EXP_KEY);
            skillCalculatorViewHandler.toExp = savedInstanceState.getInt(TO_EXP_KEY);
            skillCalculatorViewHandler.setValueToEditText(R.id.current_lvl, skillCalculatorViewHandler.fromLvl);
            skillCalculatorViewHandler.setValueToEditText(R.id.target_lvl, skillCalculatorViewHandler.toLvl);
            skillCalculatorViewHandler.setValueToEditText(R.id.current_exp, skillCalculatorViewHandler.fromExp);
            skillCalculatorViewHandler.setValueToEditText(R.id.target_exp, skillCalculatorViewHandler.toExp);
            skillCalculatorViewHandler.updateIndicators();
            if (savedInstanceState.getBoolean(WAS_REQUESTING_KEY)) {
                skillCalculatorViewHandler.updateUser();
            }
            else if (!Utils.isNullOrEmpty(skillCalculatorViewHandler.hiscoresData)) {
                skillCalculatorViewHandler.handleHiscoresData(skillCalculatorViewHandler.hiscoresData);
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(HISCORE_TYPE_KEY, skillCalculatorViewHandler.selectedHiscoreType.getValue());
        outState.putString(HISCORE_DATA_KEY, skillCalculatorViewHandler.hiscoresData);
        outState.putInt(SKILL_KEY, skillCalculatorViewHandler.selectedSkillId);
        outState.putInt(FROM_LEVEL_KEY, skillCalculatorViewHandler.fromLvl);
        outState.putInt(TO_LEVEL_KEY, skillCalculatorViewHandler.toLvl);
        outState.putInt(FROM_EXP_KEY, skillCalculatorViewHandler.fromExp);
        outState.putInt(TO_EXP_KEY, skillCalculatorViewHandler.toExp);
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
                if (skillCalculatorViewHandler.allowUpdateUser())
                    skillCalculatorViewHandler.updateUser();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        skillCalculatorViewHandler.cancelVolleyRequests();
    }
}
