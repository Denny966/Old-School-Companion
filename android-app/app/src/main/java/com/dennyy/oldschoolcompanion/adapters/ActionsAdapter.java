package com.dennyy.oldschoolcompanion.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import com.dennyy.oldschoolcompanion.R;
import com.dennyy.oldschoolcompanion.enums.SkillType;
import com.dennyy.oldschoolcompanion.helpers.RsUtils;
import com.dennyy.oldschoolcompanion.helpers.Utils;
import com.dennyy.oldschoolcompanion.models.SkillCalculator.SkillDataAction;
import com.dennyy.oldschoolcompanion.models.SkillCalculator.SkillDataBonus;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class ActionsAdapter extends GenericAdapter<SkillDataAction> implements Filterable {
    private int expDifference;
    private int currentLvl;
    private int targetLvl;
    private SkillDataBonus skillDataBonus;
    private DecimalFormat decimalFormat;
    private ItemFilter filter = new ItemFilter();

    public ActionsAdapter(Context context, ArrayList<SkillDataAction> skillDataActions) {
        super(context, skillDataActions);
        this.expDifference = -1;
        this.currentLvl = -1;
        this.targetLvl = -1;
        this.decimalFormat = new DecimalFormat("#.#");
        this.decimalFormat.setRoundingMode(RoundingMode.FLOOR);
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

        SkillDataAction skillDataAction = getItem(i);
        viewHolder.lvl.setText(String.valueOf(skillDataAction.level));
        viewHolder.lvl.setTextColor(getColorResource(skillDataAction));
        double bonusExp = !skillDataAction.ignoreBonus && skillDataBonus != null ? skillDataBonus.value * skillDataAction.exp : 0;
        double exp = skillDataAction.exp + bonusExp;
        viewHolder.action.setText(skillDataAction.getFormattedName());
        viewHolder.exp.setText(getExpText(skillDataAction, exp));
        viewHolder.amount.setText(getAmountText(expDifference, exp, skillDataAction));
        if (skillDataBonus != null && !skillDataBonus.isEmpty() && skillDataAction.ignoreBonus) {
            convertView.setAlpha(0.3f);
        }
        else {
            convertView.setAlpha(1.0f);
        }
        return convertView;
    }

    private int getColorResource(SkillDataAction action) {
        int color = R.color.text;
        if (targetLvl > 0 || currentLvl > 0) {
            if (action.level <= currentLvl) {
                color = R.color.green;
            }
            else if (action.level <= targetLvl) {
                color = R.color.orange;
            }
            else {
                color = R.color.red;
            }
        }
        return context.getResources().getColor(color);
    }

    private double getCombatFactor(SkillDataAction action) {
        double combatFactor = 1;
        if (SkillType.isCombat(action.skillType, SkillType.PRAYER, SkillType.HITPOINTS, SkillType.MAGIC)) {
            combatFactor = 4;
        }
        else if (action.skillType == SkillType.HITPOINTS) {
            combatFactor = 1.33;
        }
        return combatFactor;
    }

    private String getExpText(SkillDataAction action, double exp) {
        if (SkillType.isCombat(action.skillType, SkillType.PRAYER, SkillType.MAGIC)) {
            return String.format("%s (%shp)", decimalFormat.format(exp * getCombatFactor(action)), (int) exp);
        }
        else {
            return decimalFormat.format(exp);
        }
    }

    private String getAmountText(int expDifference, double exp, SkillDataAction action) {
        if (expDifference == -1) {
            return "N/A";
        }
        int result = (int) Math.ceil(expDifference / (exp * getCombatFactor(action)));
        return String.valueOf(Utils.formatNumber(result));
    }

    public void updateListFromExp(int currentExp, int targetExp) {
        expDifference = targetExp - currentExp;
        this.currentLvl = RsUtils.lvl(currentExp, true);
        this.targetLvl = RsUtils.lvl(targetExp, true);
        notifyDataSetChanged();
    }

    public void updateBonus(SkillDataBonus bonus) {
        skillDataBonus = bonus;
        notifyDataSetChanged();
    }

    public void updateCustomExp(int exp) {
        if (this.collection.isEmpty() || exp < 1)
            return; // already added so don't continue
        if (this.collection.get(0).skillType == SkillType.OVERALL) {
            collection.remove(0);
        }
        this.collection.add(0, new SkillDataAction(SkillType.OVERALL, context.getResources().getString(R.string.custom_exp), 1, exp, true));
        this.notifyDataSetChanged();
    }

    @Override
    public Filter getFilter() {
        return filter;
    }
    private class ItemFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();
            if (constraint == null || constraint.length() == 0) {
                results.values = originalCollection;
                results.count = originalCollection.size();
            }
            else {
                final List<SkillDataAction> newList = new ArrayList<>();
                for (SkillDataAction action : originalCollection) {
                    if (action.name.toLowerCase().contains(constraint.toString().toLowerCase())) {
                        newList.add(action);
                    }
                }
                results.values = newList;
                results.count = newList.size();
            }
            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            updateList((List<SkillDataAction>) results.values);
        }
    }


    private static class ViewHolder {
        public TextView lvl;
        public TextView action;
        public TextView exp;
        public TextView amount;
    }
}
