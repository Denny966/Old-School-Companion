package com.dennyy.osrscompanion.layouthandlers;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.NoConnectionError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.bumptech.glide.Glide;
import com.dennyy.osrscompanion.AppController;
import com.dennyy.osrscompanion.R;
import com.dennyy.osrscompanion.adapters.GrandExchangeSearchAdapter;
import com.dennyy.osrscompanion.customviews.ClearableAutoCompleteTextView;
import com.dennyy.osrscompanion.customviews.LineIndicatorButton;
import com.dennyy.osrscompanion.enums.GeGraphDays;
import com.dennyy.osrscompanion.helpers.AppDb;
import com.dennyy.osrscompanion.helpers.Constants;
import com.dennyy.osrscompanion.helpers.RsUtils;
import com.dennyy.osrscompanion.helpers.Utils;
import com.dennyy.osrscompanion.models.GrandExchange.CustomLineDataSet;
import com.dennyy.osrscompanion.models.GrandExchange.GrandExchangeData;
import com.dennyy.osrscompanion.models.GrandExchange.GrandExchangeGraphData;
import com.dennyy.osrscompanion.models.GrandExchange.GrandExchangeItem;
import com.dennyy.osrscompanion.models.GrandExchange.GrandExchangeUpdateData;
import com.dennyy.osrscompanion.models.GrandExchange.JsonItem;
import com.dennyy.osrscompanion.models.OSBuddy.OSBuddyExchangeData;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class GrandExchangeViewHandler extends BaseViewHandler implements View.OnClickListener {
    public JsonItem jsonItem;
    public String geItemData;
    public String geupdateData;
    public String geGraphData;
    public String osBuddyItemData;
    public GrandExchangeSearchAdapter adapter;
    public ArrayList<JsonItem> searchAdapterItems = new ArrayList<>();
    public GeGraphDays currentSelectedDays = GeGraphDays.ALL;
    public int selectedAdapterIndex;
    public boolean wasRequestingGe;
    public boolean wasRequestingGeupdate;
    public boolean wasRequestingGegraph;
    public boolean wasRequestingOsBuddy;

    private static final String GE_REQUEST_TAG = "grandexchangerequest";
    private static final String GEUPDATE_REQUEST_TAG = "grandexchangeupdaterequest";
    private static final String GEGRAPH_REQUEST_TAG = "grandexchangegraphrequest";
    private static final String OSBUDDY_EXCHANGE_REQUEST_TAG = "osbuddy_exchange_request_tag";

    private AutoCompleteTextView autoCompleteTextView;
    private SwipeRefreshLayout refreshLayout;
    private ArrayList<JsonItem> allItems = new ArrayList<>();

    private HashMap<GeGraphDays, Integer> indicators;
    private long lastRefreshTimeMs;
    private ItemsLoadedCallback itemsLoadedCallback;

    public GrandExchangeViewHandler(final Context context, final View view, final ItemsLoadedCallback itemsLoadedCallback) {
        super(context, view);

        indicators = new HashMap<>();
        indicators.put(GeGraphDays.ALL, R.id.ge_graph_show_all);
        indicators.put(GeGraphDays.QUARTER, R.id.ge_graph_show_quarter);
        indicators.put(GeGraphDays.MONTH, R.id.ge_graph_show_month);
        indicators.put(GeGraphDays.WEEK, R.id.ge_graph_show_week);

        new LoadItems(context, new ItemsLoadedCallback() {
            @Override
            public void onItemsLoaded(ArrayList<JsonItem> items) {
                allItems = new ArrayList<>(items);
                updateView(view);
                if (itemsLoadedCallback != null)
                    itemsLoadedCallback.onItemsLoaded(null);
            }

            @Override
            public void onLoadError() {
                showToast(getResources().getString(R.string.exception_occurred, "exception", "loading items from file"), Toast.LENGTH_LONG);
            }
        }).execute();

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Utils.hideKeyboard(context, GrandExchangeViewHandler.super.view);
            }
        }, 200);
    }


    public void updateView(View view) {
        this.view = view;
        initChartSettings(view);
        autoCompleteTextView = ((ClearableAutoCompleteTextView) view.findViewById(R.id.ge_search_input)).getAutoCompleteTextView();
        if (jsonItem != null)
            autoCompleteTextView.setText(jsonItem.name);
        refreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.ge_refresh_layout);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (allowUpdateItem()) {
                    updateItem();
                }
            }
        });
        for (Map.Entry<GeGraphDays, Integer> entry : indicators.entrySet()) {
            view.findViewById(entry.getValue()).setOnClickListener(this);
        }
        if (adapter == null)
            adapter = new GrandExchangeSearchAdapter(getActivity(), (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE), allItems);
        autoCompleteTextView.setAdapter(adapter);
        autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Utils.hideKeyboard(getActivity(), autoCompleteTextView);
                selectedAdapterIndex = i;
                if (allowUpdateItem())
                    updateItem();
            }
        });

        autoCompleteTextView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                return false;
            }
        });
        autoCompleteTextView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                autoCompleteTextView.setThreshold(3);
                return false;
            }
        });
        autoCompleteTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().trim().length() == 0) {
                    adapter.resetItems();
                    searchAdapterItems.clear();
                    searchAdapterItems.trimToSize();
                }
            }
        });
    }

    private void initChartSettings(View view) {
        LineChart chart = (LineChart) view.findViewById(R.id.ge_item_graph);
        int white = getResources().getColor(R.color.text);
        chart.getDescription().setEnabled(false);
        chart.getAxisRight().setEnabled(false);
        chart.setScaleYEnabled(false);
        chart.setMaxVisibleValueCount(10);
        chart.setDrawBorders(true);
        chart.setBorderColor(white);
        chart.getLegend().setTextColor(white);
        chart.getAxisLeft().setTextColor(white);
        chart.getXAxis().setTextColor(white);
        chart.setNoDataText(getResources().getString(R.string.ge_graph_loading));
        Paint paint = chart.getPaint(Chart.PAINT_INFO);
        paint.setColor(getResources().getColor(R.color.text));
    }

    public void updateItem() {
        jsonItem = adapter.getItem(selectedAdapterIndex);
        activateRefreshCooldown();
        refreshLayout.setRefreshing(true);
        wasRequestingGe = true;
        final LinearLayout cacheInfoLinearLayout = view.findViewById(R.id.ge_cache_info_wrapper);
        final TextView cacheInfoTextView = view.findViewById(R.id.ge_cache_info);
        Utils.getString(Constants.GE_ITEM_URL + jsonItem.id, GE_REQUEST_TAG, new Utils.VolleyCallback() {
            @Override
            public void onSuccess(String result) {
                geItemData = result;
                cacheInfoLinearLayout.setVisibility(View.GONE);
                view.findViewById(R.id.ge_data).setVisibility(View.VISIBLE);
                AppDb.getInstance(getActivity()).insertOrUpdateGrandExchangeData(jsonItem.id, result);
                loadGraph();
                loadGeupdate();
                loadOSBuddyExchange();
                handleGeData();
            }

            @Override
            public void onError(VolleyError error) {
                if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                    GrandExchangeData cachedData = AppDb.getInstance(getActivity()).getGrandExchangeData(Integer.parseInt(jsonItem.id));
                    if (cachedData == null) {
                        showToast(getResources().getString(R.string.failed_to_obtain_data, "ge item data", getResources().getString(R.string.network_error)), Toast.LENGTH_LONG);
                        return;
                    }
                    geItemData = cachedData.data;
                    String cacheText = getResources().getString(R.string.using_cached_data, Utils.convertTime(cachedData.dateModified));
                    cacheInfoTextView.setText(cacheText);
                    cacheInfoLinearLayout.setVisibility(View.VISIBLE);
                    view.findViewById(R.id.ge_data).setVisibility(View.VISIBLE);
                    loadGraph();
                    loadGeupdate();
                    loadOSBuddyExchange();
                    handleGeData();
                }
                else {
                    showToast(getResources().getString(R.string.failed_to_obtain_data, "ge item data", error.getMessage()), Toast.LENGTH_LONG);
                }
            }

            @Override
            public void always() {
                refreshLayout.setRefreshing(false);
                wasRequestingGe = false;
            }
        });
    }

    public void handleGeData() {
        try {
            JSONObject obj = new JSONObject(geItemData);
            JSONObject jItem = obj.getJSONObject("item");
            GrandExchangeItem item = getItemFromJson(jsonItem.id, jItem);
            int red = getResources().getColor(R.color.red);
            int green = getResources().getColor(R.color.green);

            Glide.with(getActivity()).load(Constants.GE_IMG_LARGE_URL + item.id).into((ImageView) view.findViewById(R.id.ge_item_icon));
            if (item.members) {
                ((ImageView) view.findViewById(R.id.ge_item_members_indicator)).setImageDrawable(getResources().getDrawable(R.drawable.members));
            }

            ((TextView) view.findViewById(R.id.ge_item_name)).setText(item.name);
            ((TextView) view.findViewById(R.id.ge_item_examine)).setText(item.description);
            ((TextView) view.findViewById(R.id.ge_item_price)).setText(RsUtils.kmbt(item.price, 2));

            TextView itemChangeTextView = (TextView) view.findViewById(R.id.ge_item_change);
            TextView itemChangePercentTextView = (TextView) view.findViewById(R.id.ge_item_change_percent);
            itemChangeTextView.setText(String.format("%s%s", item.change < 0 ? "" : "+", RsUtils.kmbt(item.change)));
            itemChangePercentTextView.setText(String.format("%s%s%%", item.changePercent < 0 ? "" : "+", String.valueOf((int) Math.round(item.changePercent))));
            itemChangeTextView.setTextColor(item.change < 0 ? red : green);
            itemChangePercentTextView.setTextColor(item.change < 0 ? red : green);

            TextView item30daysTextView = (TextView) view.findViewById(R.id.ge_item_30days);
            TextView item30daysPercentTextView = (TextView) view.findViewById(R.id.ge_item_30days_percent);
            item30daysTextView.setText(String.format("%s%s", item.day30change < 0 ? "" : "+", RsUtils.kmbt(item.day30change)));
            item30daysPercentTextView.setText(String.format("%s%s%%", item.day30change < 0 ? "" : "+", String.valueOf((int) Math.round(item.day30changePercent))));
            item30daysTextView.setTextColor(item.day30change < 0 ? red : green);
            item30daysPercentTextView.setTextColor(item.day30change < 0 ? red : green);

            TextView item90daysTextView = (TextView) view.findViewById(R.id.ge_item_90days);
            TextView item90daysPercentTextView = (TextView) view.findViewById(R.id.ge_item_90days_percent);
            item90daysTextView.setText(String.format("%s%s", item.day90change < 0 ? "" : "+", RsUtils.kmbt(item.day90change)));
            item90daysPercentTextView.setText(String.format("%s%s%%", item.day90change < 0 ? "" : "+", String.valueOf((int) Math.round(item.day90changePercent))));
            item90daysTextView.setTextColor(item.day90change < 0 ? red : green);
            item90daysPercentTextView.setTextColor(item.day90change < 0 ? red : green);


            TextView item180daysTextView = (TextView) view.findViewById(R.id.ge_item_180days);
            TextView item180daysPercentTextview = (TextView) view.findViewById(R.id.ge_item_180days_percent);
            item180daysTextView.setText(String.format("%s%s", item.day180change < 0 ? "" : "+", RsUtils.kmbt(item.day180change)));
            item180daysPercentTextview.setText(String.format("%s%s%%", item.day180change < 0 ? "" : "+", String.valueOf((int) Math.round(item.day180changePercent))));
            item180daysTextView.setTextColor(item.day180change < 0 ? red : green);
            item180daysPercentTextview.setTextColor(item.day180change < 0 ? red : green);

            TextView highAlchTextView = (TextView) view.findViewById(R.id.ge_item_high_alch);
            TextView lowAlchTextView = (TextView) view.findViewById(R.id.ge_item_low_alch);
            double lowAlch = Math.floor(Integer.valueOf(jsonItem.store) * 0.4);
            double highAlch = Math.floor(Integer.valueOf(jsonItem.store) * 0.6);
            lowAlchTextView.setText(RsUtils.kmbt(lowAlch < 1 ? 1 : lowAlch, 2));
            highAlchTextView.setText(RsUtils.kmbt(highAlch < 1 ? 1 : highAlch, 2));

        }
        catch (JSONException e) {
            showToast(getResources().getString(R.string.exception_occurred, e.getClass().getCanonicalName(), "parsing ge item data"), Toast.LENGTH_LONG);
        }
    }

    private GrandExchangeItem getItemFromJson(String id, JSONObject jsonItem) {
        GrandExchangeItem geItem = new GrandExchangeItem();
        try {
            geItem.id = id;
            geItem.name = jsonItem.getString("name");
            geItem.description = jsonItem.getString("description");
            geItem.members = jsonItem.getBoolean("members");
            geItem.price = RsUtils.revkmbt(jsonItem.getJSONObject("current").getString("price").replace(",", ""));
            geItem.change = RsUtils.revkmbt(jsonItem.getJSONObject("today").getString("price").replace(",", ""));
            geItem.changePercent = RsUtils.getGEPercentChange(geItem.price, geItem.change);
            geItem.day30changePercent = RsUtils.revkmbt(jsonItem.getJSONObject("day30").getString("change").replace("%", ""));
            geItem.day30change = RsUtils.getGEPriceChange(geItem.price, geItem.day30changePercent);
            geItem.day90changePercent = RsUtils.revkmbt(jsonItem.getJSONObject("day90").getString("change").replace("%", ""));
            geItem.day90change = RsUtils.getGEPriceChange(geItem.price, geItem.day90changePercent);
            geItem.day180changePercent = RsUtils.revkmbt(jsonItem.getJSONObject("day180").getString("change").replace("%", ""));
            geItem.day180change = RsUtils.getGEPriceChange(geItem.price, geItem.day180changePercent);
        }
        catch (JSONException e) {
            showToast(getResources().getString(R.string.exception_occurred, e.getClass().getCanonicalName(), "parsing json to object"), Toast.LENGTH_LONG);
        }
        return geItem;
    }

    private void setMembersIndicator(ImageView view) {
        try {
            InputStream ims = getActivity().getAssets().open("members.png");
            Drawable d = Drawable.createFromStream(ims, null);
            view.setImageDrawable(d);
        }
        catch (IOException ex) {
            showToast(getResources().getString(R.string.exception_occurred, ex.getClass().getCanonicalName(), "loading members indicator"), Toast.LENGTH_LONG);
        }
    }

    public boolean allowUpdateItem() {
        long refreshPeriod = System.currentTimeMillis() - lastRefreshTimeMs;
        if (autoCompleteTextView.getText().toString().isEmpty()) {
            showToast(getResources().getString(R.string.empty_item_error), Toast.LENGTH_SHORT);
            refreshLayout.setRefreshing(false);
            return false;
        }
        if (refreshPeriod < Constants.REFRESH_COOLDOWN_MS) {
            double timeLeft = (Constants.REFRESH_COOLDOWN_MS - refreshPeriod) / 1000;
            showToast(getResources().getString(R.string.wait_before_refresh, (int) Math.ceil(timeLeft + 0.5)), Toast.LENGTH_SHORT);
            refreshLayout.setRefreshing(false);
            return false;
        }
        JsonItem jsonItem = adapter.getItem(selectedAdapterIndex);
        if (jsonItem != null && jsonItem.id.equals("-1")) {
            refreshLayout.setRefreshing(false);
            return false;
        }
        return true;
    }

    public void loadGeupdate() {
        wasRequestingGeupdate = true;
        Utils.getString(Constants.GE_UPDATE_URL, GEUPDATE_REQUEST_TAG, new Utils.VolleyCallback() {
            @Override
            public void onSuccess(String result) {
                AppDb.getInstance(getActivity()).updateGrandExchangeUpdateData(result);
                geupdateData = result;
                handleGeUpdateData();
            }

            @Override
            public void onError(VolleyError error) {
                if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                    GrandExchangeUpdateData cachedData = AppDb.getInstance(getActivity()).getGrandExchangeUpdateData();

                    if (cachedData == null) {
                        showToast(getResources().getString(R.string.failed_to_obtain_data, "ge update data", getResources().getString(R.string.network_error)), Toast.LENGTH_LONG);
                        return;
                    }
                    geupdateData = cachedData.data;
                    handleGeUpdateData();
                }
                else {
                    showToast(getResources().getString(R.string.failed_to_obtain_data, "geupdate data", error.getMessage()), Toast.LENGTH_LONG);
                }
            }

            @Override
            public void always() {
                wasRequestingGeupdate = false;
            }
        });
    }

    public void handleGeUpdateData() {
        try {
            JSONObject obj = new JSONObject(geupdateData);
            TextView geupdateTextView = view.findViewById(R.id.geupdate);
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date date = format.parse(obj.getString("datetime"));
            geupdateTextView.setText(String.format(getResources().getString(R.string.time_utc), DateFormat.getDateTimeInstance().format(date)));

            TextView geupdateTimeAgoTextView = view.findViewById(R.id.geupdate_time_ago);
            long millisAgo = new Date().getTime() - date.getTime();
            String timeAgo = getResources().getString(R.string.geupdate_time_ago, TimeUnit.MILLISECONDS.toHours(millisAgo), TimeUnit.MILLISECONDS.toMinutes(millisAgo) % TimeUnit.HOURS.toMinutes(1));
            geupdateTimeAgoTextView.setText(timeAgo);

        }
        catch (JSONException | ParseException e) {
            showToast(getResources().getString(R.string.exception_occurred, e.getClass().getCanonicalName(), "parsing geupdate data"), Toast.LENGTH_LONG);
        }
    }

    public void loadGraph() {
        final String id = jsonItem.id;
        wasRequestingGegraph = true;
        Utils.getString(Constants.GE_GRAPH_URL(id), GEGRAPH_REQUEST_TAG, new Utils.VolleyCallback() {
            @Override
            public void onSuccess(String result) {
                AppDb.getInstance(getActivity()).insertOrupdateGrandExchangeGraphData(id, result);
                geGraphData = result;
                handleGeGraphData();
            }

            @Override
            public void onError(VolleyError error) {
                if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                    GrandExchangeGraphData cachedData = AppDb.getInstance(getActivity()).getGrandExchangeGraphData(Integer.parseInt(id));
                    if (cachedData == null) {
                        showToast(getResources().getString(R.string.failed_to_obtain_data, "ge data", getResources().getString(R.string.network_error)), Toast.LENGTH_LONG);
                        return;
                    }
                    geGraphData = cachedData.data;
                    handleGeGraphData();

                }
                else {
                    showToast(getResources().getString(R.string.failed_to_obtain_data, "ge graph data", error.getMessage()), Toast.LENGTH_LONG);
                }
            }

            @Override
            public void always() {
                wasRequestingGegraph = false;
            }
        });
    }

    public void handleGeGraphData() {
        try {
            LineChart chart = (LineChart) view.findViewById(R.id.ge_item_graph);
            JSONObject dailyGraphData = new JSONObject(geGraphData).getJSONObject("daily");
            List<Entry> data = new ArrayList<>();
            for (Iterator<String> iter = dailyGraphData.keys(); iter.hasNext(); ) {
                String key = iter.next();
                float time = Float.parseFloat(key);
                data.add(new Entry(time, dailyGraphData.getInt(key)));
            }
            CustomLineDataSet dataSet = new CustomLineDataSet(data, getResources().getString(R.string.price), getResources().getColor(R.color.ge_graph_price));

            JSONObject trendGraphData = new JSONObject(geGraphData).getJSONObject("average");
            List<Entry> trendData = new ArrayList<>();
            for (Iterator<String> iter = trendGraphData.keys(); iter.hasNext(); ) {
                String key = iter.next();
                float time = Float.parseFloat(key);
                trendData.add(new Entry(time, trendGraphData.getInt(key)));
            }
            CustomLineDataSet dataSet2 = new CustomLineDataSet(trendData, getResources().getString(R.string.trend), getResources().getColor(R.color.ge_graph_trend));

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
            chart.invalidate();
        }
        catch (JSONException e) {
            showToast(getResources().getString(R.string.exception_occurred, e.getClass().getCanonicalName(), "parsing ge graph data"), Toast.LENGTH_LONG);

        }
    }

    public void zoomGraphToDays(GeGraphDays days) {
        LineChart chart = (LineChart) view.findViewById(R.id.ge_item_graph);
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

    public void loadOSBuddyExchange() {
        final String id = jsonItem.id;
        wasRequestingOsBuddy = true;
        setOSBuddyText("...", false);
        Utils.getString(Constants.OSBUDDY_EXCHANGE_URL + id, OSBUDDY_EXCHANGE_REQUEST_TAG, new Utils.VolleyCallback() {
            @Override
            public void onSuccess(String result) {
                AppDb.getInstance(getActivity()).insertOrUpdateOSBuddyExchangeData(id, result);
                osBuddyItemData = result;
                handleOSBuddyData();
            }

            @Override
            public void onError(VolleyError error) {
                OSBuddyExchangeData cachedData = AppDb.getInstance(getActivity()).getOSBuddyExchangeData(Integer.parseInt(id));
                if (cachedData == null) {
                    setOSBuddyText(getResources().getString(R.string.osb_error), true);
                    return;
                }
                osBuddyItemData = cachedData.data;
                handleOSBuddyData();
            }

            @Override
            public void always() {
                wasRequestingOsBuddy = false;
            }
        });
    }

    public void handleOSBuddyData() {
        try {
            JSONObject obj = new JSONObject(osBuddyItemData);
            int buyPrice = Integer.parseInt(obj.getString("buying"));
            int sellPrice = Integer.parseInt(obj.getString("selling"));
            String buyText = buyPrice < 1 ? getResources().getString(R.string.inactive) : RsUtils.kmbt(buyPrice, 2);
            String sellText = sellPrice < 1 ? getResources().getString(R.string.inactive) : RsUtils.kmbt(sellPrice, 2);
            setOSBuddyText(buyText, sellText, false);
        }
        catch (JSONException e) {
            showToast(getResources().getString(R.string.exception_occurred, e.getClass().getCanonicalName(), "parsing osbuddy data"), Toast.LENGTH_LONG);
        }
    }

    private void setOSBuddyText(String buyText, String sellText, boolean isError) {
        TextView osbBuyPriceTextView = view.findViewById(R.id.osb_buy_price);
        TextView osbSellPriceTextView = view.findViewById(R.id.osb_sell_price);
        osbBuyPriceTextView.setTextColor(getResources().getColor(isError ? R.color.red : R.color.text));
        osbSellPriceTextView.setTextColor(getResources().getColor(isError ? R.color.red : R.color.text));
        osbBuyPriceTextView.setText(buyText);
        osbSellPriceTextView.setText(sellText);
    }

    private void setOSBuddyText(String text, boolean isError) {
        setOSBuddyText(text, text, isError);
    }

    @Override
    public void onClick(View v) {
        GeGraphDays days;
        switch (v.getId()) {
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

    public void reloadData() {
        handleGeData();
        handleGeUpdateData();
        handleGeGraphData();
        handleOSBuddyData();
        zoomGraphToDays(currentSelectedDays);
        view.findViewById(R.id.ge_data).setVisibility(View.VISIBLE);
    }

    private void activateRefreshCooldown() {
        lastRefreshTimeMs = System.currentTimeMillis();
    }

    private Resources getResources() {
        return this.resources;
    }

    private Context getActivity() {
        return this.context;
    }

    @Override
    public void cancelVolleyRequests() {
        AppController.getInstance().cancelPendingRequests(GE_REQUEST_TAG);
        AppController.getInstance().cancelPendingRequests(GEUPDATE_REQUEST_TAG);
        AppController.getInstance().cancelPendingRequests(GEGRAPH_REQUEST_TAG);
        AppController.getInstance().cancelPendingRequests(OSBUDDY_EXCHANGE_REQUEST_TAG);
    }

    @Override
    public boolean wasRequesting() {
        return wasRequestingGe || wasRequestingGegraph || wasRequestingGeupdate || wasRequestingOsBuddy;
    }

    public void reloadOnOrientationChanged() {
        if (wasRequestingGe) {
            updateItem();
        }
        else if (!Utils.isNullOrEmpty(geItemData)) {
            view.findViewById(R.id.ge_data).setVisibility(View.VISIBLE);
            handleGeData();
        }

        if (wasRequestingGeupdate) {
            loadGeupdate();
        }
        else if (!Utils.isNullOrEmpty(geupdateData)) {
            handleGeUpdateData();
        }

        if (wasRequestingGegraph) {
            loadGraph();
        }
        else if (!Utils.isNullOrEmpty(geGraphData)) {
            handleGeGraphData();
        }

        if (wasRequestingOsBuddy) {
            loadOSBuddyExchange();
        }
        else if (!Utils.isNullOrEmpty(osBuddyItemData)) {
            handleOSBuddyData();
        }
    }

    private static class LoadItems extends AsyncTask<String, Void, ArrayList<JsonItem>> {
        private WeakReference<Context> context;
        private ItemsLoadedCallback itemsLoadedCallback;
        private ArrayList<JsonItem> allItems = new ArrayList<>();

        private LoadItems(Context context, ItemsLoadedCallback itemsLoadedCallback) {
            this.context = new WeakReference<>(context);
            this.itemsLoadedCallback = itemsLoadedCallback;
        }

        @Override
        protected ArrayList<JsonItem> doInBackground(String... params) {
            String json = Utils.readFromFile(context.get(), Constants.ITEMIDLIST_FILE_NAME);
            try {
                if (Utils.isNullOrEmpty(json)) {
                    InputStream is = context.get().getAssets().open("names.json");
                    int size = is.available();
                    byte[] buffer = new byte[size];
                    is.read(buffer);
                    is.close();
                    json = new String(buffer, "UTF-8");
                }
                JSONObject obj = new JSONObject(json);
                String itemsString = obj.getString("items");
                JSONObject items = new JSONObject(itemsString);
                Iterator iterator = items.keys();
                while (iterator.hasNext()) {
                    String id = (String) iterator.next();
                    JSONObject result = items.getJSONObject(id);
                    JsonItem geResult = new JsonItem();
                    geResult.id = id;
                    geResult.name = result.getString("name");
                    geResult.store = result.getString("store");
                    allItems.add(geResult);
                }
            }
            catch (JSONException | IOException ignored) {

            }
            return allItems;
        }

        @Override
        protected void onPostExecute(ArrayList<JsonItem> items) {
            if (items.size() > 0) {
                itemsLoadedCallback.onItemsLoaded(items);
            }
            else {
                itemsLoadedCallback.onLoadError();
            }
        }
    }

    public interface ItemsLoadedCallback {
        void onItemsLoaded(ArrayList<JsonItem> items);

        void onLoadError();
    }

    public void setItemsLoadedCallback(ItemsLoadedCallback listener) {
        this.itemsLoadedCallback = listener;
    }
}
