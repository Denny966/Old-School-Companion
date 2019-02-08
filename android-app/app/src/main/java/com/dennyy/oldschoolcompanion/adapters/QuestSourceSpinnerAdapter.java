package com.dennyy.oldschoolcompanion.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.dennyy.oldschoolcompanion.R;
import com.dennyy.oldschoolcompanion.enums.QuestSource;

import java.util.ArrayList;

public class QuestSourceSpinnerAdapter extends GenericAdapter<QuestSource> {

    public QuestSourceSpinnerAdapter(Context context, ArrayList<QuestSource> questSources) {
        super(context, questSources);
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.adapter_row, null);
            viewHolder = new ViewHolder();
            viewHolder.questName = convertView.findViewById(R.id.adapter_row_text);

            convertView.setTag(viewHolder);
        }
        else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        QuestSource questSource = getItem(i);
        viewHolder.questName.setText(questSource.getName());
        return convertView;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.adapter_row_dropdown, null);
            viewHolder = new ViewHolder();
            viewHolder.questName = convertView.findViewById(R.id.adapter_row_text);

            convertView.setTag(viewHolder);
        }
        else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        QuestSource questSource = getItem(position);
        viewHolder.questName.setText(questSource.getName());
        return convertView;
    }

    private static class ViewHolder {
        public TextView questName;
    }
}
