package com.dennyy.oldschoolcompanion.viewhandlers;

import android.content.Context;
import android.graphics.Paint;
import android.os.SystemClock;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.*;
import com.bumptech.glide.Glide;
import com.dennyy.oldschoolcompanion.AppController;
import com.dennyy.oldschoolcompanion.R;
import com.dennyy.oldschoolcompanion.adapters.GeHistoryAdapter;
import com.dennyy.oldschoolcompanion.adapters.GrandExchangeSearchAdapter;
import com.dennyy.oldschoolcompanion.asynctasks.GeAsyncTasks;
import com.dennyy.oldschoolcompanion.asynctasks.GetOSBuddyExchangeSummaryTask;
import com.dennyy.oldschoolcompanion.asynctasks.LoadGeItemsTask;
import com.dennyy.oldschoolcompanion.customviews.ClearableAutoCompleteTextView;
import com.dennyy.oldschoolcompanion.customviews.DelayedAutoCompleteTextView;
import com.dennyy.oldschoolcompanion.customviews.LineIndicatorButton;
import com.dennyy.oldschoolcompanion.enums.GeGraphDays;
import com.dennyy.oldschoolcompanion.helpers.*;
import com.dennyy.oldschoolcompanion.interfaces.AdapterGeHistoryClickListener;
import com.dennyy.oldschoolcompanion.interfaces.GeListeners;
import com.dennyy.oldschoolcompanion.interfaces.JsonItemsLoadedListener;
import com.dennyy.oldschoolcompanion.interfaces.OSBuddySummaryLoadedListener;
import com.dennyy.oldschoolcompanion.models.GrandExchange.*;
import com.dennyy.oldschoolcompanion.models.OSBuddy.OSBuddySummaryItem;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.dennyy.oldschoolcompanion.asynctasks.GeAsyncTasks.GetGeGraphData.GEGRAPH_REQUEST_TAG;
import static com.dennyy.oldschoolcompanion.asynctasks.GeAsyncTasks.GetGeUpdate.GEUPDATE_REQUEST_TAG;
import static com.dennyy.oldschoolcompanion.asynctasks.GeAsyncTasks.GetItemData.GRAND_EXCHANGE_REQUEST_TAG;
import static com.dennyy.oldschoolcompanion.asynctasks.GetOSBuddyExchangeSummaryTask.OSBUDDY_SUMMARY_REQUEST_TAG;

public class GrandExchangeViewHandler extends BaseViewHandler implements View.OnClickListener, JsonItemsLoadedListener, GeListeners.GeHistoryLoadedListener, AdapterGeHistoryClickListener, OSBuddySummaryLoadedListener, GeListeners.ItemDataLoadedListener, GeListeners.GraphDataLoadedListener, GeListeners.GeUpdateLoadedListener {

    private JsonItem jsonItem;
    private GeGraphDays currentSelectedDays = GeGraphDays.WEEK;
    private boolean wasRequestingGe;
    private boolean wasRequestingGeupdate;
    private boolean wasRequestingGeGraph;
    private boolean wasRequestingOsBuddy;


    private DelayedAutoCompleteTextView autoCompleteTextView;
    private SwipeRefreshLayout refreshLayout;
    private HashMap<String, JsonItem> allItems = new HashMap<>();
    private ListView geHistoryListView;
    private GeHistoryAdapter geHistoryAdapter;
    private LinearLayout geHistoryContainer;
    private GeHistory geHistory = new GeHistory();

    private ImageButton favoriteIcon;
    private HashMap<GeGraphDays, Integer> indicators = new HashMap<>();
    private long lastRefreshTimeMs;
    private JsonItemsLoadedListener jsonItemsLoadedListener;
    private HashMap<String, OSBuddySummaryItem> summaryItems = new HashMap<>();
    private long summaryItemsDateModified;
    private LineChart chart;
    private LinearLayout cacheInfoLinearLayout;

    public GrandExchangeViewHandler(Context context, View view, boolean isFloatingView, JsonItemsLoadedListener listener) {
        super(context, view, isFloatingView);

        initIndicators();
        initChartSettings();
        autoCompleteTextView = ((ClearableAutoCompleteTextView) view.findViewById(R.id.ge_search_input)).getAutoCompleteTextView();
        jsonItemsLoadedListener = listener;
        refreshLayout = view.findViewById(R.id.ge_refresh_layout);
        favoriteIcon = view.findViewById(R.id.ge_favorite_icon);
        favoriteIcon.setOnClickListener(this);
        geHistoryContainer = view.findViewById(R.id.ge_history_container);
        geHistoryListView = geHistoryContainer.findViewById(R.id.ge_history_listview);
        cacheInfoLinearLayout = view.findViewById(R.id.ge_cache_info_wrapper);

        geHistoryContainer.findViewById(R.id.ge_history_clear).setOnClickListener(this);
        new LoadGeItemsTask(context, this).execute();
        new GetOSBuddyExchangeSummaryTask(context, this).execute();
        if (isFloatingView) {
            ImageButton historyIcon = view.findViewById(R.id.ge_history_icon);
            historyIcon.setVisibility(View.VISIBLE);
            historyIcon.setOnClickListener(this);
        }
    }

    @Override
    public void onJsonItemsLoaded(HashMap<String, JsonItem> items) {
        allItems = new HashMap<>(items);
        GrandExchangeSearchAdapter searchAdapter = new GrandExchangeSearchAdapter(context, allItems.values());
        autoCompleteTextView.setAdapter(searchAdapter);
        new GeAsyncTasks.GetHistory(context, false, this).execute();
        initListeners();
    }

    private void initListeners() {
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (allowUpdateItem()) {
                    updateItem(jsonItem, true);
                }
            }
        });

        autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long rowId) {
                Utils.hideKeyboard(context, autoCompleteTextView);
                autoCompleteTextView.forceDismissDropdown();
                jsonItem = (JsonItem) adapterView.getItemAtPosition(position);
                boolean isFavorite = geHistory.isFavorite(jsonItem.id);
                new GeAsyncTasks.InsertOrUpdateGeHistory(context, jsonItem.id, jsonItem.name, isFavorite, GrandExchangeViewHandler.this).execute();
                if (allowUpdateItem()) {
                    updateItem(jsonItem);
                }
            }
        });

        autoCompleteTextView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                return false;
            }
        });
        if (jsonItemsLoadedListener != null) {
            jsonItemsLoadedListener.onJsonItemsLoaded(null);
        }
    }

    @Override
    public void onJsonItemsLoadError() {
        showToast(getString(R.string.exception_occurred, "exception", "loading items from file"), Toast.LENGTH_LONG);
    }

    @Override
    public void onGeHistoryLoaded(GeHistory geHistory) {
        this.geHistory = new GeHistory(geHistory);
        if (geHistoryAdapter == null) {
            geHistoryAdapter = new GeHistoryAdapter(context, geHistory, this);
            geHistoryListView.setAdapter(geHistoryAdapter);
            if (!geHistory.isEmpty()) {
                toggleGeData(false);
            }
        }
        else {
            geHistoryAdapter.updateList(geHistory);
        }
    }

    @Override
    public void onClickGeHistory(String itemId) {
        updateItem(itemId, false);
    }

    @Override
    public void onClickRemoveFavorite(String itemId, String itemName) {
        new GeAsyncTasks.InsertOrUpdateGeHistory(context, itemId, itemName, false, this).execute();
    }

    private void initChartSettings() {
        chart = view.findViewById(R.id.ge_item_graph);
        int white = getColor(R.color.text);
        chart.getDescription().setEnabled(false);
        chart.getAxisRight().setEnabled(false);
        chart.setScaleYEnabled(false);
        chart.setMaxVisibleValueCount(10);
        chart.setDrawBorders(true);
        chart.setBorderColor(white);
        chart.getAxisLeft().setGranularity(1);
        chart.getLegend().setTextColor(white);
        chart.getAxisLeft().setTextColor(white);
        chart.getXAxis().setTextColor(white);
        chart.setNoDataText(getString(R.string.ge_graph_loading));
        Paint paint = chart.getPaint(Chart.PAINT_INFO);
        paint.setColor(getColor(R.color.text));
    }

    public void updateItem(final String id, boolean restoreFromInstanceState) {
        jsonItem = allItems.get(id);
        if (jsonItem == null) {
            showToast(getString(R.string.unexpected_error_try_reopen), Toast.LENGTH_SHORT);
            Logger.log(id, new NullPointerException("jsonitem not found"));
            return;
        }
        autoCompleteTextView.setText(jsonItem.name);
        autoCompleteTextView.forceDismissDropdown();
        if (restoreFromInstanceState) {
            new GeAsyncTasks.GetCompleteItemData(context, jsonItem.id, new GeListeners.CompleteItemDataLoadedListener() {
                @Override
                public void onItemDataLoaded(ItemData itemData) {
                    if (itemData.geData != null) {
                        handleGeData(itemData.geData.data);
                        toggleGeData(true);
                    }
                    if (itemData.graphData != null) {
                        handleGeGraphData(itemData.graphData.data);
                    }
                    if (itemData.geUpdate != null) {
                        handleGeUpdateData(itemData.geUpdate.data);
                    }
                    summaryItems = itemData.osbSummary;
                    handleOSBuddyData();
                }

                @Override
                public void onItemDataLoadFailed() {
                    Logger.log(id, new RuntimeException("failed to restore item from savedinstancestate"));
                    showToast(getString(R.string.unexpected_error_try_reopen), Toast.LENGTH_SHORT);
                }
            }).execute();
        }
        else {
            updateItem(jsonItem);
        }
    }

    private void updateItem(final JsonItem jsonItem) {
        updateItem(jsonItem, false);
    }

    private void updateItem(final JsonItem jsonItem, boolean forceCacheReload) {
        activateRefreshCooldown();
        refreshLayout.setRefreshing(true);
        wasRequestingGe = true;
        new GeAsyncTasks.GetItemData(context, jsonItem.id, forceCacheReload, this).execute();
        loadGraph(forceCacheReload);
        loadGeUpdate(forceCacheReload);
        loadOSBuddyExchange();
    }

    @Override
    public void onGeItemDataLoaded(GrandExchangeData grandExchangeData, boolean isCacheExpired) {
        cacheInfoLinearLayout.setVisibility(View.GONE);
        toggleGeData(true);
        handleGeData(grandExchangeData.data);
        resetGeLoading();
    }

    @Override
    public void onGeItemDataContextError() {
        this.onOsBuddySummaryContextError();
        resetGeLoading();
    }

    @Override
    public void onGeItemDataLoadFailed() {
        showToast(getString(R.string.failed_to_obtain_data, "GE item data", getString(R.string.network_error)), Toast.LENGTH_LONG);
        resetGeLoading();

    }

    private void resetGeLoading() {
        refreshLayout.setRefreshing(false);
        wasRequestingGe = false;
    }


    private void toggleFavoriteItem(String itemId) {
        boolean isFavorite = geHistory.isFavorite(itemId);
        favoriteIcon.setBackground(ContextCompat.getDrawable(context, isFavorite ? R.drawable.baseline_star_white_24 : R.drawable.baseline_star_border_white_24));
        favoriteIcon.setVisibility(View.VISIBLE);
    }

    private void handleGeData(String geItemData) {
        try {
            JSONObject obj = new JSONObject(geItemData);
            JSONObject jItem = obj.getJSONObject("item");
            GrandExchangeItem item = GeHelper.getItemFromJson(jsonItem.id, jsonItem.limit, jItem);
            int red = getColor(R.color.red);
            int green = getColor(R.color.green);
            toggleFavoriteItem(item.id);
            if (Utils.isValidContextForGlide(context)) {
                Glide.with(context).load(Constants.GE_IMG_LARGE_URL + item.id).into((ImageView) view.findViewById(R.id.ge_item_icon));
            }
            ((ImageView) view.findViewById(R.id.ge_item_members_indicator)).setImageDrawable(item.members ? getDrawable(R.drawable.members) : null);

            ((TextView) view.findViewById(R.id.ge_item_name)).setText(item.name);
            ((TextView) view.findViewById(R.id.ge_item_examine)).setText(item.description);
            ((TextView) view.findViewById(R.id.ge_item_price)).setText(RsUtils.kmbt(item.price));
            ((TextView) view.findViewById(R.id.ge_buy_limit)).setText(item.limit < 0 ? getString(R.string.unknown) : RsUtils.kmbt(item.limit, 0));

            TextView itemChangeTextView = view.findViewById(R.id.ge_item_change);
            TextView itemChangePercentTextView = view.findViewById(R.id.ge_item_change_percent);
            itemChangeTextView.setText(String.format("%s%s", item.change < 0 ? "" : "+", RsUtils.kmbt(item.change, 1)));
            itemChangePercentTextView.setText(String.format("%s%s%%", item.changePercent < 0 ? "" : "+", String.valueOf((int) Math.round(item.changePercent))));
            itemChangeTextView.setTextColor(item.change < 0 ? red : green);
            itemChangePercentTextView.setTextColor(item.change < 0 ? red : green);

            TextView item30daysTextView = view.findViewById(R.id.ge_item_30days);
            TextView item30daysPercentTextView = view.findViewById(R.id.ge_item_30days_percent);
            item30daysTextView.setText(String.format("%s%s", item.day30change < 0 ? "" : "+", RsUtils.kmbt(item.day30change, 1)));
            item30daysPercentTextView.setText(String.format("%s%s%%", item.day30change < 0 ? "" : "+", String.valueOf((int) Math.round(item.day30changePercent))));
            item30daysTextView.setTextColor(item.day30change < 0 ? red : green);
            item30daysPercentTextView.setTextColor(item.day30change < 0 ? red : green);

            TextView item90daysTextView = view.findViewById(R.id.ge_item_90days);
            TextView item90daysPercentTextView = view.findViewById(R.id.ge_item_90days_percent);
            item90daysTextView.setText(String.format("%s%s", item.day90change < 0 ? "" : "+", RsUtils.kmbt(item.day90change, 1)));
            item90daysPercentTextView.setText(String.format("%s%s%%", item.day90change < 0 ? "" : "+", String.valueOf((int) Math.round(item.day90changePercent))));
            item90daysTextView.setTextColor(item.day90change < 0 ? red : green);
            item90daysPercentTextView.setTextColor(item.day90change < 0 ? red : green);


            TextView item180daysTextView = view.findViewById(R.id.ge_item_180days);
            TextView item180daysPercentTextview = view.findViewById(R.id.ge_item_180days_percent);
            item180daysTextView.setText(String.format("%s%s", item.day180change < 0 ? "" : "+", RsUtils.kmbt(item.day180change, 1)));
            item180daysPercentTextview.setText(String.format("%s%s%%", item.day180change < 0 ? "" : "+", String.valueOf((int) Math.round(item.day180changePercent))));
            item180daysTextView.setTextColor(item.day180change < 0 ? red : green);
            item180daysPercentTextview.setTextColor(item.day180change < 0 ? red : green);
        }
        catch (Exception ex) {
            Logger.log(ex);
            showToast(getString(R.string.unexpected_error_try_reopen), Toast.LENGTH_LONG);
        }
    }

    public boolean allowUpdateItem() {
        long refreshPeriod = System.currentTimeMillis() - lastRefreshTimeMs;
        if (jsonItem == null || autoCompleteTextView.getText().toString().isEmpty()) {
            showToast(getString(R.string.empty_item_error), Toast.LENGTH_SHORT);
            refreshLayout.setRefreshing(false);
            return false;
        }
        if (refreshPeriod < Constants.REFRESH_COOLDOWN_MS) {
            double timeLeft = (Constants.REFRESH_COOLDOWN_MS - refreshPeriod) / 1000;
            showToast(getString(R.string.wait_before_refresh, (int) Math.ceil(timeLeft + 0.5)), Toast.LENGTH_SHORT);
            refreshLayout.setRefreshing(false);
            return false;
        }
        if (jsonItem != null && jsonItem.id.equals("-1")) {
            refreshLayout.setRefreshing(false);
            return false;
        }
        return true;
    }

    private void loadGeUpdate(boolean forceCacheReload) {
        wasRequestingGeupdate = true;
        new GeAsyncTasks.GetGeUpdate(context, forceCacheReload, this).execute();
    }

    @Override
    public void onGeUpdateLoaded(GrandExchangeUpdateData geUpdateData, boolean cacheExpired) {
        handleGeUpdateData(geUpdateData.data);
        wasRequestingGeupdate = false;
    }

    @Override
    public void onGeUpdateContextError() {
        showToast(getString(R.string.unexpected_error_try_reopen), Toast.LENGTH_LONG);
        wasRequestingGeupdate = false;
    }

    @Override
    public void onGeUpdateLoadFailed() {
        TextView geupdateTextView = view.findViewById(R.id.geupdate);
        geupdateTextView.setText(getString(R.string.failed_to_obtain_data, "ge update data", "network error"));
        wasRequestingGeupdate = false;
    }

    private void handleGeUpdateData(String geupdateData) {
        TextView geupdateTextView = view.findViewById(R.id.geupdate);
        try {
            JSONObject obj = new JSONObject(geupdateData);
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            format.setTimeZone(TimeZone.getTimeZone("UTC"));

            Date date = format.parse(obj.getString("datetime"));

            SimpleDateFormat displayFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss", Locale.getDefault());
            displayFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            geupdateTextView.setText(String.format(getString(R.string.time_utc), displayFormat.format(date)));

            TextView geupdateTimeAgoTextView = view.findViewById(R.id.geupdate_time_ago);
            long millisAgo = new Date().getTime() - date.getTime();
            String timeAgo = getString(R.string.geupdate_time_ago, TimeUnit.MILLISECONDS.toHours(millisAgo), TimeUnit.MILLISECONDS.toMinutes(millisAgo) % TimeUnit.HOURS.toMinutes(1));
            geupdateTimeAgoTextView.setText(timeAgo);
        }
        catch (JSONException | ParseException ex) {
            Logger.log(geupdateData, ex);
            geupdateTextView.setText(getString(R.string.exception_occurred, ex.getClass().getCanonicalName(), "getting geupdate data"));
        }
    }

    private void loadGraph(boolean forceCacheReload) {
        wasRequestingGeGraph = true;
        chart.setNoDataText(getString(R.string.ge_graph_loading));
        chart.clear();
        new GeAsyncTasks.GetGeGraphData(context, jsonItem.id, forceCacheReload, this).execute();
    }

    @Override
    public void onGeGraphDataLoaded(String graphData, boolean isCacheExpired) {
        handleGeGraphData(graphData);
        wasRequestingGeGraph = false;
    }

    @Override
    public void onGeGraphDataContextError() {
        showToast(getString(R.string.unexpected_error_try_reopen), Toast.LENGTH_LONG);
        wasRequestingGeGraph = false;
    }

    @Override
    public void onGeGraphDataLoadFailed() {
        chart.setNoDataText(getString(R.string.network_error));
        chart.invalidate();
        wasRequestingGeGraph = false;
    }

    private void handleGeGraphData(String geGraphData) {
        try {
            JSONObject dailyGraphData = new JSONObject(geGraphData).getJSONObject("daily");
            List<Entry> data = new ArrayList<>();
            for (Iterator<String> iter = dailyGraphData.keys(); iter.hasNext(); ) {
                String key = iter.next();
                float time = Float.parseFloat(key);
                data.add(new Entry(time, dailyGraphData.getInt(key)));
            }
            CustomLineDataSet dataSet = new CustomLineDataSet(data, getString(R.string.price), getColor(R.color.ge_graph_price));

            JSONObject trendGraphData = new JSONObject(geGraphData).getJSONObject("average");
            List<Entry> trendData = new ArrayList<>();
            for (Iterator<String> iter = trendGraphData.keys(); iter.hasNext(); ) {
                String key = iter.next();
                float time = Float.parseFloat(key);
                trendData.add(new Entry(time, trendGraphData.getInt(key)));
            }
            CustomLineDataSet dataSet2 = new CustomLineDataSet(trendData, getString(R.string.trend), getColor(R.color.ge_graph_trend));

            LineData lineData = new LineData(dataSet, dataSet2);
            XAxis xAxis = chart.getXAxis();
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setAvoidFirstLastClipping(true);
            xAxis.setValueFormatter(new IAxisValueFormatter() {

                @Override
                public String getFormattedValue(float value, AxisBase axis) {
                    long unixSeconds = (long) value;
                    Date date = new Date(unixSeconds);
                    SimpleDateFormat sdf = new SimpleDateFormat("MMM dd", Locale.getDefault());
                    return sdf.format(date);
                }
            });
            chart.setData(lineData);
            zoomGraphToDays(currentSelectedDays);
        }
        catch (JSONException ex) {
            Logger.log(geGraphData, ex);
            chart.setNoDataText(getString(R.string.exception_occurred, ex.getClass().getCanonicalName(), "parsing ge graph data"));
            chart.invalidate();
        }
    }

    private void zoomGraphToDays(GeGraphDays days) {
        chart.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN, 0, 0, 0));
        chart.fitScreen();
        chart.setVisibleXRangeMaximum((long) days.getDays() * 86400000);
        chart.moveViewToX(chart.getXAxis().getAxisMaximum());
        chart.invalidate();

        // update indicators
        for (Map.Entry<GeGraphDays, Integer> entry : indicators.entrySet()) {
            ((LineIndicatorButton) view.findViewById(entry.getValue())).setActive(false);
        }
        ((LineIndicatorButton) view.findViewById(indicators.get(days))).setActive(true);
    }

    private void loadOSBuddyExchange() {
        setOSBuddyText("...", false);
        boolean cacheExpired = Math.abs(System.currentTimeMillis() - summaryItemsDateModified) > Constants.OSBUDDY_SUMMARY_CACHE_DURATION;
        if (summaryItems.isEmpty() || cacheExpired) {
            wasRequestingOsBuddy = true;
            new GetOSBuddyExchangeSummaryTask(context, this).execute();
        }
        else {
            handleOSBuddyData();
        }
    }

    private void handleOSBuddyData() {
        String error = getString(R.string.osb_error);
        if (jsonItem == null) {
            setOSBuddyText(error, true);
            return;
        }
        OSBuddySummaryItem summaryItem = summaryItems.get(jsonItem.id);
        if (summaryItem == null) {
            setOSBuddyText(error, true);
            return;
        }

        int buyPrice = summaryItem.buyPrice;
        int sellPrice = summaryItem.sellPrice;
        String buyText = buyPrice < 1 ? getString(R.string.inactive) : RsUtils.kmbt(buyPrice);
        String sellText = sellPrice < 1 ? getString(R.string.inactive) : RsUtils.kmbt(sellPrice);
        setOSBuddyText(buyText, sellText, false);
        setOsBuddyAlchText(summaryItem.storePrice);
    }

    private void setOSBuddyText(String buyText, String sellText, boolean isError) {
        TextView osbBuyPriceTextView = view.findViewById(R.id.osb_buy_price);
        TextView osbSellPriceTextView = view.findViewById(R.id.osb_sell_price);
        osbBuyPriceTextView.setTextColor(getColor(isError ? R.color.red : R.color.text));
        osbSellPriceTextView.setTextColor(getColor(isError ? R.color.red : R.color.text));
        osbBuyPriceTextView.setText(buyText);
        osbSellPriceTextView.setText(sellText);
    }

    private void setOSBuddyText(String text, boolean isError) {
        setOSBuddyText(text, text, isError);
    }

    private void setOsBuddyAlchText(int storePrice) {
        TextView highAlchTextView = view.findViewById(R.id.ge_item_high_alch);
        TextView lowAlchTextView = view.findViewById(R.id.ge_item_low_alch);
        double lowAlch = Math.floor(storePrice * 0.4);
        double highAlch = Math.floor(storePrice * 0.6);
        lowAlchTextView.setText(RsUtils.kmbt(lowAlch < 1 ? 1 : lowAlch));
        highAlchTextView.setText(RsUtils.kmbt(highAlch < 1 ? 1 : highAlch));
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.ge_favorite_icon && jsonItem != null) {
            boolean isFavorite = geHistory.isFavorite(jsonItem.id);
            geHistory.toggleFavorite(jsonItem.id, !isFavorite);
            new GeAsyncTasks.InsertOrUpdateGeHistory(context, jsonItem.id, jsonItem.name, !isFavorite, this).execute();
            toggleFavoriteItem(jsonItem.id);
        }
        else if (id == R.id.ge_history_clear) {
            new GeAsyncTasks.GetHistory(context, true, this).execute();
        }
        else if (id == R.id.ge_history_icon) {
            toggleGeData();
        }
        else {
            GeGraphDays days;
            switch (id) {
                case R.id.ge_graph_show_quarter:
                    days = GeGraphDays.QUARTER;
                    break;
                case R.id.ge_graph_show_month:
                    days = GeGraphDays.MONTH;
                    break;
                case R.id.ge_graph_show_week:
                    days = GeGraphDays.WEEK;
                    break;
                default:
                    days = GeGraphDays.ALL;
            }
            if (currentSelectedDays != days) {
                currentSelectedDays = days;
                zoomGraphToDays(days);
            }
        }
    }

    private void activateRefreshCooldown() {
        lastRefreshTimeMs = System.currentTimeMillis();
    }

    @Override
    public void cancelRunningTasks() {
        AppController.getInstance().cancelPendingRequests(GRAND_EXCHANGE_REQUEST_TAG);
        AppController.getInstance().cancelPendingRequests(GEUPDATE_REQUEST_TAG);
        AppController.getInstance().cancelPendingRequests(GEGRAPH_REQUEST_TAG);
        AppController.getInstance().cancelPendingRequests(OSBUDDY_SUMMARY_REQUEST_TAG);
    }

    @Override
    public boolean wasRequesting() {
        return wasRequestingGe || wasRequestingGeGraph || wasRequestingGeupdate || wasRequestingOsBuddy;
    }

    @Override
    public void onGeHistoryLoadFailed() {

    }

    private void initIndicators() {
        indicators.put(GeGraphDays.ALL, R.id.ge_graph_show_all);
        indicators.put(GeGraphDays.QUARTER, R.id.ge_graph_show_quarter);
        indicators.put(GeGraphDays.MONTH, R.id.ge_graph_show_month);
        indicators.put(GeGraphDays.WEEK, R.id.ge_graph_show_week);
        for (Map.Entry<GeGraphDays, Integer> entry : indicators.entrySet()) {
            view.findViewById(entry.getValue()).setOnClickListener(this);
        }
    }

    public String getItemId() {
        return jsonItem != null ? jsonItem.id : null;
    }

    public void toggleGeData() {
        if (jsonItem == null) {
            toggleGeData(false);
        }
        else {
            toggleGeData(!isGeDataVisible());
        }
    }

    public void toggleGeData(boolean visible) {
        view.findViewById(R.id.ge_data).setVisibility(visible ? View.VISIBLE : View.GONE);
        favoriteIcon.setVisibility(visible ? View.VISIBLE : View.GONE);
        geHistoryContainer.setVisibility(visible ? View.GONE : View.VISIBLE);
    }

    public boolean isGeDataVisible() {
        return view.findViewById(R.id.ge_data).getVisibility() == View.VISIBLE;
    }

    @Override
    public void onOsBuddySummaryLoaded(HashMap<String, OSBuddySummaryItem> content, long dateModified, boolean cacheExpired) {
        summaryItemsDateModified = dateModified;
        summaryItems = content;
        handleOSBuddyData();
    }

    @Override
    public void onOsBuddySummaryContextError() {
        showToast(getString(R.string.unexpected_error_try_reopen), Toast.LENGTH_LONG);
    }

    @Override
    public void onOsBuddySummaryLoadFailed(Exception ex) {
        setOSBuddyText(getString(R.string.osb_parse_error), true);
    }
}
