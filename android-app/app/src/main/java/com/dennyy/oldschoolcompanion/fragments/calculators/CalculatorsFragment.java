package com.dennyy.oldschoolcompanion.fragments.calculators;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.*;
import com.dennyy.oldschoolcompanion.R;
import com.dennyy.oldschoolcompanion.fragments.BaseTileFragment;
import com.dennyy.oldschoolcompanion.interfaces.AdapterTileClickListener;
import com.dennyy.oldschoolcompanion.models.General.TileData;

public class CalculatorsFragment extends BaseTileFragment implements AdapterTileClickListener {

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

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        toolbarTitle.setText(getResources().getString(R.string.calculators));
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        inflater.inflate(R.menu.menu_tile, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    protected void initializeTiles() {
        if (tiles.isEmpty()) {
            tiles.add(new TileData(200, getString(R.string.experience_calculator), getDrawable(R.drawable.exp_lamp)));
            tiles.add(new TileData(201, getString(R.string.math_calculator), getDrawable(R.drawable.calculator)));
            tiles.add(new TileData(202, getString(R.string.combat_calculator), getDrawable(R.drawable.combat)));
            tiles.add(new TileData(203, getString(R.string.skill_calculator), getDrawable(R.drawable.stats)));
            tiles.add(new TileData(204, getString(R.string.diary_calculator), getDrawable(R.drawable.diary_icon)));
        }
    }

    @Override
    protected void initializeGridView() {
        gridView = view.findViewById(R.id.calculators_grid_layout);
    }

    @Override
    public void onTileClick(TileData tileData) {
        if (!isTransactionSafe()) {
            return;
        }
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