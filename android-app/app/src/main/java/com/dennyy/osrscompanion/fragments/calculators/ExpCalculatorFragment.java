package com.dennyy.osrscompanion.fragments.calculators;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dennyy.osrscompanion.R;
import com.dennyy.osrscompanion.fragments.BaseFragment;
import com.dennyy.osrscompanion.layouthandlers.ExpCalculatorViewHandler;

public class ExpCalculatorFragment extends BaseFragment {
    private View view;

    public ExpCalculatorFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        view = inflater.inflate(R.layout.exp_calc_layout, container, false);
        return view;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        toolbarTitle.setText(getResources().getString(R.string.experience_calculator));

        new ExpCalculatorViewHandler(getActivity(), view);
    }
}
