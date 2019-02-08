package com.dennyy.oldschoolcompanion.viewhandlers;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.dennyy.oldschoolcompanion.R;
import com.dennyy.oldschoolcompanion.adapters.FairyRingListAdapter;
import com.dennyy.oldschoolcompanion.adapters.FairyRingSearchAdapter;
import com.dennyy.oldschoolcompanion.asynctasks.GetFairyRingsTask;
import com.dennyy.oldschoolcompanion.customviews.ClearableAutoCompleteTextView;
import com.dennyy.oldschoolcompanion.customviews.DelayedAutoCompleteTextView;
import com.dennyy.oldschoolcompanion.customviews.ObservableListView;
import com.dennyy.oldschoolcompanion.enums.ScrollState;
import com.dennyy.oldschoolcompanion.helpers.Utils;
import com.dennyy.oldschoolcompanion.interfaces.FairyRingsLoadedListener;
import com.dennyy.oldschoolcompanion.interfaces.ObservableScrollViewCallbacks;
import com.dennyy.oldschoolcompanion.models.FairyRings.FairyRing;

import java.util.ArrayList;

public class FairyRingViewHandler extends BaseViewHandler implements TextWatcher, ObservableScrollViewCallbacks {
    public int selectedIndex;

    private ClearableAutoCompleteTextView clearableAutoCompleteTextView;
    private DelayedAutoCompleteTextView autoCompleteTextView;
    private ObservableListView listView;
    private FairyRingSearchAdapter searchAdapter;
    private FairyRingListAdapter listViewAdapter;
    private ArrayList<FairyRing> fairyRings;

    private final Handler navBarHandler = new Handler();
    private Runnable navBarRunnable;

    public FairyRingViewHandler(Context context, final View view) {
        super(context, view);
        new GetFairyRingsTask(context, new FairyRingsLoadedListener() {
            @Override
            public void onFairyRingsLoaded(ArrayList<FairyRing> items) {
                fairyRings = new ArrayList<>(items);
                updateView(view);
            }

            @Override
            public void onFairyRingsLoadError() {
                showToast(resources.getString(R.string.exception_occurred, "exception", "loading fairy rings"), Toast.LENGTH_LONG);
            }
        }).execute();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void updateView(View view) {
        clearableAutoCompleteTextView = view.findViewById(R.id.fr_search_input);
        listView = view.findViewById(R.id.fairy_rings_listview);
        listView.addScrollViewCallbacks(this);
        autoCompleteTextView = clearableAutoCompleteTextView.getAutoCompleteTextView();

        if (searchAdapter == null) {
            searchAdapter = new FairyRingSearchAdapter(context, fairyRings);
        }
        if (listViewAdapter == null) {
            listViewAdapter = new FairyRingListAdapter(context, fairyRings);
        }
        listView.setAdapter(listViewAdapter);
        autoCompleteTextView.setAdapter(searchAdapter);
        autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Utils.hideKeyboard(context, autoCompleteTextView);
                startHideNavBar(500);
                String selection = adapterView.getItemAtPosition(position).toString();
                selectedIndex = -1;
                for (int i = 0; i < fairyRings.size(); i++) {
                    if (fairyRings.get(i).code.toLowerCase().equals(selection.toLowerCase())) {
                        selectedIndex = i;
                        break;
                    }
                }
                listView.setSelection(selectedIndex);
            }
        });

        autoCompleteTextView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                showNavBar();
                return false;
            }
        });

        autoCompleteTextView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                showNavBar();
                return false;
            }
        });

        autoCompleteTextView.addTextChangedListener(this);
        listView.setSelection(selectedIndex);
        startHideNavBar(1000);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        if (s.toString().trim().length() == 0) {
            searchAdapter.resetList();
        }
    }

    @Override
    public void onScrollChanged(int scrollY, boolean firstScroll, boolean dragging) {
    }

    @Override
    public void onDownMotionEvent() {
    }

    @Override
    public void onUpOrCancelMotionEvent(ScrollState scrollState) {
        if (scrollState == ScrollState.UP) {
            startHideNavBar(0);
        }

        else if (scrollState == ScrollState.DOWN) {
            showNavBar();
            startHideNavBar();
        }
        else if ((scrollState == ScrollState.STOP || scrollState == null) && listView.getCurrentScrollY() == 0) {
            showNavBar();
            startHideNavBar();
        }
    }

    private void showNavBar() {
        navBarHandler.removeCallbacks(navBarRunnable);
        clearableAutoCompleteTextView.setVisibility(View.VISIBLE);
        clearableAutoCompleteTextView.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2));
    }

    private void startHideNavBar() {
        startHideNavBar(2000);
    }

    private void startHideNavBar(int delay) {
        navBarHandler.removeCallbacks(navBarRunnable);
        navBarRunnable = new Runnable() {
            @Override
            public void run() {
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) clearableAutoCompleteTextView.getLayoutParams();
                int height = clearableAutoCompleteTextView.getHeight() + params.bottomMargin + params.topMargin;
                clearableAutoCompleteTextView.animate().translationY(-height).setInterpolator(new AccelerateInterpolator(2));
            }
        };
        navBarHandler.postDelayed(navBarRunnable, delay);
    }

    @Override
    public void cancelRunningTasks() {
        navBarHandler.removeCallbacks(navBarRunnable);
    }

    @Override
    public boolean wasRequesting() {
        return false;
    }
}
