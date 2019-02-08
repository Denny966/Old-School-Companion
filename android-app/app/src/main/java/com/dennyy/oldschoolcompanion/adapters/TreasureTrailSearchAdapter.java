package com.dennyy.oldschoolcompanion.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import com.dennyy.oldschoolcompanion.R;
import com.dennyy.oldschoolcompanion.enums.TreasureTrailType;
import com.dennyy.oldschoolcompanion.helpers.Constants;
import com.dennyy.oldschoolcompanion.models.TreasureTrails.TreasureTrail;
import me.xdrop.fuzzywuzzy.FuzzySearch;

import java.util.ArrayList;
import java.util.List;


public class TreasureTrailSearchAdapter extends GenericAdapter<TreasureTrail> implements Filterable {
    private ItemFilter mFilter = new ItemFilter();

    public TreasureTrailSearchAdapter(Context context, ArrayList<TreasureTrail> treasureTrails) {
        super(context, treasureTrails);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.tt_search_row, null);

            viewHolder = new ViewHolder();
            viewHolder.text = convertView.findViewById(R.id.tt_search_item_name);
            convertView.setTag(viewHolder);
        }
        else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        if (position % 2 == 0)
            convertView.setBackgroundColor(context.getResources().getColor(R.color.alternate_row_color));
        else
            convertView.setBackgroundColor(context.getResources().getColor(R.color.input_background_color));
        TreasureTrail treasureTrail = getItem(position);
        if (treasureTrail.type == TreasureTrailType.COORDINATES) {
            viewHolder.text.setText(treasureTrail.getCoordinatesFormatted());
        }
        else
            viewHolder.text.setText(treasureTrail.text);

        return convertView;
    }

    @Override
    public void notifyDataSetChanged() {
        if (collection.size() < 1) {
            TreasureTrail treasureTrail = new TreasureTrail();
            treasureTrail.text = context.getString(R.string.clue_not_found);
            collection.add(treasureTrail);
        }
        super.notifyDataSetChanged();
    }

    @Override
    public Filter getFilter() {
        return mFilter;
    }

    private static class ViewHolder {
        public TextView text;
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
                final List<TreasureTrail> nlist = new ArrayList<>();
                for (TreasureTrail treasureTrail : originalCollection) {
                    String search = constraint.toString().toLowerCase();
                    if (treasureTrail.text.toLowerCase().contains(search)) {
                        nlist.add(treasureTrail);
                    }
                    else if (treasureTrail.containsCoordinates(search)) {
                        nlist.add(treasureTrail);
                    }
                    else if (FuzzySearch.partialRatio(treasureTrail.text.toLowerCase(), search) >= Constants.FUZZY_RATIO) {
                        nlist.add(treasureTrail);
                    }
                }
                results.values = nlist;
                results.count = nlist.size();
            }
            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            updateList((ArrayList<TreasureTrail>) results.values);
        }
    }
}
