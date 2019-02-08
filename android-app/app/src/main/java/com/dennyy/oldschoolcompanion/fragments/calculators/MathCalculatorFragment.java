package com.dennyy.oldschoolcompanion.fragments.calculators;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dennyy.oldschoolcompanion.R;
import com.dennyy.oldschoolcompanion.fragments.BaseFragment;
import com.dennyy.oldschoolcompanion.viewhandlers.CalculatorViewHandler;

public class MathCalculatorFragment extends BaseFragment {

    private static final String CALCANSWER = "CALCANSWER";
    private static final String CALCEQUATION = "CALCEQUATION";
    private static final String CALCLASTNUMERIC = "CALCLASTNUMERIC";
    private static final String CALCLASTDOT = "CALCLASTDOT";
    private static final String CALCSTATEERROR = "CALCSTATEERROR";
    private static final String CALCULATED = "CALCULATED";

    private CalculatorViewHandler calculatorViewHandler;

    public MathCalculatorFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater,container,savedInstanceState);
        view = inflater.inflate(R.layout.calculator_layout, container, false);
        return view;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        toolbarTitle.setText(getResources().getString(R.string.math_calculator));

        calculatorViewHandler = new CalculatorViewHandler(getActivity(), view);
        if (savedInstanceState != null) {
            calculatorViewHandler.equation = savedInstanceState.getString(CALCEQUATION);
            calculatorViewHandler.answer = savedInstanceState.getString(CALCANSWER);
            calculatorViewHandler.lastNumeric = savedInstanceState.getBoolean(CALCLASTNUMERIC);
            calculatorViewHandler.stateError = savedInstanceState.getBoolean(CALCSTATEERROR);
            calculatorViewHandler.lastDot = savedInstanceState.getBoolean(CALCLASTDOT);
            calculatorViewHandler.calculated = savedInstanceState.getBoolean(CALCULATED);
            calculatorViewHandler.reloadData();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(CALCANSWER, calculatorViewHandler.answer);
        outState.putString(CALCEQUATION, calculatorViewHandler.equation);
        outState.putBoolean(CALCLASTNUMERIC, calculatorViewHandler.lastNumeric);
        outState.putBoolean(CALCSTATEERROR, calculatorViewHandler.stateError);
        outState.putBoolean(CALCLASTDOT, calculatorViewHandler.lastDot);
        outState.putBoolean(CALCULATED, calculatorViewHandler.calculated);
    }
}