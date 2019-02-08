package com.dennyy.oldschoolcompanion.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.bumptech.glide.Glide;
import com.dennyy.oldschoolcompanion.R;
import com.dennyy.oldschoolcompanion.helpers.Constants;
import com.dennyy.oldschoolcompanion.helpers.RsUtils;
import com.dennyy.oldschoolcompanion.helpers.Utils;
import com.dennyy.oldschoolcompanion.models.AlchOverview.AlchItem;

import java.util.ArrayList;
import java.util.List;

public class AlchOverviewAdapter extends GenericAdapter<AlchItem> implements Filterable {

    private int natureRunePrice;
    private int red;
    private int green;
    private ItemFilter filter = new ItemFilter();

    public AlchOverviewAdapter(Context context, List<AlchItem> alchItems) {
        super(context, alchItems);
        red = context.getResources().getColor(R.color.red);
        green = context.getResources().getColor(R.color.green);
    }

    public void updateNatureRunePrice(int price) {
        natureRunePrice = price;
        notifyDataSetChanged();
    }

    @Override
    public View getView(final int i, View convertView, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.alch_row, null);
            viewHolder = new ViewHolder();
            viewHolder.img = convertView.findViewById(R.id.alch_row_img);
            viewHolder.name = convertView.findViewById(R.id.alch_row_name);
            viewHolder.members = convertView.findViewById(R.id.alch_item_members_indicator);
            viewHolder.detailView = convertView.findViewById(R.id.alch_row_detailed);
            viewHolder.buyPrice = convertView.findViewById(R.id.alch_row_buy_price);
            viewHolder.buyLimit = convertView.findViewById(R.id.alch_row_buy_limit);
            viewHolder.highAlchValue = convertView.findViewById(R.id.alch_row_high_alch_value);
            viewHolder.highAlchProfitLoss = convertView.findViewById(R.id.high_alch_profit_loss_text);
            viewHolder.quickViewHighAlchDiff = convertView.findViewById(R.id.alch_row_quick_view_diff);
            viewHolder.highAlchDiff = convertView.findViewById(R.id.alch_row_high_alch_difference);
            viewHolder.lowAlchValue = convertView.findViewById(R.id.alch_row_low_alch_value);
            viewHolder.lowAlchProfitLoss = convertView.findViewById(R.id.low_alch_profit_loss_text);
            viewHolder.lowAlchDiff = convertView.findViewById(R.id.alch_row_low_alch_difference);
            viewHolder.dropDown = convertView.findViewById(R.id.alch_row_expand_arrow);

            convertView.setTag(viewHolder);
        }
        else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        final AlchItem alchItem = getItem(i);
        if (Utils.isValidContextForGlide(context)) {
            Glide.with(context).load(Constants.GE_IMG_SMALL_URL + alchItem.id).into(viewHolder.img);
        }
        viewHolder.name.setText(alchItem.name);
        viewHolder.members.setImageDrawable(alchItem.isMembers ? context.getDrawable(R.drawable.members) : null);
        viewHolder.buyPrice.setText(RsUtils.kmbt(alchItem.buyPrice));
        viewHolder.buyLimit.setText(alchItem.buyLimit < 0 ? context.getString(R.string.unknown) : RsUtils.kmbt(alchItem.buyLimit));
        int highAlchProfit = alchItem.getHighAlchProfit(natureRunePrice);
        int lowAlchProfit = alchItem.getLowAlchProfit(natureRunePrice);
        viewHolder.highAlchDiff.setTextColor(highAlchProfit < 0 ? red : green);
        viewHolder.quickViewHighAlchDiff.setTextColor(highAlchProfit < 0 ? red : green);
        viewHolder.lowAlchDiff.setTextColor(lowAlchProfit < 0 ? red : green);
        viewHolder.highAlchValue.setText(RsUtils.kmbt(alchItem.highAlchValue));
        viewHolder.highAlchDiff.setText(String.format("%s%s", highAlchProfit < 0 ? "" : "+", RsUtils.kmbt(alchItem.getHighAlchProfit(natureRunePrice))));
        viewHolder.quickViewHighAlchDiff.setText(viewHolder.highAlchDiff.getText().toString());
        viewHolder.highAlchProfitLoss.setText(context.getString(highAlchProfit < 0 ? R.string.high_alch_loss : R.string.high_alch_profit));
        viewHolder.lowAlchProfitLoss.setText(context.getString(lowAlchProfit < 0 ? R.string.low_alch_loss : R.string.low_alch_profit));
        viewHolder.lowAlchValue.setText(RsUtils.kmbt(alchItem.lowAlchValue));
        viewHolder.lowAlchDiff.setText(String.format("%s%s", lowAlchProfit < 0 ? "" : "+", RsUtils.kmbt(alchItem.getLowAlchProfit(natureRunePrice))));

        viewHolder.detailView.setVisibility(alchItem.expanded ? View.VISIBLE : View.GONE);
        viewHolder.quickViewHighAlchDiff.setVisibility(alchItem.expanded ? View.GONE : View.VISIBLE);
        viewHolder.dropDown.setImageDrawable(context.getResources().getDrawable(alchItem.expanded ? R.drawable.baseline_arrow_drop_up_white_24 : R.drawable.baseline_arrow_drop_down_white_24));

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alchItem.expanded = !alchItem.expanded;
                notifyDataSetChanged();
            }
        });
        return convertView;
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
                final List<AlchItem> newList = new ArrayList<>();
                for (AlchItem alchItem : originalCollection) {
                    if (alchItem.name.toLowerCase().contains(constraint.toString().toLowerCase())) {
                        newList.add(alchItem);
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
            updateList((List<AlchItem>) results.values);
        }
    }

    private static class ViewHolder {
        public ImageView img;
        public TextView name;
        public ImageView members;
        public LinearLayout detailView;
        public TextView buyPrice;
        public TextView buyLimit;
        public TextView highAlchProfitLoss;
        public TextView highAlchValue;
        public TextView highAlchDiff;
        public TextView quickViewHighAlchDiff;
        public TextView lowAlchValue;
        public TextView lowAlchProfitLoss;
        public TextView lowAlchDiff;
        public ImageView dropDown;
    }
}
