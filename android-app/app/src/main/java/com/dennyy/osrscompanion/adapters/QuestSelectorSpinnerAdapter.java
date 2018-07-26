package com.dennyy.osrscompanion.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.dennyy.osrscompanion.R;
import com.dennyy.osrscompanion.models.General.Quest;

import java.util.ArrayList;

public class QuestSelectorSpinnerAdapter extends BaseAdapter {
    private ArrayList<Quest> quests;
    private Context context;

    public QuestSelectorSpinnerAdapter(Context context, ArrayList<Quest> quests) {
        this.context = context;
        this.quests = quests;
    }

    @Override
    public int getCount() {
        return quests.size();
    }

    @Override
    public Quest getItem(int i) {
        return quests.get(i);
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
            convertView = layoutInflater.inflate(R.layout.quest_selector_row, null);
            viewHolder = new ViewHolder();
            viewHolder.questName = convertView.findViewById(R.id.quest_name);

            convertView.setTag(viewHolder);
        }
        else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        Quest quest = quests.get(i);
        viewHolder.questName.setText(quest.name);
        return convertView;
    }

    private static class ViewHolder {
        public TextView questName;
    }
}
