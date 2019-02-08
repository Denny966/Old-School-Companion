package com.dennyy.oldschoolcompanion.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.dennyy.oldschoolcompanion.R;
import com.dennyy.oldschoolcompanion.helpers.Constants;
import com.dennyy.oldschoolcompanion.models.FairyRings.FairyRing;

import java.util.ArrayList;

public class FairyRingListAdapter extends GenericAdapter<FairyRing> {

    public FairyRingListAdapter(Context context, ArrayList<FairyRing> fairyRings) {
        super(context, fairyRings);
    }

    @Override
    public View getView(final int i, View convertView, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.fr_list_row, null);
            viewHolder = new ViewHolder();
            viewHolder.code = convertView.findViewById(R.id.fr_code_text);
            viewHolder.image = convertView.findViewById(R.id.fr_location_img);
            viewHolder.location = convertView.findViewById(R.id.fr_location_text);
            viewHolder.poi = convertView.findViewById(R.id.fr_poi_text);

            convertView.setTag(viewHolder);
        }
        else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        FairyRing fairyRing = getItem(i);
        Glide.with(context).load(Constants.FAIRY_RING_MAP_URL(fairyRing.code)).into(viewHolder.image);
        viewHolder.code.setText(fairyRing.code);
        viewHolder.location.setText(fairyRing.location);
        viewHolder.poi.setText(fairyRing.pointsOfInterest);
        return convertView;
    }

    private static class ViewHolder {
        public ImageView image;
        public TextView poi;
        public TextView code;
        public TextView location;
    }

}
