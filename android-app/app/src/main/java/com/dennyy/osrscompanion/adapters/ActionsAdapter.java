package com.dennyy.osrscompanion.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.dennyy.osrscompanion.R;
import com.dennyy.osrscompanion.helpers.RsUtils;
import com.dennyy.osrscompanion.helpers.Utils;
import com.dennyy.osrscompanion.models.General.Action;

import java.util.ArrayList;

public class ActionsAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<Action> actions;
    private int expDifference;
    private int currentLvl;
    private int targetLvl;


    public ActionsAdapter(Context context, ArrayList<Action> actions) {
        this.context = context;
        this.actions = actions;
        this.expDifference = -1;
        this.currentLvl = -1;
        this.targetLvl = -1;
    }

    @Override
    public int getCount() {
        return actions.size();
    }

    @Override
    public Action getItem(int i) {
        return actions.get(i);
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
            convertView = layoutInflater.inflate(R.layout.action_list_row, null);
            viewHolder = new ViewHolder();
            viewHolder.lvl = convertView.findViewById(R.id.action_list_lvl);
            viewHolder.action = convertView.findViewById(R.id.action_list_action);
            viewHolder.exp = convertView.findViewById(R.id.action_list_exp);
            viewHolder.amount = convertView.findViewById(R.id.action_list_amount);

            convertView.setTag(viewHolder);
        }
        else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        Action action = actions.get(i);
        viewHolder.lvl.setText(String.valueOf(action.level));
        if (targetLvl > 0 || currentLvl > 0) {
            if (action.level <= currentLvl){
                viewHolder.lvl.setTextColor(context.getResources().getColor(R.color.green));
            }
            else if (action.level <= targetLvl) {
                viewHolder.lvl.setTextColor(context.getResources().getColor(R.color.orange));
            }
            else if (action.level > currentLvl) {
                viewHolder.lvl.setTextColor(context.getResources().getColor(R.color.red));
            }
        }
        viewHolder.action.setText(action.name);
        viewHolder.exp.setText(String.valueOf(action.exp));
        viewHolder.amount.setText(expDifference == -1 ? "N/A" : String.valueOf(Utils.formatNumber((int) Math.ceil(expDifference / action.exp))));
        return convertView;
    }

    public void updateList(ArrayList<Action> actions) {
        this.actions.clear();
        this.actions.addAll(actions);
        notifyDataSetChanged();
    }


    public void updateListFromLvl(int currentLvl, int targetLvl) {
        expDifference = RsUtils.exp(targetLvl) - RsUtils.exp(currentLvl);
        this.currentLvl = currentLvl;
        this.targetLvl = targetLvl;
        notifyDataSetChanged();
    }

    public void updateListFromExp(int currentExp, int targetExp) {
        expDifference = targetExp - currentExp;
        this.currentLvl = RsUtils.lvl(currentExp,true);
        this.targetLvl = RsUtils.lvl(targetExp,true);
        notifyDataSetChanged();
    }

    private static class ViewHolder {
        public TextView lvl;
        public TextView action;
        public TextView exp;
        public TextView amount;
    }
}
