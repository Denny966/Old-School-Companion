package com.dennyy.osrscompanion.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.dennyy.osrscompanion.R;
import com.dennyy.osrscompanion.models.Home.TileData;

import java.util.ArrayList;

public class TileAdapter extends BaseAdapter {
    private ArrayList<TileData> tiles;
    private Context context;

    public TileAdapter(Context context, ArrayList<TileData> tiles) {
        this.tiles = tiles;
        this.context = context;
    }

    @Override
    public int getCount() {
        return tiles.size();
    }

    @Override
    public TileData getItem(int i) {
        return tiles.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        if (convertView == null) {
            final LayoutInflater layoutInflater = LayoutInflater.from(context);
            convertView = layoutInflater.inflate(R.layout.home_tile_layout, null);
            viewHolder = new ViewHolder();
            viewHolder.drawable = convertView.findViewById(R.id.home_tile_drawable);
            viewHolder.text = convertView.findViewById(R.id.home_tile_textview);

            convertView.setTag(viewHolder);
        }
        else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        TileData tileData = this.tiles.get(i);
        viewHolder.text.setText(tileData.text);
        viewHolder.drawable.setBackground(tileData.drawable);
        return convertView;
    }

    private static class ViewHolder {
        public ImageView drawable;
        public TextView text;
    }
}
