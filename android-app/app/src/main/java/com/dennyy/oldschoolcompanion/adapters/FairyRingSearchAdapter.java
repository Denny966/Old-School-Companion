package com.dennyy.oldschoolcompanion.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import com.dennyy.oldschoolcompanion.R;
import com.dennyy.oldschoolcompanion.helpers.Utils;
import com.dennyy.oldschoolcompanion.models.FairyRings.FairyRing;

import java.util.ArrayList;
import java.util.List;

public class FairyRingSearchAdapter extends GenericAdapter<FairyRing> implements Filterable {
    private ItemFilter mFilter = new ItemFilter();

    public FairyRingSearchAdapter(Context context, ArrayList<FairyRing> fairyRings) {
        super(context, fairyRings);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.fr_search_row, null);

            viewHolder = new ViewHolder();
            viewHolder.text = convertView.findViewById(R.id.fr_search_code);
            convertView.setTag(viewHolder);
        }
        else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        FairyRing fairyRing = getItem(position);
        viewHolder.text.setText(String.format(Utils.isNullOrEmpty(fairyRing.location) ? "%s" : "%s (%s)", fairyRing.code, fairyRing.location));

        return convertView;
    }

    @Override
    public void notifyDataSetChanged() {
        if (collection.size() < 1) {
            FairyRing fairyRing = new FairyRing();
            fairyRing.code = context.getString(R.string.fairy_ring_not_found);
            collection.add(fairyRing);
        }
        super.notifyDataSetChanged();
    }

    @Override
    public Filter getFilter() {
        return mFilter;
    }

    public static class ViewHolder {
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
                final List<FairyRing> nlist = new ArrayList<>();
                for (FairyRing fairyRing : originalCollection) {
                    if (fairyRing.code.toLowerCase().contains(constraint.toString().toLowerCase()) || fairyRing.location.toLowerCase().contains(constraint.toString().toLowerCase()) || fairyRing.pointsOfInterest.toLowerCase().contains(constraint.toString().toLowerCase())) {
                        nlist.add(fairyRing);
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
            updateList((ArrayList<FairyRing>) results.values);
        }
    }
}
