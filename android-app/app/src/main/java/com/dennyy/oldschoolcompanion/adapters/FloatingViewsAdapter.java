package com.dennyy.oldschoolcompanion.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.dennyy.oldschoolcompanion.FloatingViewService;
import com.dennyy.oldschoolcompanion.R;
import com.dennyy.oldschoolcompanion.helpers.Utils;
import com.dennyy.oldschoolcompanion.interfaces.AdapterFloatingViewClickListener;
import com.dennyy.oldschoolcompanion.models.FloatingViews.FloatingView;
import com.woxthebox.draglistview.DragItemAdapter;

import java.util.*;

public class FloatingViewsAdapter extends DragItemAdapter<FloatingView, FloatingViewsAdapter.ViewHolder> {
    private Context context;
    private LayoutInflater inflater;
    private AdapterFloatingViewClickListener listener;

    public FloatingViewsAdapter(Context context, String selectedFloatingViews, AdapterFloatingViewClickListener listener) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.listener = listener;
        updateSelection(selectedFloatingViews);
    }

    public void updateSelection(String selectedFloatingViews) {
        Map<String, FloatingView> map = FloatingViewService.MAP;
        HashSet<String> selected = new HashSet<>();
        Collections.addAll(selected, selectedFloatingViews.split(FloatingViewService.DEFAULT_SEPARATOR));
        List<FloatingView> floatingViews = new ArrayList<>(map.values());
        Collections.sort(floatingViews);
        if (Utils.isNullOrEmpty(selectedFloatingViews) || selected.isEmpty()) {
            setItemList(floatingViews);
            return;
        }
        for (FloatingView floatingView : floatingViews) {
            floatingView.setSelected(selected.contains(floatingView.id));
        }
        setItemList(floatingViews);
    }

    public FloatingView getItem(int position) {
        return mItemList.get(position);
    }

    @Override
    public long getUniqueItemId(int position) {
        return getItem(position).id.hashCode();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.floating_view_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, int position) {
        final FloatingView floatingView = getItem(position);
        viewHolder.name.setText(floatingView.name);
        viewHolder.checkBox.setChecked(floatingView.isSelected());
        viewHolder.icon.setImageDrawable(context.getResources().getDrawable(floatingView.drawableId));
        viewHolder.row.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isChecked = !viewHolder.checkBox.isChecked();
                viewHolder.checkBox.setChecked(isChecked);
                floatingView.setSelected(isChecked);
                listener.onSelectionListener(mItemList);
            }
        });
        super.onBindViewHolder(viewHolder, position);
    }

    class ViewHolder extends DragItemAdapter.ViewHolder {
        public RelativeLayout row;
        public ImageView dragHandler;
        public ImageView icon;
        public TextView name;
        public CheckBox checkBox;

        public ViewHolder(View convertView) {
            super(convertView, R.id.floating_view_row_drag, false);
            row = convertView.findViewById(R.id.floating_view_list_item);
            name = convertView.findViewById(R.id.floating_view_row_name);
            icon = convertView.findViewById(R.id.floating_view_row_icon);
            dragHandler = convertView.findViewById(R.id.floating_view_row_drag);
            checkBox = convertView.findViewById(R.id.floating_view_row_checkbox);
        }
    }
}
