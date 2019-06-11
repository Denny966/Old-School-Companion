package com.dennyy.oldschoolcompanion.adapters;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.dennyy.oldschoolcompanion.R;
import com.dennyy.oldschoolcompanion.helpers.Constants;
import com.dennyy.oldschoolcompanion.helpers.RsUtils;
import com.dennyy.oldschoolcompanion.helpers.Utils;
import com.dennyy.oldschoolcompanion.models.GrandExchange.JsonItem;
import me.xdrop.fuzzywuzzy.FuzzySearch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class GrandExchangeSearchAdapter extends GenericAdapter<JsonItem> implements Filterable, View.OnTouchListener {

    private ItemFilter mFilter = new ItemFilter();

    public GrandExchangeSearchAdapter(Context context, Collection<JsonItem> grandExchangeItems) {
        super(context, new ArrayList<>(grandExchangeItems));
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.ge_search_row, null);

            viewHolder = new ViewHolder();
            viewHolder.icon = convertView.findViewById(R.id.ge_search_item_img);
            viewHolder.name = convertView.findViewById(R.id.ge_search_item_name);

            convertView.setTag(viewHolder);
        }
        else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        convertView.setOnTouchListener(this);
        JsonItem search = getItem(position);
        viewHolder.name.setText(search.name);
        Glide.with(context).load(Constants.GE_IMG_SMALL_URL + search.id).into(viewHolder.icon);
        return convertView;
    }

    @Override
    public void notifyDataSetChanged() {
        if (collection.size() < 1) {
            JsonItem item = new JsonItem();
            item.id = "-1";
            item.name = context.getString(R.string.ge_item_not_found);
            collection.add(item);
        }
        super.notifyDataSetChanged();
    }

    @Override
    public Filter getFilter() {
        return mFilter;
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
            Utils.hideKeyboard(context, view);
        }
        return false;
    }

    private static class ViewHolder {
        public TextView name;
        public ImageView icon;
    }

    // https://gist.github.com/fjfish/3024308
    private class ItemFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();
            if (constraint == null || constraint.length() == 0) {
                results.values = collection;
                results.count = collection.size();
            }
            else {
                final List<JsonItem> nlist = new ArrayList<>();
                final List<JsonItem> fuzzyList = new ArrayList<>();
                String itemName = RsUtils.longName(constraint.toString()).toLowerCase();
                for (JsonItem grandExchangeItem : originalCollection) {
                    if (grandExchangeItem.name.toLowerCase().contains(itemName)) {
                        nlist.add(grandExchangeItem);
                    }
                    else if (FuzzySearch.partialRatio(grandExchangeItem.name.toLowerCase(), itemName) >= Constants.FUZZY_RATIO) {
                        fuzzyList.add(grandExchangeItem);
                    }
                }
                nlist.addAll(fuzzyList);

                results.values = nlist;
                results.count = nlist.size();
            }
            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            updateList((ArrayList<JsonItem>) results.values);
        }
    }
}