package com.dennyy.osrscompanion.layouthandlers;


import android.content.Context;
import android.support.v4.widget.NestedScrollView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.VolleyError;
import com.dennyy.osrscompanion.AppController;
import com.dennyy.osrscompanion.R;
import com.dennyy.osrscompanion.customviews.ClearableEditText;
import com.dennyy.osrscompanion.customviews.LineIndicatorButton;
import com.dennyy.osrscompanion.enums.TrackDurationType;
import com.dennyy.osrscompanion.helpers.AppDb;
import com.dennyy.osrscompanion.helpers.Constants;
import com.dennyy.osrscompanion.helpers.RsUtils;
import com.dennyy.osrscompanion.helpers.Utils;
import com.dennyy.osrscompanion.models.Tracker.TrackData;

import java.util.HashMap;
import java.util.Map;

public class TrackerViewHandler extends BaseViewHandler implements View.OnClickListener {

    public HashMap<TrackDurationType, TrackData> trackData = new HashMap<>();
    public TrackDurationType durationType = TrackDurationType.WEEK;

    private final String TRACK_REQUEST_TAG = "trackrequest";

    private EditText rsnEditText;
    private TableLayout trackerTable;
    private SwipeRefreshLayout refreshLayout;
    private TableRow.LayoutParams rowParams;
    private NestedScrollView scrollView;
    private long lastRefreshTimeMs;
    private HashMap<TrackDurationType, Integer> indicators;
    private boolean lastLoadedFromCache;

    public TrackerViewHandler(final Context context, View view) {
        super(context, view);
        rsnEditText = ((ClearableEditText) view.findViewById(R.id.track_rsn_input)).getEditText();
        rsnEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND || actionId == EditorInfo.IME_ACTION_DONE) {
                    if (allowUpdateUser())
                        updateUser();
                    Utils.hideKeyboard(context, v);
                    return true;
                }
                return false;
            }
        });
        view.findViewById(R.id.tracker_lookup_button).setOnClickListener(this);
        trackerTable = (TableLayout) view.findViewById(R.id.tracker_table);
        scrollView = (NestedScrollView) view.findViewById(R.id.tracker_scrollview);
        refreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.tracker_refresh_layout);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (allowUpdateUser())
                    updateUser();
            }
        });
        indicators = new HashMap<>();

        indicators.put(TrackDurationType.DAY, R.id.tracker_period_day);
        indicators.put(TrackDurationType.WEEK, R.id.tracker_period_week);
        indicators.put(TrackDurationType.MONTH, R.id.tracker_period_month);
        indicators.put(TrackDurationType.YEAR, R.id.tracker_period_year);

        for (Map.Entry<TrackDurationType, Integer> entry : indicators.entrySet()) {
            view.findViewById(entry.getValue()).setOnClickListener(this);
        }

        rowParams = new TableRow.LayoutParams(0, (int) Utils.convertDpToPixel(30, context), 1f);
        trackerTable.removeAllViews();


        if (!defaultRsn.isEmpty()) {
            rsnEditText.setText(defaultRsn);
            loadTrackDataFromDb();
            if (trackData.get(TrackDurationType.WEEK) != null)
                updateUserFromPeriod(R.id.tracker_period_week, true);
        }
    }

    @Override
    public boolean wasRequesting() {
        return wasRequesting;
    }

    private void loadTrackDataFromDb() {
        String rsn = rsnEditText.getText().toString();
        TrackData cachedDataDay = AppDb.getInstance(context).getTrackData(rsn, TrackDurationType.DAY);
        TrackData cachedDataWeek = AppDb.getInstance(context).getTrackData(rsn, TrackDurationType.WEEK);
        TrackData cachedDataMonth = AppDb.getInstance(context).getTrackData(rsn, TrackDurationType.MONTH);
        TrackData cachedDataYear = AppDb.getInstance(context).getTrackData(rsn, TrackDurationType.YEAR);
        trackData.put(TrackDurationType.DAY, cachedDataDay);
        trackData.put(TrackDurationType.WEEK, cachedDataWeek);
        trackData.put(TrackDurationType.MONTH, cachedDataMonth);
        trackData.put(TrackDurationType.YEAR, cachedDataYear);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        switch (id) {
            case R.id.tracker_lookup_button:
                Utils.hideKeyboard(context, rsnEditText);
                if (allowUpdateUser())
                    updateUser();
                break;
            case R.id.tracker_period_day:
            case R.id.tracker_period_week:
            case R.id.tracker_period_month:
            case R.id.tracker_period_year:
                updateUserFromPeriod(id);
                break;
        }
    }

    private void updateUserFromPeriod(int selectedButtonResourceId, boolean forceReload) {
        lastLoadedFromCache = true;
        if (!allowUpdateUser())
            return;
        TrackDurationType selectedType = getTrackDurationType(selectedButtonResourceId);
        if (!forceReload && durationType == selectedType)
            return;
        durationType = selectedType;
        updateIndicators();
        activateRefreshCooldown();
        TrackData trackData = null;
        if (durationType == TrackDurationType.DAY)
            trackData = this.trackData.get(TrackDurationType.DAY);
        else if (durationType == TrackDurationType.WEEK)
            trackData = this.trackData.get(TrackDurationType.WEEK);
        else if (durationType == TrackDurationType.MONTH)
            trackData = this.trackData.get(TrackDurationType.MONTH);
        else if (durationType == TrackDurationType.YEAR)
            trackData = this.trackData.get(TrackDurationType.YEAR);
        if (trackData == null) {
            updateUser();
            return;
        }
        trackerTable.removeAllViews();
        showToast(resources.getString(R.string.last_updated_at, Utils.convertTime(trackData.dateModified)), Toast.LENGTH_LONG);
        handleTrackData(trackData.data);
        view.findViewById(R.id.tracker_data_layout).setVisibility(View.VISIBLE);
    }

    private void updateUserFromPeriod(int selectedButtonResourceId) {
        updateUserFromPeriod(selectedButtonResourceId, false);
    }


    private TrackDurationType getTrackDurationType(int buttonId) {
        TrackDurationType mode;
        switch (buttonId) {
            case R.id.tracker_period_day:
                mode = TrackDurationType.DAY;
                break;
            case R.id.tracker_period_month:
                mode = TrackDurationType.MONTH;
                break;
            case R.id.tracker_period_year:
                mode = TrackDurationType.YEAR;
                break;
            default:
                mode = TrackDurationType.WEEK;
                break;
        }
        return mode;
    }

    public boolean allowUpdateUser() {
        long refreshPeriod = System.currentTimeMillis() - lastRefreshTimeMs;

        if (rsnEditText.getText().toString().isEmpty()) {
            showToast(resources.getString(R.string.empty_rsn_error), Toast.LENGTH_SHORT);
            refreshLayout.setRefreshing(false);
            return false;
        }
        int cooldown = Constants.REFRESH_COOLDOWN_TRACK;
        if (lastLoadedFromCache) {
            cooldown = Constants.REFRESH_COOLDOWN_CACHE;
        }
        if (refreshPeriod < cooldown) {
            double timeLeft = (cooldown - refreshPeriod) / 1000;
            showToast(resources.getString(R.string.wait_before_refresh, (int) Math.ceil(timeLeft + 0.5)), Toast.LENGTH_SHORT);
            refreshLayout.setRefreshing(false);
            return false;
        }
        return true;
    }

    public void updateIndicators() {
        for (Map.Entry<TrackDurationType, Integer> entry : indicators.entrySet()) {

            ((LineIndicatorButton) view.findViewById(entry.getValue())).setActive(false);
        }
        ((LineIndicatorButton) view.findViewById(indicators.get(durationType))).setActive(true);
    }

    public void updateUser() {
        final String rsn = rsnEditText.getText().toString();
        activateRefreshCooldown();
        refreshLayout.setRefreshing(true);
        wasRequesting = true;
        Utils.getString(Constants.TRACKER_URL(rsn, durationType.getValue()), TRACK_REQUEST_TAG, new Utils.VolleyCallback() {
            @Override
            public void onSuccess(String result) {
                refreshLayout.setRefreshing(false);
                trackerTable.removeAllViews();
                lastLoadedFromCache = false;
                String[] apiResults = result.split("\n~~\n");
                if (apiResults.length == 2) {
                    String updateResult = apiResults[0];
                    String trackerResult = apiResults[1];
                    if (updateResult.equals("2")) {
                        showToast(resources.getString(R.string.player_not_found), Toast.LENGTH_LONG);
                        return;
                    }
                    if (updateResult.equals("-1") || trackerResult.equals("-1")) {
                        showTrackError(getString(R.string.tracker_no_gains_found));
                        return;
                    }
                    if (updateResult.equals("-4") || trackerResult.equals("-4")) {
                        TrackData cachedData = AppDb.getInstance(context).getTrackData(rsn, durationType);
                        if (cachedData == null) {
                            showTrackError(resources.getString(R.string.tracker_api_under_load));
                            return;
                        }
                        showTrackError(resources.getString(R.string.tracker_load_from_cache));
                        lastLoadedFromCache = true;
                        trackerTable.removeAllViews();
                        showTrackError(resources.getString(R.string.using_cached_data_under_load, Utils.convertTime(cachedData.dateModified)));

                        view.findViewById(R.id.tracker_data_layout).setVisibility(View.VISIBLE);
                        handleTrackData(cacheTrackData(cachedData));
                        return;
                    }
                    TrackData trackData = new TrackData();
                    trackData.rsn = rsn;
                    trackData.durationType = durationType;
                    trackData.data = trackerResult;
                    trackData.dateModified = System.currentTimeMillis();
                    cacheTrackData(trackData);
                    hideTrackError();
                    view.findViewById(R.id.tracker_data_layout).setVisibility(View.VISIBLE);
                    AppDb.getInstance(context).insertOrUpdateTrackData(trackData);
                    handleTrackData(trackerResult);
                }
            }

            @Override
            public void onError(VolleyError error) {
                refreshLayout.setRefreshing(false);
                NetworkResponse response = error.networkResponse;
                TrackData cachedData = AppDb.getInstance(context).getTrackData(rsn, durationType);
                lastLoadedFromCache = true;
                if (cachedData == null) {
                    String errorMessage = response == null ? resources.getString(R.string.network_error) : Utils.trimMessage(new String(response.data), "message");
                    showToast(resources.getString(R.string.failed_to_obtain_data, "track data", errorMessage), Toast.LENGTH_LONG);
                    return;
                }
                trackerTable.removeAllViews();
                showTrackError(resources.getString(R.string.tracker_load_from_network_error, Utils.convertTime(cachedData.dateModified)));
                view.findViewById(R.id.tracker_data_layout).setVisibility(View.VISIBLE);
                handleTrackData(cacheTrackData(cachedData));
            }

            @Override
            public void always() {
                wasRequesting = false;
            }
        });
    }


    public void handleTrackData(String trackerResult) {
        String[] lines = trackerResult.split("\n");
        TableLayout trackerTable = (TableLayout) view.findViewById(R.id.tracker_table);

        int totalLevel = 0;
        int totalLevelGain = 0;
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            String[] skillResult = line.split(",");
            if (skillResult.length == 4 && i > 1) {
                int startExp = Integer.parseInt(skillResult[2]) - Integer.parseInt(skillResult[0]);
                int endExp = Integer.parseInt(skillResult[2]);
                int startLvl = RsUtils.lvl(startExp, true);
                int endLvl = RsUtils.lvl(endExp, true);
                totalLevelGain += endLvl - startLvl;
                totalLevel += RsUtils.lvl(endExp, true);
            }
        }
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            String[] skillResult = line.split(",");
            if (skillResult.length == 4) {
                long startExp = Long.parseLong(skillResult[2]) - Integer.parseInt(skillResult[0]);
                int startLvl = RsUtils.lvl(startExp, false);
                long endExp = Long.parseLong(skillResult[2]);
                int endLvl = RsUtils.lvl(endExp, false);
                int rankGains = -Integer.parseInt(skillResult[1]);
                boolean useTotalLevel = i == 1;
                trackerTable.addView(createRow(i, useTotalLevel ? totalLevel - totalLevelGain : startLvl, useTotalLevel ? totalLevel : endLvl, rankGains, Integer.parseInt(skillResult[0])));
            }
        }
        //        scrollView.postDelayed(new Runnable() {
        //            @Override
        //            public void run() {
        //                scrollView.smoothScrollTo(0, (int) Utils.convertDpToPixel(75, context));
        //            }
        //        }, 100);
    }

    private TableRow createRow(int skillId, int startLvl, int endLvl, int rank, int expGains) {
        TableRow.LayoutParams imageParams = new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 0.5f);
        imageParams.gravity = Gravity.CENTER;

        TableRow row = new TableRow(context);

        ImageView skillImageView = new ImageView(context);
        skillImageView.setImageDrawable(resources.getDrawable(RsUtils.getSkillResourceId(skillId - 1)));
        skillImageView.setLayoutParams(imageParams);
        row.addView(skillImageView);

        TextView rankTextView = new TextView(context);
        rankTextView.setText(String.valueOf(rank));
        rankTextView.setGravity(Gravity.CENTER);
        rankTextView.setLayoutParams(rowParams);
        rankTextView.setTextColor(context.getResources().getColor(R.color.text));
        if (rank < 0)
            rankTextView.setTextColor(resources.getColor(R.color.red));
        if (rank > 0) {
            rankTextView.setTextColor(resources.getColor(R.color.green));
            rankTextView.setText(String.format("+%s", Utils.formatNumber(rank)));
        }
        row.addView(rankTextView);

        TextView endLvlTextView = new TextView(context);
        endLvlTextView.setText(startLvl < endLvl ? startLvl + " > " + endLvl : endLvl + "");
        endLvlTextView.setGravity(Gravity.CENTER);
        endLvlTextView.setLayoutParams(rowParams);
        endLvlTextView.setTextColor(context.getResources().getColor(R.color.text));

        row.addView(endLvlTextView);

        TextView gainsTextView = new TextView(context);
        gainsTextView.setText(String.valueOf(expGains));
        gainsTextView.setTextColor(context.getResources().getColor(R.color.text));
        if (expGains > 0) {
            gainsTextView.setTextColor(resources.getColor(R.color.green));
            gainsTextView.setText(String.format("+%s", Utils.formatNumber(expGains)));
        }
        gainsTextView.setGravity(Gravity.CENTER);
        gainsTextView.setLayoutParams(rowParams);
        row.addView(gainsTextView);

        return row;
    }

    private void showTrackError(String text) {
        view.findViewById(R.id.track_error_wrapper).setVisibility(View.VISIBLE);
        ((TextView) view.findViewById(R.id.track_error_info)).setText(text);
    }

    private void hideTrackError() {
        view.findViewById(R.id.track_error_wrapper).setVisibility(View.GONE);
    }

    private void activateRefreshCooldown() {
        lastRefreshTimeMs = System.currentTimeMillis();
    }


    private String cacheTrackData(TrackData trackData) {
        this.trackData.put(this.durationType, trackData);
        return trackData.data;
    }

    @Override
    public void cancelVolleyRequests() {
        AppController.getInstance().cancelPendingRequests(TRACK_REQUEST_TAG);
    }
}
