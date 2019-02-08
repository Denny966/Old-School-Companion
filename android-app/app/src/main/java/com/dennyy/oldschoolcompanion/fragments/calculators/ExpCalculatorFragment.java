package com.dennyy.oldschoolcompanion.fragments.calculators;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dennyy.oldschoolcompanion.R;
import com.dennyy.oldschoolcompanion.fragments.BaseFragment;
import com.dennyy.oldschoolcompanion.viewhandlers.ExpCalculatorViewHandler;

public class ExpCalculatorFragment extends BaseFragment {
    private ExpCalculatorViewHandler expCalculatorViewHandler;

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

        expCalculatorViewHandler = new ExpCalculatorViewHandler(getActivity(), view, false);
    }

    @Override
    public boolean onBackClick() {
        if (expCalculatorViewHandler != null && !expCalculatorViewHandler.inputContainerVisible()) {
            expCalculatorViewHandler.toggleInputContainer(true);
            return true;
        }
        return super.onBackClick();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        expCalculatorViewHandler.cancelRunningTasks();
    }
}
