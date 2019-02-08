package com.dennyy.oldschoolcompanion.adapters;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.dennyy.oldschoolcompanion.R;
import com.dennyy.oldschoolcompanion.helpers.Constants;
import com.dennyy.oldschoolcompanion.interfaces.AdapterGeHistoryClickListener;
import com.dennyy.oldschoolcompanion.models.GrandExchange.GeHistory;
import com.dennyy.oldschoolcompanion.models.GrandExchange.GeHistoryEntry;

public class GeHistoryAdapter extends GenericAdapter<GeHistoryEntry> {
    private AdapterGeHistoryClickListener callback;

    public GeHistoryAdapter(Context context, GeHistory geHistory, AdapterGeHistoryClickListener callback) {
        super(context, geHistory);
        this.callback = callback;
    }

    @Override
    public View getView(final int i, View convertView, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.ge_history_row, null);
            viewHolder = new ViewHolder();
            viewHolder.itemImage = convertView.findViewById(R.id.ge_history_item_img);
            viewHolder.itemName = convertView.findViewById(R.id.ge_history_item_name);
            viewHolder.favoriteIcon = convertView.findViewById(R.id.ge_history_favorite_icon);

            convertView.setTag(viewHolder);
        }
        else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        final GeHistoryEntry entry = getItem(i);
        Glide.with(context).load(Constants.GE_IMG_SMALL_URL + entry.itemId).into(viewHolder.itemImage);
        viewHolder.itemName.setText(entry.itemName);
        if (entry.isFavorite()) {
            viewHolder.favoriteIcon.setBackground(ContextCompat.getDrawable(context, R.drawable.baseline_star_white_24));

            viewHolder.favoriteIcon.setVisibility(View.VISIBLE);
        }
        else {
            viewHolder.favoriteIcon.setBackgroundResource(android.R.color.transparent);
            viewHolder.favoriteIcon.setVisibility(View.GONE);
        }
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (callback != null) {
                    callback.onClickGeHistory(entry.itemId);
                }
            }
        });
        viewHolder.favoriteIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (callback != null) {
                    callback.onClickRemoveFavorite(entry.itemId, entry.itemName);
                }
            }
        });
        return convertView;
    }

    private static class ViewHolder {
        public ImageView itemImage;
        public TextView itemName;
        public ImageView favoriteIcon;
    }
}