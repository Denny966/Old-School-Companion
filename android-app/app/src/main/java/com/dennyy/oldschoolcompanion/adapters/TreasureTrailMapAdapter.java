package com.dennyy.oldschoolcompanion.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.dennyy.oldschoolcompanion.R;
import com.dennyy.oldschoolcompanion.helpers.Constants;
import com.dennyy.oldschoolcompanion.interfaces.AdapterImageClickListener;
import com.dennyy.oldschoolcompanion.models.TreasureTrails.TreasureTrailMap;

import java.util.ArrayList;

public class TreasureTrailMapAdapter extends GenericAdapter<TreasureTrailMap> {
    private AdapterImageClickListener adapterImageClickListener;

    public TreasureTrailMapAdapter(Context context, ArrayList<TreasureTrailMap> treasureTrailMaps, AdapterImageClickListener imageClickListener) {
        super(context, treasureTrailMaps);
        this.adapterImageClickListener = imageClickListener;
    }

    @Override
    public View getView(final int i, View convertView, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        if (convertView == null) {
            final LayoutInflater layoutInflater = LayoutInflater.from(context);
            convertView = layoutInflater.inflate(R.layout.tt_map_row, null);
            viewHolder = new ViewHolder();
            viewHolder.mapImage = convertView.findViewById(R.id.tt_map_img);
            viewHolder.ingameImage = convertView.findViewById(R.id.tt_ingame_img);
            viewHolder.location = convertView.findViewById(R.id.tt_map_location_text);

            convertView.setTag(viewHolder);
        }
        else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        TreasureTrailMap treasureTrailMap = getItem(i);
        Glide.with(context).load(Constants.TT_MAPS_URL(treasureTrailMap.id)).into(viewHolder.mapImage);
        Glide.with(context).load(Constants.TT_MAPS_URL(treasureTrailMap.id + "_2")).into(viewHolder.ingameImage);
        viewHolder.location.setText(treasureTrailMap.location);

        viewHolder.mapImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (adapterImageClickListener != null) {
                    adapterImageClickListener.onClickImage(i, view);
                }
            }
        });
        viewHolder.ingameImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (adapterImageClickListener != null) {
                    adapterImageClickListener.onClickImage(i, view);
                }
            }
        });
        return convertView;
    }

    private static class ViewHolder {
        public ImageView mapImage;
        public ImageView ingameImage;
        public TextView location;
    }
}