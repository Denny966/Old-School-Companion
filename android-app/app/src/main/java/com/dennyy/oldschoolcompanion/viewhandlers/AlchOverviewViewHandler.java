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
import android.widget.*;
import com.dennyy.oldschoolcompanion.AppController;
import com.dennyy.oldschoolcompanion.R;
import com.dennyy.oldschoolcompanion.adapters.AlchOverviewAdapter;
import com.dennyy.oldschoolcompanion.asynctasks.GeAsyncTasks;
import com.dennyy.oldschoolcompanion.asynctasks.GetOSBuddyExchangeSummaryTask;
import com.dennyy.oldschoolcompanion.customviews.ClearableEditText;
import com.dennyy.oldschoolcompanion.customviews.ObservableListView;
import com.dennyy.oldschoolcompanion.enums.ScrollState;
import com.dennyy.oldschoolcompanion.helpers.GeHelper;
import com.dennyy.oldschoolcompanion.helpers.Logger;
import com.dennyy.oldschoolcompanion.helpers.Utils;
import com.dennyy.oldschoolcompanion.interfaces.ClearableEditTextListener;
import com.dennyy.oldschoolcompanion.interfaces.GeListeners;
import com.dennyy.oldschoolcompanion.interfaces.OSBuddySummaryLoadedListener;
import com.dennyy.oldschoolcompanion.interfaces.ObservableScrollViewCallbacks;
import com.dennyy.oldschoolcompanion.models.AlchOverview.AlchItem;
import com.dennyy.oldschoolcompanion.models.GrandExchange.GrandExchangeData;
import com.dennyy.oldschoolcompanion.models.GrandExchange.GrandExchangeItem;
import com.dennyy.oldschoolcompanion.models.OSBuddy.OSBuddySummaryItem;
import me.zhanghai.android.materialprogressbar.MaterialProgressBar;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

import static com.dennyy.oldschoolcompanion.asynctasks.GeAsyncTasks.GetItemData.GRAND_EXCHANGE_REQUEST_TAG;
import static com.dennyy.oldschoolcompanion.asynctasks.GetOSBuddyExchangeSummaryTask.OSBUDDY_SUMMARY_REQUEST_TAG;
import static com.dennyy.oldschoolcompanion.helpers.GeHelper.BUY_LIMITS;

public class AlchOverviewViewHandler extends BaseViewHandler implements OSBuddySummaryLoadedListener, GeListeners.ItemDataLoadedListener, ObservableScrollViewCallbacks, TextWatcher, View.OnClickListener, View.OnTouchListener, TextView.OnEditorActionListener, ClearableEditTextListener {

    private static final String NATURE_RUNE_ID = "561";
    private static final int NATURE_RUNE_BUY_LIMIT = 12_000;

    private ObservableListView listView;
    private AlchOverviewAdapter adapter;
    private EditText natureRuneTextView;
    private ClearableEditText searchInput;
    private MaterialProgressBar progressBar;
    private ImageButton refreshButton;
    private LinearLayout natureRuneContainer;
    private final Handler handler = new Handler();
    private Runnable runnable;

    private final Handler navBarHandler = new Handler();
    private Runnable navBarRunnable;

    private boolean wasRequestingNatureRune;

    @SuppressLint("ClickableViewAccessibility")
    public AlchOverviewViewHandler(final Context context, View view, boolean isFloatingView) {
        super(context, view, isFloatingView);
        natureRuneTextView = view.findViewById(R.id.nature_rune_edittext);
        progressBar = view.findViewById(R.id.progressBar);
        listView = view.findViewById(R.id.alch_listview);
        natureRuneContainer = view.findViewById(R.id.nature_rune_container);
        refreshButton = view.findViewById(R.id.alch_refresh_button);
        searchInput = view.findViewById(R.id.alch_search_input);

        reloadData();
        listView.addScrollViewCallbacks(this);
        refreshButton.setOnClickListener(this);

        natureRuneTextView.setOnTouchListener(this);
        natureRuneTextView.setOnEditorActionListener(this);
        searchInput.getEditText().setOnTouchListener(this);
        searchInput.getEditText().setOnEditorActionListener(this);
        searchInput.setListener(this);

        natureRuneTextView.addTextChangedListener(this);
        startHideNavBar();
    }

    private void reloadData() {
        progressBar.setVisibility(View.VISIBLE);
        wasRequesting = true;
        wasRequestingNatureRune = true;
        new GetOSBuddyExchangeSummaryTask(context, this).execute();
        new GeAsyncTasks.GetItemData(context, NATURE_RUNE_ID, this).execute();

    }

    @Override
    public void onOsBuddySummaryLoaded(HashMap<String, OSBuddySummaryItem> content, long dateModified, boolean cacheExpired) {
        if (adapter == null) {
            adapter = new AlchOverviewAdapter(context, getAlchItems(content));
            listView.setAdapter(adapter);
        }
        else {
            adapter.updateList(getAlchItems(content));
        }

        wasRequesting = false;
        if (!wasRequestingNatureRune) {
            progressBar.setVisibility(View.GONE);
        }
    }

    private List<AlchItem> getAlchItems(HashMap<String, OSBuddySummaryItem> content) {
        List<AlchItem> items = new ArrayList<>();
        for (Map.Entry<String, OSBuddySummaryItem> entry : content.entrySet()) {
            OSBuddySummaryItem item = entry.getValue();
            int lowAlchValue = item.getLowAlchValue();
            if (item.buyPrice < 10 || item.getLowAlchValue() < 1) continue;
            String key = entry.getKey();
            AlchItem alchItem = new AlchItem(key, item.name, item.members, item.buyPrice, lowAlchValue, item.getHighAlchValue(), BUY_LIMITS.get(key));
            items.add(alchItem);
        }
        Collections.sort(items);
        return items;
    }

    @Override
    public void onOsBuddySummaryContextError() {
        wasRequesting = false;
        wasRequestingNatureRune = false;
        showToast(getString(R.string.unexpected_error_try_reopen), Toast.LENGTH_LONG);
    }

    @Override
    public void onOsBuddySummaryLoadFailed(Exception ex) {
        wasRequesting = false;
        if (!wasRequestingNatureRune) {
            progressBar.setVisibility(View.GONE);
        }
    }

    @Override
    public void onGeItemDataLoaded(GrandExchangeData grandExchangeData, boolean isCacheExpired) {
        int natureRunePrice = 0;
        try {
            JSONObject obj = new JSONObject(grandExchangeData.data);
            JSONObject jItem = obj.getJSONObject("item");
            GrandExchangeItem item = GeHelper.getItemFromJson(grandExchangeData.itemId, NATURE_RUNE_BUY_LIMIT, jItem);
            natureRunePrice = item.price;
            natureRuneTextView.setText(String.valueOf(natureRunePrice));
        }
        catch (JSONException ex) {
            Logger.log(ex, "failed to parse nature rune data", grandExchangeData.data);
            showToast(getString(R.string.failed_to_retrieve_nature_rune_price), Toast.LENGTH_LONG);
        }

        if (adapter == null) {
            adapter = new AlchOverviewAdapter(context, new ArrayList<AlchItem>());
            listView.setAdapter(adapter);
        }
        else {
            adapter.updateNatureRunePrice(natureRunePrice);
        }

        wasRequestingNatureRune = false;
        if (!wasRequesting) {
            progressBar.setVisibility(View.GONE);
        }
    }

    @Override
    public void onGeItemDataContextError() {
        this.onOsBuddySummaryContextError();
    }

    @Override
    public void onGeItemDataLoadFailed() {
        showToast(getString(R.string.failed_to_retrieve_nature_rune_price), Toast.LENGTH_LONG);
        wasRequestingNatureRune = false;
        if (!wasRequesting) {
            progressBar.setVisibility(View.GONE);
        }
    }


    @Override
    public boolean wasRequesting() {
        return wasRequesting || wasRequestingNatureRune;
    }

    @Override
    public void cancelRunningTasks() {
        AppController.getInstance().cancelPendingRequests(GRAND_EXCHANGE_REQUEST_TAG);
        AppController.getInstance().cancelPendingRequests(OSBUDDY_SUMMARY_REQUEST_TAG);
        handler.removeCallbacks(runnable);
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
        natureRuneContainer.setVisibility(View.VISIBLE);
        natureRuneContainer.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2));
    }

    private void startHideNavBar() {
        startHideNavBar(2000);
    }

    private void startHideNavBar(int delay) {
        navBarHandler.removeCallbacks(navBarRunnable);
        navBarRunnable = new Runnable() {
            @Override
            public void run() {
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) natureRuneContainer.getLayoutParams();
                int height = natureRuneContainer.getHeight() + params.bottomMargin + params.topMargin;
                natureRuneContainer.animate().translationY(-height).setInterpolator(new AccelerateInterpolator(2));
                Utils.hideKeyboard(context, searchInput);
            }
        };
        navBarHandler.postDelayed(navBarRunnable, delay);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(final Editable s) {
        handler.removeCallbacks(runnable);
        runnable = new Runnable() {
            @Override
            public void run() {
                if (adapter == null)
                    return;

                if (natureRuneTextView.getText().hashCode() == s.hashCode()) {
                    String text = natureRuneTextView.getText().toString();
                    int price = Utils.isNullOrEmpty(text) ? 0 : Integer.parseInt(text);
                    adapter.updateNatureRunePrice(price);
                }
            }
        };
        handler.postDelayed(runnable, 500);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.alch_refresh_button) {
            reloadData();
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        showNavBar();
        return false;
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        showNavBar();
        return false;
    }

    @Override
    public void onClearableEditTextTextChanged(final String text, boolean isEmpty) {
        handler.removeCallbacks(runnable);
        runnable = new Runnable() {
            @Override
            public void run() {
                if (adapter == null)
                    return;

                if (Utils.isNullOrEmpty(text)) {
                    adapter.resetList();
                }
                else {
                    adapter.getFilter().filter(text);
                }
            }
        };
        handler.postDelayed(runnable, 500);
    }

    @Override
    public void onClearableEditTextClear() {
        showNavBar();
    }
}