package com.dennyy.oldschoolcompanion.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.dennyy.oldschoolcompanion.R;
import com.dennyy.oldschoolcompanion.models.SkillCalculator.SkillDataBonus;

import java.util.ArrayList;

public class SkillBonusSpinnerAdapter extends GenericAdapter<SkillDataBonus> {

    public SkillBonusSpinnerAdapter(Context context, ArrayList<SkillDataBonus> bonuses) {
        super(context, bonuses);
        this.collection.add(0, new SkillDataBonus(context.getString(R.string.no_bonus), 0));
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        if (convertView == null) {
            final LayoutInflater layoutInflater = LayoutInflater.from(context);
            convertView = layoutInflater.inflate(R.layout.adapter_row, null);
            viewHolder = new ViewHolder();
            viewHolder.bonusName = convertView.findViewById(R.id.adapter_row_text);

            convertView.setTag(viewHolder);
        }
        else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        SkillDataBonus bonus = getItem(i);
        viewHolder.bonusName.setText(bonus.name);
        return convertView;
    }


    @Override
    public View getDropDownView(int i, View convertView, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        if (convertView == null) {
            final LayoutInflater layoutInflater = LayoutInflater.from(context);
            convertView = layoutInflater.inflate(R.layout.adapter_row_dropdown, null);
            viewHolder = new ViewHolder();
            viewHolder.bonusName = convertView.findViewById(R.id.adapter_row_text);

            convertView.setTag(viewHolder);
        }
        else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        SkillDataBonus bonus = getItem(i);
        viewHolder.bonusName.setText(bonus.name);
        return convertView;
    }

    private static class ViewHolder {
        public TextView bonusName;
    }
}
