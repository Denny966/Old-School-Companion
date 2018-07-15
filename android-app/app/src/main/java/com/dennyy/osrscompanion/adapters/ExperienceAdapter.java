package com.dennyy.osrscompanion.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.dennyy.osrscompanion.R;
import com.dennyy.osrscompanion.helpers.Utils;
import com.dennyy.osrscompanion.models.General.Experience;

import java.util.ArrayList;

public class ExperienceAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<Experience> experiences;

    public ExperienceAdapter(Context context, ArrayList<Experience> experiences) {
        this.context = context;
        this.experiences = experiences;
    }

    @Override
    public int getCount() {
        return experiences.size();
    }

    @Override
    public Experience getItem(int i) {
        return experiences.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        if (convertView == null) {
            final LayoutInflater layoutInflater = LayoutInflater.from(context);
            convertView = layoutInflater.inflate(R.layout.exp_calc_list_row, null);
            viewHolder = new ViewHolder();
            viewHolder.lvl = convertView.findViewById(R.id.exp_list_lvl);
            viewHolder.exp = convertView.findViewById(R.id.exp_list_exp);
            viewHolder.diff = convertView.findViewById(R.id.exp_list_diff);

            convertView.setTag(viewHolder);
        }
        else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        Experience experience = experiences.get(i);
        viewHolder.lvl.setText(String.valueOf(experience.level));
        viewHolder.exp.setText(Utils.formatNumber(experience.experience));
        viewHolder.diff.setText(Utils.formatNumber(experience.difference));
        return convertView;
    }

    private static class ViewHolder {
        public TextView lvl;
        public TextView exp;
        public TextView diff;
    }
}
