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
import com.dennyy.osrscompanion.enums.TreasureTrailType;
import com.dennyy.osrscompanion.helpers.Constants;
import com.dennyy.osrscompanion.models.TreasureTrails.TreasureTrail;

import java.util.ArrayList;
import java.util.List;

import me.xdrop.fuzzywuzzy.FuzzySearch;


public class TreasureTrailSearchAdapter extends ArrayAdapter<TreasureTrail> implements Filterable {
    private Context context;
    private LayoutInflater inflater;
    private ArrayList<TreasureTrail> treasureTrails;
    private ArrayList<TreasureTrail> originalTreasureTrails;
    private ItemFilter mFilter = new ItemFilter();

    public TreasureTrailSearchAdapter(Context context, LayoutInflater inflater, ArrayList<TreasureTrail> treasureTrails) {
        super(context, 0, treasureTrails);
        this.context = context;
        this.inflater = inflater;
        this.treasureTrails = new ArrayList<>(treasureTrails);
        this.originalTreasureTrails = new ArrayList<>(treasureTrails);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.tt_search_row, null);

            viewHolder = new ViewHolder();
            viewHolder.text = (TextView) convertView.findViewById(R.id.tt_search_item_name);
            convertView.setTag(viewHolder);
        }
        else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        if (position % 2 == 0)
            convertView.setBackgroundColor(context.getResources().getColor(R.color.alternate_row_color));
        else
            convertView.setBackgroundColor(context.getResources().getColor(R.color.input_background_color));
        TreasureTrail treasureTrail = treasureTrails.get(position);
        if (treasureTrail.type == TreasureTrailType.COORDINATES) {
            String coords = treasureTrail.text;
            String formattedCoords = (coords.substring(0, 2) + "." + coords.substring(2, 5) + ", " + coords.substring(5, 7) + "." + coords.substring(7, 10)).toUpperCase();
            viewHolder.text.setText(formattedCoords);
        }
        else
            viewHolder.text.setText(treasureTrail.text);

        return convertView;
    }

    public ArrayList<TreasureTrail> getItems() {
        return treasureTrails;
    }

    @Override
    public TreasureTrail getItem(int position) {
        return treasureTrails.get(position);
    }

    @Override
    public int getCount() {
        return treasureTrails != null ? treasureTrails.size() : 0;
    }

    public void resetItems() {
        treasureTrails.clear();
        treasureTrails.trimToSize();
        treasureTrails.addAll(originalTreasureTrails);
        this.notifyDataSetChanged();
    }

    public void updateItems(List<TreasureTrail> newList) {
        treasureTrails.clear();
        treasureTrails.trimToSize();
        treasureTrails.addAll(newList);
        this.notifyDataSetChanged();
    }

    @Override
    public void notifyDataSetChanged() {
        if (treasureTrails.size() < 1) {
            TreasureTrail treasureTrail = new TreasureTrail();
            treasureTrail.text = context.getString(R.string.clue_not_found);
            treasureTrails.add(treasureTrail);
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
                results.values = treasureTrails;
                results.count = treasureTrails.size();
            }
            else {
                final List<TreasureTrail> nlist = new ArrayList<>();
                final List<TreasureTrail> fuzzyList = new ArrayList<>();
                for (TreasureTrail treasureTrail : originalTreasureTrails) {
                    if (treasureTrail.text.toLowerCase().contains(constraint.toString().toLowerCase())) {
                        nlist.add(treasureTrail);
                    }
                    else if (FuzzySearch.partialRatio(treasureTrail.text.toLowerCase(), constraint.toString().toLowerCase()) >= Constants.FUZZY_RATIO) {
                        fuzzyList.add(treasureTrail);
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
            treasureTrails = (ArrayList<TreasureTrail>) results.values;
            notifyDataSetChanged();
        }

    }
}
