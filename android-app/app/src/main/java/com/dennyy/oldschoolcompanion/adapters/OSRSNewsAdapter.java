package com.dennyy.oldschoolcompanion.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.dennyy.oldschoolcompanion.R;
import com.dennyy.oldschoolcompanion.models.OSRSNews.OSRSNews;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class OSRSNewsAdapter extends GenericAdapter<OSRSNews> {
    private DateFormat dateFormat;

    public OSRSNewsAdapter(Context context, ArrayList<OSRSNews> osrsNewsList) {
        super(context, osrsNewsList);
        dateFormat = new SimpleDateFormat("c, dd MMM yyyy", Locale.getDefault());
    }

    @Override
    public View getView(final int i, View convertView, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.rsnews_row_layout, null);
            viewHolder = new ViewHolder();
            viewHolder.title = convertView.findViewById(R.id.osrs_news_title);
            viewHolder.category = convertView.findViewById(R.id.osrs_news_category);
            viewHolder.image = convertView.findViewById(R.id.osrs_news_img);
            viewHolder.description = convertView.findViewById(R.id.osrs_news_desc);
            viewHolder.date = convertView.findViewById(R.id.osrs_news_date);

            convertView.setTag(viewHolder);
        }
        else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        OSRSNews osrsNews = getItem(i);
        Glide.with(context).load(osrsNews.imageUrl).into(viewHolder.image);
        viewHolder.title.setText(osrsNews.title);
        viewHolder.category.setText(osrsNews.category);
        viewHolder.description.setText(osrsNews.description);
        viewHolder.date.setText(dateFormat.format(new Date(osrsNews.publicationDate)));
        return convertView;
    }

    private static class ViewHolder {
        public ImageView image;
        public TextView title;
        public TextView category;
        public TextView description;
        public TextView date;
    }

}
