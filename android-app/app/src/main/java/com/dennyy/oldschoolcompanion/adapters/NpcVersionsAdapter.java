package com.dennyy.oldschoolcompanion.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.dennyy.oldschoolcompanion.R;

import java.util.ArrayList;

public class NpcVersionsAdapter extends GenericAdapter<String> {

    public NpcVersionsAdapter(Context context, ArrayList<String> versions) {
        super(context, versions);
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        if (convertView == null) {
            final LayoutInflater layoutInflater = LayoutInflater.from(context);
            convertView = layoutInflater.inflate(R.layout.adapter_row, null);
            viewHolder = new ViewHolder();
            viewHolder.version = convertView.findViewById(R.id.adapter_row_text);
            convertView.setTag(viewHolder);
        }
        else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        String version = getItem(i);
        viewHolder.version.setText(version);
        return convertView;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            final LayoutInflater layoutInflater = LayoutInflater.from(context);
            convertView = layoutInflater.inflate(R.layout.adapter_row_dropdown, null);
            viewHolder = new ViewHolder();
            viewHolder.version = convertView.findViewById(R.id.adapter_row_text);
            convertView.setTag(viewHolder);
        }
        else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        String version = getItem(position);

        viewHolder.version.setText(version);

        return convertView;
    }

    private static class ViewHolder {
        public TextView version;
    }
}
