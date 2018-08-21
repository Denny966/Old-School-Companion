package com.dennyy.osrscompanion.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.dennyy.osrscompanion.R;
import com.dennyy.osrscompanion.helpers.Utils;
import com.dennyy.osrscompanion.models.FairyRings.FairyRing;

import java.util.ArrayList;
import java.util.List;

public class FairyRingSearchAdapter extends ArrayAdapter<FairyRing> implements Filterable {
    private Context context;
    private LayoutInflater inflater;
    private ArrayList<FairyRing> fairyRings;
    private ArrayList<FairyRing> originalFairyRings;
    private ItemFilter mFilter = new ItemFilter();

    public FairyRingSearchAdapter(Context context, ArrayList<FairyRing> fairyRings) {
        super(context, 0, fairyRings);
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.fairyRings = new ArrayList<>(fairyRings);
        this.originalFairyRings = new ArrayList<>(fairyRings);
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

        FairyRing fairyRing = fairyRings.get(position);
        viewHolder.text.setText(String.format(Utils.isNullOrEmpty(fairyRing.location) ? "%s" : "%s (%s)", fairyRing.code, fairyRing.location));

        return convertView;
    }


    @Override
    public FairyRing getItem(int position) {
        return fairyRings.get(position);
    }

    @Override
    public int getCount() {
        return fairyRings != null ? fairyRings.size() : 0;
    }

    public void resetItems() {
        fairyRings.clear();
        fairyRings.trimToSize();
        fairyRings.addAll(originalFairyRings);
        this.notifyDataSetChanged();
    }

    @Override
    public void notifyDataSetChanged() {
        if (fairyRings.size() < 1) {
            FairyRing fairyRing = new FairyRing();
            fairyRing.code = context.getString(R.string.fairy_ring_not_found);
            fairyRings.add(fairyRing);
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
                results.values = fairyRings;
                results.count = fairyRings.size();
            }
            else {
                final List<FairyRing> nlist = new ArrayList<>();
                for (FairyRing fairyRing : originalFairyRings) {
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
            fairyRings = (ArrayList<FairyRing>) results.values;
            notifyDataSetChanged();
        }
    }
}
