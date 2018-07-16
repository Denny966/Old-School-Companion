package com.dennyy.osrscompanion.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.dennyy.osrscompanion.R;
import com.dennyy.osrscompanion.helpers.RsUtils;

import java.util.ArrayList;

public class SkillSelectorSpinnerAdapter extends BaseAdapter {
    private ArrayList<Integer> skills;
    private Context context;

    public SkillSelectorSpinnerAdapter(Context context, ArrayList<Integer> skills) {
        this.context = context;
        this.skills = skills;
    }

    @Override
    public int getCount() {
        return skills.size();
    }

    @Override
    public Integer getItem(int i) {
        return skills.get(i);
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
            convertView = layoutInflater.inflate(R.layout.skill_selector_row, null);
            viewHolder = new ViewHolder();
            viewHolder.skillIcon = convertView.findViewById(R.id.skill_icon);
            viewHolder.skillName = convertView.findViewById(R.id.skill_name);

            convertView.setTag(viewHolder);
        }
        else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        int skill = skills.get(i);
        viewHolder.skillName.setText(RsUtils.getSkill(skill));
        viewHolder.skillIcon.setImageDrawable(context.getResources().getDrawable(RsUtils.getSkillResourceId(skill)));
        return convertView;
    }

    private static class ViewHolder {
        public ImageView skillIcon;
        public TextView skillName;
    }
}
