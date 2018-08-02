package com.dennyy.osrscompanion.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.dennyy.osrscompanion.R;
import com.dennyy.osrscompanion.helpers.Constants;
import com.dennyy.osrscompanion.models.TreasureTrails.TreasureTrailMap;

import java.util.ArrayList;

public class TreasureTrailMapAdapter extends BaseAdapter{
    private ArrayList<TreasureTrailMap> treasureTrailMaps;
    private Context context;
    private AdapterImageClickListener adapterImageClickListener;

    public TreasureTrailMapAdapter(Context context, ArrayList<TreasureTrailMap> treasureTrailMaps, AdapterImageClickListener imageClickListener) {
        this.context = context;
        this.treasureTrailMaps = treasureTrailMaps;
        this.adapterImageClickListener = imageClickListener;
    }

    @Override
    public int getCount() {
        return treasureTrailMaps.size();
    }

    @Override
    public TreasureTrailMap getItem(int i) {
        return treasureTrailMaps.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
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
        TreasureTrailMap treasureTrailMap = treasureTrailMaps.get(i);
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

    public interface AdapterImageClickListener {
        void onClickImage(int index, View view);
    }
}
