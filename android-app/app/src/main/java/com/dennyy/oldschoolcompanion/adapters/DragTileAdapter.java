package com.dennyy.oldschoolcompanion.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.dennyy.oldschoolcompanion.R;
import com.dennyy.oldschoolcompanion.interfaces.AdapterTileClickListener;
import com.dennyy.oldschoolcompanion.models.General.TileData;
import com.dennyy.oldschoolcompanion.models.General.Tiles;
import com.woxthebox.draglistview.DragItemAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.dennyy.oldschoolcompanion.helpers.Constants.SORT_DELIMITER;

public class DragTileAdapter extends DragItemAdapter<TileData, DragTileAdapter.ViewHolder> {

    private Tiles tiles;
    private Context context;
    private LayoutInflater inflater;
    private boolean editModeActivated;
    private AdapterTileClickListener callback;

    public DragTileAdapter(Context context, ArrayList<TileData> tiles, Set<String> set, AdapterTileClickListener callback) {
        this.context = context;
        this.tiles = new Tiles(tiles);
        this.inflater = LayoutInflater.from(context);
        this.callback = callback;
        updateSortOrder(set);
        setItemList(this.tiles);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        final TileData tileData = tiles.get(position);
        viewHolder.drawable.setImageDrawable(tileData.drawable);
        viewHolder.text.setText(tileData.name);
        if (editModeActivated) {
            viewHolder.tile.setBackground(context.getDrawable(R.drawable.tile_bordered_background));
            if (tileData.isCustomTile) {
                viewHolder.editIcon.setVisibility(View.VISIBLE);
                viewHolder.editIcon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        callback.onEditButtonClick(tileData);
                    }
                });
                viewHolder.deleteIcon.setVisibility(View.VISIBLE);
                viewHolder.deleteIcon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        callback.onDeleteButtonClick(tileData);
                    }
                });
            }
            else {
                viewHolder.editIcon.setVisibility(View.GONE);
                viewHolder.deleteIcon.setVisibility(View.GONE);
            }
        }
        else {
            viewHolder.editIcon.setVisibility(View.GONE);
            viewHolder.deleteIcon.setVisibility(View.GONE);
            viewHolder.tile.setBackground(context.getDrawable(R.drawable.tile_background));
        }
        viewHolder.itemView.setTag(tileData);
        super.onBindViewHolder(viewHolder, position);
    }

    @Override
    public long getUniqueItemId(int position) {
        return tiles.get(position).id;
    }

    public ArrayList<TileData> getTiles() {
        return tiles;
    }

    public void updateSortOrder(Set<String> set) {
        if (set.size() < 1) return;
        for (TileData tileData : tiles) {
            int order = Integer.MAX_VALUE;
            for (String s : set) {
                String[] split = s.split(SORT_DELIMITER);
                long id = Long.parseLong(split[0]);
                if (tileData.id == id) {
                    order = Integer.parseInt(split[1]);
                    break;
                }
            }
            tileData.setSortOrder(order);
        }
        Collections.sort(tiles);
        notifyDataSetChanged();
    }

    public boolean isEditModeActivated() {
        return editModeActivated;
    }

    public void toggleEditMode(boolean activated) {
        editModeActivated = activated;
        notifyDataSetChanged();
    }

    public void updateList(List<TileData> newCollection) {
        this.tiles.clear();
        this.tiles.trimToSize();
        this.tiles.addAll(newCollection);
        setItemList(this.tiles);
    }

    @NonNull
    @Override
    public DragTileAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.home_tile_layout, parent, false);
        return new ViewHolder(view);
    }

    class ViewHolder extends DragItemAdapter.ViewHolder {
        public View tile;
        public ImageView drawable;
        public TextView text;
        public ImageView editIcon;
        public ImageView deleteIcon;

        public ViewHolder(View convertView) {
            super(convertView, R.id.home_tile_drawable, false);
            this.tile = convertView;
            this.drawable = convertView.findViewById(R.id.home_tile_drawable);
            this.text = convertView.findViewById(R.id.home_tile_textview);
            this.editIcon = convertView.findViewById(R.id.home_tile_edit);
            this.deleteIcon = convertView.findViewById(R.id.home_tile_delete);
        }

        @Override
        public void onItemClicked(View view) {
            Object tileData = view.getTag();
            if (tileData instanceof TileData) {
                callback.onTileClick((TileData) tileData);
            }
        }
    }
}
