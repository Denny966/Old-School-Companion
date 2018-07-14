package com.dennyy.osrscompanion.fragments.calculators;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dennyy.osrscompanion.R;
import com.dennyy.osrscompanion.fragments.BaseFragment;
import com.dennyy.osrscompanion.layouthandlers.CalculatorViewHandler;

public class SkillCalculatorFragment extends BaseFragment {
    private static final String CALCANSWER = "CALCANSWER";
    private static final String CALCEQUATION = "CALCEQUATION";
    private static final String CALCLASTNUMERIC = "CALCLASTNUMERIC";
    private static final String CALCLASTDOT = "CALCLASTDOT";
    private static final String CALCSTATEERROR = "CALCSTATEERROR";
    private static final String CALCULATED = "CALCULATED";

    private CalculatorViewHandler calculatorViewHandler;
    private View view;


    public SkillCalculatorFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater,container,savedInstanceState);
        view = inflater.inflate(R.layout.skill_calculator_layout, container, false);
        return view;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        toolbarTitle.setText(getResources().getString(R.string.skill_calculator));

        //   calculatorViewHandler = new CalculatorViewHandler(getActivity(), view);
        if (savedInstanceState != null) {
            // calculatorViewHandler.equation = savedInstanceState.getString(CALCEQUATION);

        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //   outState.putString(CALCANSWER, calculatorViewHandler.answer);

    }
}
