package com.dennyy.osrscompanion.fragments.calculators;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.dennyy.osrscompanion.R;
import com.dennyy.osrscompanion.adapters.TileAdapter;
import com.dennyy.osrscompanion.fragments.BaseTileFragment;
import com.dennyy.osrscompanion.models.Home.TileData;

import java.util.ArrayList;

public class CalculatorsFragment extends BaseTileFragment implements AdapterView.OnItemClickListener {
    private View view;
    private ArrayList<TileData> calculatorTiles;

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
        calculatorTiles = new ArrayList<>();
        calculatorTiles.add(new TileData(getString(R.string.math_calculator), getDrawable(R.drawable.calculator)));
        calculatorTiles.add(new TileData(getString(R.string.combat_calculator), getDrawable(R.drawable.combat)));
        calculatorTiles.add(new TileData(getString(R.string.skill_calculator), getDrawable(R.drawable.stats)));

        TileAdapter tileAdapter = new TileAdapter(getActivity(), calculatorTiles);
        GridView gridView = view.findViewById(R.id.calculators_grid_layout);
        gridView.setNumColumns(currentColumns);
        gridView.setAdapter(tileAdapter);
        gridView.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        TileData tileData = this.calculatorTiles.get(i);
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        Fragment fragment = null;
        String tag = "";
        if (tileData.text.equals(getString(R.string.math_calculator))) {
            fragment = new MathCalculatorFragment();
        }
        if (tileData.text.equals(getString(R.string.combat_calculator))) {
            fragment = new CombatCalculatorFragment();
        }
        if (tileData.text.equals(getString(R.string.skill_calculator))) {
            //  fragment = new SkillCalculatorFragment();
            showToast(getString(R.string.coming_soon), Toast.LENGTH_SHORT);
        }

        transaction.replace(R.id.fragment_container, fragment, tag);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}