package com.dennyy.oldschoolcompanion.fragments.calculators;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dennyy.oldschoolcompanion.R;
import com.dennyy.oldschoolcompanion.enums.HiscoreType;
import com.dennyy.oldschoolcompanion.fragments.BaseFragment;
import com.dennyy.oldschoolcompanion.helpers.Utils;
import com.dennyy.oldschoolcompanion.viewhandlers.SkillCalculatorViewHandler;

public class SkillCalculatorFragment extends BaseFragment {
    private static final String HISCORE_TYPE_KEY = "hiscore_type_key";
    private static final String HISCORE_DATA_KEY = "hiscore_data_key";
    private static final String SKILL_KEY = "selected_skill_type_key";
    private static final String FROM_LEVEL_KEY = "from_level_key";
    private static final String TO_LEVEL_KEY = "to_level_key";
    private static final String FROM_EXP_KEY = "from_exp_key";
    private static final String TO_EXP_KEY = "to_exp_key";
    private static final String CUSTOM_EXP_KEY = "custom_exp_key";
    private static final String WAS_REQUESTING_KEY = "was_requesting_key";

    private SkillCalculatorViewHandler skillCalculatorViewHandler;

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

        skillCalculatorViewHandler = new SkillCalculatorViewHandler(getActivity(), view, false);
        if (savedInstanceState != null) {
            skillCalculatorViewHandler.selectedHiscoreType = HiscoreType.fromValue(savedInstanceState.getInt(HISCORE_TYPE_KEY));
            skillCalculatorViewHandler.hiscoresData = savedInstanceState.getString(HISCORE_DATA_KEY);
            skillCalculatorViewHandler.selectedSkillId = savedInstanceState.getInt(SKILL_KEY);
            skillCalculatorViewHandler.fromLvl = savedInstanceState.getInt(FROM_LEVEL_KEY);
            skillCalculatorViewHandler.toLvl = savedInstanceState.getInt(TO_LEVEL_KEY);
            skillCalculatorViewHandler.fromExp = savedInstanceState.getInt(FROM_EXP_KEY);
            skillCalculatorViewHandler.toExp = savedInstanceState.getInt(TO_EXP_KEY);
            skillCalculatorViewHandler.customExp = savedInstanceState.getInt(CUSTOM_EXP_KEY);
            skillCalculatorViewHandler.setValueToEditText(R.id.current_lvl, skillCalculatorViewHandler.fromLvl);
            skillCalculatorViewHandler.setValueToEditText(R.id.target_lvl, skillCalculatorViewHandler.toLvl);
            skillCalculatorViewHandler.setValueToEditText(R.id.current_exp, skillCalculatorViewHandler.fromExp);
            skillCalculatorViewHandler.setValueToEditText(R.id.target_exp, skillCalculatorViewHandler.toExp);
            skillCalculatorViewHandler.setValueToEditText(R.id.custom_exp, skillCalculatorViewHandler.customExp);
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
        if (skillCalculatorViewHandler != null) {
            outState.putInt(HISCORE_TYPE_KEY, skillCalculatorViewHandler.selectedHiscoreType.getValue());
            outState.putString(HISCORE_DATA_KEY, skillCalculatorViewHandler.hiscoresData);
            outState.putInt(SKILL_KEY, skillCalculatorViewHandler.selectedSkillId);
            outState.putInt(FROM_LEVEL_KEY, skillCalculatorViewHandler.fromLvl);
            outState.putInt(TO_LEVEL_KEY, skillCalculatorViewHandler.toLvl);
            outState.putInt(FROM_EXP_KEY, skillCalculatorViewHandler.fromExp);
            outState.putInt(TO_EXP_KEY, skillCalculatorViewHandler.toExp);
            outState.putInt(CUSTOM_EXP_KEY, skillCalculatorViewHandler.customExp);
        }
    }

    @Override
    public boolean onBackClick() {
        if (skillCalculatorViewHandler != null && !skillCalculatorViewHandler.inputContainerVisible()) {
            skillCalculatorViewHandler.toggleInputContainer(true);
            return true;
        }
        return super.onBackClick();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        skillCalculatorViewHandler.cancelRunningTasks();
    }
}
