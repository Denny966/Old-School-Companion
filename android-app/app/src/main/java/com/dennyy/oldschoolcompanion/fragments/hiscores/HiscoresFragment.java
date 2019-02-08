package com.dennyy.oldschoolcompanion.fragments.hiscores;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.*;
import com.dennyy.oldschoolcompanion.R;
import com.dennyy.oldschoolcompanion.fragments.BaseTileFragment;
import com.dennyy.oldschoolcompanion.interfaces.AdapterTileClickListener;
import com.dennyy.oldschoolcompanion.models.General.TileData;

public class HiscoresFragment extends BaseTileFragment implements AdapterTileClickListener {

    public HiscoresFragment() {
        super(2, 4);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        view = inflater.inflate(R.layout.hiscores_layout, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        toolbarTitle.setText(getResources().getString(R.string.hiscores));
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        inflater.inflate(R.menu.menu_tile, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    protected void initializeTiles() {
        if (tiles.isEmpty()) {
            tiles.add(new TileData(100, getString(R.string.hiscore_lookup), getDrawable(R.drawable.hiscores)));
            tiles.add(new TileData(101, getString(R.string.hiscore_compare), getDrawable(R.drawable.hiscores_compare)));
        }
    }

    @Override
    protected void initializeGridView() {
        gridView = view.findViewById(R.id.hiscores_grid_layout);
    }

    @Override
    public void onTileClick(TileData tileData) {
        if (!isTransactionSafe()) {
            return;
        }
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        Fragment fragment = null;
        String tag = "";
        if (tileData.text.equals(getString(R.string.hiscore_lookup))) {
            fragment = new HiscoresLookupFragment();
        }
        else if (tileData.text.equals(getString(R.string.hiscore_compare))) {
            fragment = new HiscoresCompareFragment();
        }

        transaction.replace(R.id.fragment_container, fragment, tag);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
