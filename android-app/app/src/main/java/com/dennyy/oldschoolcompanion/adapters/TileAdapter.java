package com.dennyy.oldschoolcompanion.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.dennyy.oldschoolcompanion.R;
import com.dennyy.oldschoolcompanion.models.General.TileData;

import java.util.ArrayList;

public class TileAdapter extends GenericAdapter<TileData> {

    public TileAdapter(Context context, ArrayList<TileData> tiles) {
        super(context, tiles);
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
        TileData tileData = getItem(i);
        viewHolder.text.setText(tileData.text);
        viewHolder.drawable.setBackground(tileData.drawable);
        return convertView;
    }

    private static class ViewHolder {
        public ImageView drawable;
        public TextView text;
    }
}
