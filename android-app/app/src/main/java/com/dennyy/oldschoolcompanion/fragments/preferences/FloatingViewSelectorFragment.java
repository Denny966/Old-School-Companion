package com.dennyy.oldschoolcompanion.fragments.preferences;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.dennyy.oldschoolcompanion.FloatingViewService;
import com.dennyy.oldschoolcompanion.R;
import com.dennyy.oldschoolcompanion.adapters.FloatingViewsAdapter;
import com.dennyy.oldschoolcompanion.fragments.BaseFragment;
import com.dennyy.oldschoolcompanion.helpers.Constants;
import com.dennyy.oldschoolcompanion.interfaces.AdapterFloatingViewClickListener;
import com.dennyy.oldschoolcompanion.models.FloatingViews.FloatingView;
import com.woxthebox.draglistview.DragListView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static com.dennyy.oldschoolcompanion.helpers.Constants.SORT_DELIMITER;

public class FloatingViewSelectorFragment extends BaseFragment implements DragListView.DragListListener, AdapterFloatingViewClickListener {

    private DragListView listView;
    private FloatingViewsAdapter adapter;

    public FloatingViewSelectorFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        view = inflater.inflate(R.layout.floating_view_selector_layout, container, false);
        return view;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        toolbarTitle.setText(getString(R.string.select_floating_views));

        initListView();
    }

    private void initListView() {
        listView = view.findViewById(R.id.floating_view_listview);
        listView.setDragListListener(this);
        listView.setLayoutManager(new LinearLayoutManager(getActivity()));
        listView.setCanDragHorizontally(false);
        if (adapter == null) {
            adapter = new FloatingViewsAdapter(getActivity(), preferences.getString(Constants.PREF_FLOATING_VIEWS, ""), this);
            listView.setAdapter(adapter, true);
        }
    }

    @Override
    public void onItemDragStarted(int position) {

    }

    @Override
    public void onItemDragging(int itemPosition, float x, float y) {

    }

    @Override
    public void onItemDragEnded(int fromPosition, int toPosition) {
        List<FloatingView> floatingViews = adapter.getItemList();
        HashSet<String> sortOrder = new HashSet<>();
        for (int i = 0; i < floatingViews.size(); i++) {
            FloatingView floatingView = floatingViews.get(i);
            sortOrder.add(String.format("%s%s%s", floatingView.id, SORT_DELIMITER, i));
        }
        SharedPreferences.Editor editor = preferences.edit();
        editor.putStringSet(Constants.PREF_FLOATING_VIEWS_SORT_ORDER, sortOrder);
        editor.apply();
        FloatingViewService.updateSortOrder();
        showToast(getResources().getString(R.string.restart_to_take_effect), Toast.LENGTH_SHORT);
    }

    @Override
    public void onSelectionListener(List<FloatingView> floatingViews) {
        List<String> selectedIds = getSelected(floatingViews);

        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(Constants.PREF_FLOATING_VIEWS, TextUtils.join(FloatingViewService.DEFAULT_SEPARATOR, selectedIds));
        editor.apply();
        showToast(getResources().getString(R.string.restart_to_take_effect), Toast.LENGTH_SHORT);
    }

    private List<String> getSelected(List<FloatingView> floatingViews) {
        List<String> selected = new ArrayList<>();
        for (FloatingView floatingView : floatingViews) {
            if (floatingView.isSelected()) {
                selected.add(floatingView.id);
            }
        }
        return selected;
    }
}