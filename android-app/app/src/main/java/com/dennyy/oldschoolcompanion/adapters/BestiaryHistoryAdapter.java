package com.dennyy.oldschoolcompanion.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.dennyy.oldschoolcompanion.R;
import com.dennyy.oldschoolcompanion.interfaces.BestiaryListeners;

import java.util.List;

public class BestiaryHistoryAdapter extends GenericAdapter<String> {
    private BestiaryListeners.BestiaryAdapterListener callback;

    public BestiaryHistoryAdapter(Context context, List<String> monsters, BestiaryListeners.BestiaryAdapterListener callback) {
        super(context, monsters);
        this.callback = callback;
    }

    @Override
    public View getView(final int i, View convertView, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.bestiary_history_row, null);
            viewHolder = new ViewHolder();
            viewHolder.monsterName = convertView.findViewById(R.id.bestiary_history_row_text);

            convertView.setTag(viewHolder);
        }
        else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        final String monsterName = getItem(i);
        viewHolder.monsterName.setText(monsterName);

        viewHolder.monsterName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.onClickMonsterName(monsterName);
            }
        });
        return convertView;
    }

    private static class ViewHolder {
        public TextView monsterName;
    }
}