package com.dennyy.oldschoolcompanion.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.dennyy.oldschoolcompanion.R;
import com.dennyy.oldschoolcompanion.helpers.Utils;
import com.dennyy.oldschoolcompanion.models.General.Experience;

import java.util.ArrayList;

public class ExperienceAdapter extends GenericAdapter<Experience> {

    public ExperienceAdapter(Context context, ArrayList<Experience> experiences) {
        super(context, experiences);
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
        Experience experience = getItem(i);
        viewHolder.lvl.setText(String.valueOf(experience.level));
        viewHolder.exp.setText(Utils.formatNumber(experience.experience));
        viewHolder.diff.setText(Utils.formatNumber(experience.difference));
        if (experience.level % 2 == 1) {
            convertView.setBackgroundResource(R.color.background_light);
        }
        else {
            convertView.setBackgroundResource(R.color.background);
        }
        return convertView;
    }

    private static class ViewHolder {
        public TextView lvl;
        public TextView exp;
        public TextView diff;
    }
}
