package com.dennyy.oldschoolcompanion.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.dennyy.oldschoolcompanion.R;
import com.dennyy.oldschoolcompanion.enums.SkillType;
import com.dennyy.oldschoolcompanion.models.SkillCalculator.SkillCalculatorTypes;

public class SkillSelectorSpinnerAdapter extends BaseAdapter {
    private SkillCalculatorTypes skillCalculatorTypes;
    private Context context;
    private SkillType[] keys;

    public SkillSelectorSpinnerAdapter(Context context, SkillCalculatorTypes skillCalculatorTypes) {
        this.context = context;
        this.skillCalculatorTypes = skillCalculatorTypes;
        this.keys = skillCalculatorTypes.keySet().toArray(new SkillType[0]);
    }

    @Override
    public int getCount() {
        return skillCalculatorTypes.size();
    }

    @Override
    public String getItem(int position) {
        return skillCalculatorTypes.get(keys[position]);
    }

    public SkillType getSelectedSkillType(int position) {
        return keys[position];
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
        SkillType skillType = keys[i];
        viewHolder.skillName.setText(skillType.name);
        viewHolder.skillIcon.setImageDrawable(context.getResources().getDrawable(skillType.drawable));
        return convertView;
    }

    private static class ViewHolder {
        public ImageView skillIcon;
        public TextView skillName;
    }
}
