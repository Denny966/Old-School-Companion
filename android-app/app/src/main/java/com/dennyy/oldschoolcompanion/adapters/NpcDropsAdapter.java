package com.dennyy.oldschoolcompanion.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.dennyy.oldschoolcompanion.R;
import com.dennyy.oldschoolcompanion.enums.DropRarity;
import com.dennyy.oldschoolcompanion.models.Bestiary.NpcDrop;

import java.util.ArrayList;

public class NpcDropsAdapter extends GenericAdapter<NpcDrop> {

    public NpcDropsAdapter(Context context, ArrayList<NpcDrop> drops) {
        super(context, drops);
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.npc_drop_row, null);
            viewHolder = new ViewHolder();
            viewHolder.drop = convertView.findViewById(R.id.npc_drop_name);
            viewHolder.quantity = convertView.findViewById(R.id.npc_drop_quantity);
            viewHolder.rarity = convertView.findViewById(R.id.npc_drop_rarity);
            viewHolder.notes = convertView.findViewById(R.id.npc_drop_note);

            convertView.setTag(viewHolder);
        }
        else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        NpcDrop drop = getItem(i);

        viewHolder.drop.setText(String.format("%s %s", drop.name, drop.nameNotes));
        viewHolder.quantity.setText(drop.quantity);
        int color;
        DropRarity dropRarity = DropRarity.fromString(drop.rarity);
        switch (dropRarity) {
            case ALWAYS:
                color = R.color.npc_drop_always;
                break;
            case COMMON:
                color = R.color.npc_drop_common;
                break;
            case UNCOMMON:
                color = R.color.npc_drop_uncommon;
                break;
            case RARE:
                color = R.color.npc_drop_rare;
                break;
            case VERY_RARE:
                color = R.color.npc_drop_very_rare;
                break;
            default:
                color = R.color.text;
        }
        viewHolder.rarity.setTextColor(context.getResources().getColor(color));
        viewHolder.rarity.setText(drop.rarity);
        viewHolder.notes.setText(drop.rarityNotes);
        if (i % 2 == 0)
            convertView.setBackgroundColor(context.getResources().getColor(R.color.alternate_row_color));
        else
            convertView.setBackgroundColor(context.getResources().getColor(R.color.input_background_color));

        return convertView;
    }

    private static class ViewHolder {
        public TextView drop;
        public TextView quantity;
        public TextView rarity;
        public TextView notes;
    }
}
