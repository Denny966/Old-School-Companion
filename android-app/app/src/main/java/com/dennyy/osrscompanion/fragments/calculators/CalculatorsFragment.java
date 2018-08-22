package com.dennyy.osrscompanion.fragments.calculators;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.dennyy.osrscompanion.R;
import com.dennyy.osrscompanion.adapters.TileAdapter;
import com.dennyy.osrscompanion.fragments.BaseTileFragment;
import com.dennyy.osrscompanion.models.General.TileData;

public class CalculatorsFragment extends BaseTileFragment implements AdapterView.OnItemClickListener {
    private View view;

    public CalculatorsFragment() {
        super(2, 4);
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        view = inflater.inflate(R.layout.calculators_layout, container, false);
        return view;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        toolbarTitle.setText(getResources().getString(R.string.calculators));
        initializeTiles();
    }

    @Override
    protected void initializeTiles() {
        if (tiles.isEmpty()) {
            tiles.add(new TileData(getString(R.string.experience_calculator), getDrawable(R.drawable.exp_lamp)));
            tiles.add(new TileData(getString(R.string.math_calculator), getDrawable(R.drawable.calculator)));
            tiles.add(new TileData(getString(R.string.combat_calculator), getDrawable(R.drawable.combat)));
            tiles.add(new TileData(getString(R.string.skill_calculator), getDrawable(R.drawable.stats)));
            tiles.add(new TileData(getString(R.string.diary_calculator), getDrawable(R.drawable.diary_icon)));
        }

        GridView gridView = view.findViewById(R.id.calculators_grid_layout);
        TileAdapter tileAdapter = new TileAdapter(getActivity(), tiles);
        gridView.setNumColumns(currentColumns);
        gridView.setAdapter(tileAdapter);
        gridView.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        TileData tileData = tiles.get(i);
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        Fragment fragment = null;
        String tag = "";
        if (tileData.text.equals(getString(R.string.math_calculator))) {
            fragment = new MathCalculatorFragment();
        }
        else if (tileData.text.equals(getString(R.string.combat_calculator))) {
            fragment = new CombatCalculatorFragment();
        }
        else if (tileData.text.equals(getString(R.string.experience_calculator))) {
            fragment = new ExpCalculatorFragment();
        }
        else if (tileData.text.equals(getString(R.string.skill_calculator))) {
            fragment = new SkillCalculatorFragment();
        }
        else if (tileData.text.equals(getString(R.string.diary_calculator))) {
            fragment = new DiaryCalculatorFragment();
        }

        transaction.replace(R.id.fragment_container, fragment, tag);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}