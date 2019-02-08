package com.dennyy.oldschoolcompanion.fragments;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.widget.Toast;
import com.dennyy.oldschoolcompanion.R;
import com.dennyy.oldschoolcompanion.adapters.DragTileAdapter;
import com.dennyy.oldschoolcompanion.helpers.GridSpacingItemDecoration;
import com.dennyy.oldschoolcompanion.helpers.Utils;
import com.dennyy.oldschoolcompanion.interfaces.AdapterTileClickListener;
import com.dennyy.oldschoolcompanion.models.General.TileData;
import com.dennyy.oldschoolcompanion.models.General.Tiles;
import com.woxthebox.draglistview.DragListView;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import static com.dennyy.oldschoolcompanion.helpers.Constants.SORT_DELIMITER;

public abstract class BaseTileFragment extends BaseFragment implements DragListView.DragListListener, AdapterTileClickListener {

    private int portraitColumns;
    private int landscapeColumns;

    protected DragListView gridView;
    protected DragTileAdapter adapter;
    protected int currentColumns;
    protected Tiles tiles;
    protected boolean requireOrderUpdate;

    public BaseTileFragment(int portraitColumns, int landscapeColumns) {
        this.portraitColumns = portraitColumns;
        this.landscapeColumns = landscapeColumns;
        this.tiles = new Tiles();
        requireOrderUpdate = true;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        int currentOrientation = getResources().getConfiguration().orientation;
        updateColumns(currentOrientation);
        initializeTiles();
        initializeGridView();
        updateGridView();
    }

    private void updateGridView() {
        gridView.setDragEnabled(false);
        gridView.setDragListListener(this);
        gridView.setCanDragHorizontally(true);
        if (adapter == null) {
            adapter = new DragTileAdapter(getActivity(), tiles, getTileOrder(), this);
        }

        gridView.setLayoutManager(new GridLayoutManager(getActivity(), currentColumns, LinearLayoutManager.VERTICAL, false));
        RecyclerView recyclerView = gridView.getRecyclerView();
        while (recyclerView.getItemDecorationCount() > 0) {
            recyclerView.removeItemDecorationAt(0);
        }
        gridView.getRecyclerView().addItemDecoration(new GridSpacingItemDecoration(currentColumns, (int) Utils.convertDpToPixel(7, getActivity()), false));
        if (gridView.getAdapter() == null) {
            if (requireOrderUpdate) {
                adapter.updateSortOrder(getTileOrder());
                requireOrderUpdate = false;
            }
            gridView.setAdapter(adapter, true);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateColumns(newConfig.orientation);
        if (getActivity() == null) return;
        initializeTiles();
        initializeGridView();
        updateGridView();
    }

    private void updateColumns(int currentOrientation) {
        if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            currentColumns = landscapeColumns;
        }
        else {
            currentColumns = portraitColumns;
        }
    }

    @Override
    public void onItemDragStarted(int position) {

    }

    @Override
    public void onItemDragging(int itemPosition, float x, float y) {

    }

    @Override
    public void onItemDragEnded(int fromPosition, int toPosition) {
        if (hasEmptyAdapter()) {
            return;
        }
        SharedPreferences.Editor editor = preferences.edit();
        int order = 1;
        Set<String> set = new HashSet<>();
        for (TileData tileData : adapter.getTiles()) {
            set.add(getTilePreferenceName(tileData.id, order));
            order++;
        }
        editor.putStringSet(this.getClass().getSimpleName(), set);
        editor.apply();
        requireOrderUpdate = true;
    }

    private String getTilePreferenceName(long id, int order) {
        return String.format(Locale.getDefault(), "%d%s%d", id, SORT_DELIMITER, order);
    }

    protected boolean hasEmptyAdapter() {
        if (gridView.getAdapter() == null) {
            showToast(getString(R.string.unexpected_error_try_reopen), Toast.LENGTH_SHORT);
            return true;
        }
        return false;
    }

    protected Set<String> getTileOrder() {
        return preferences.getStringSet(this.getClass().getSimpleName(), new HashSet<String>());
    }

    @Override
    public boolean onBackClick() {
        if (!hasEmptyAdapter() && adapter.isEditModeActivated()) {
            adapter.toggleEditMode(false);
            gridView.setDragEnabled(false);
            return true;
        }
        return super.onBackClick();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_tile_edit_mode && !hasEmptyAdapter()) {
            adapter.toggleEditMode(!adapter.isEditModeActivated());
            gridView.setDragEnabled(adapter.isEditModeActivated());
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTileClick(TileData tileData) {
        throw new RuntimeException("Override this method and use own implementation");
    }

    protected abstract void initializeTiles();

    protected abstract void initializeGridView();
}
