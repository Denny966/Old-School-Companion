package com.dennyy.oldschoolcompanion.viewhandlers;


import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.*;
import com.android.volley.VolleyError;
import com.dennyy.oldschoolcompanion.AppController;
import com.dennyy.oldschoolcompanion.R;
import com.dennyy.oldschoolcompanion.customviews.ClearableEditText;
import com.dennyy.oldschoolcompanion.customviews.LineIndicatorButton;
import com.dennyy.oldschoolcompanion.database.AppDb;
import com.dennyy.oldschoolcompanion.enums.SkillType;
import com.dennyy.oldschoolcompanion.enums.TrackDurationType;
import com.dennyy.oldschoolcompanion.helpers.Constants;
import com.dennyy.oldschoolcompanion.helpers.Logger;
import com.dennyy.oldschoolcompanion.helpers.RsUtils;
import com.dennyy.oldschoolcompanion.helpers.Utils;
import com.dennyy.oldschoolcompanion.models.Tracker.TrackData;
import com.dennyy.oldschoolcompanion.models.Tracker.TrackValuePair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TrackerViewHandler extends BaseViewHandler implements View.OnClickListener {


    private final String TRACK_REQUEST_TAG = "trackrequest";

    private EditText rsnEditText;
    private TableLayout trackerTable;
    private SwipeRefreshLayout refreshLayout;
    private TableRow.LayoutParams rowParams;
    private long lastRefreshTimeMs;
    private HashMap<TrackDurationType, Integer> indicators;
    private boolean lastLoadedFromCache;
    private TrackDurationType durationType;
    private HashMap<String, Long> lastUpdateTimes;

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
        trackerTable = view.findViewById(R.id.tracker_table);
        refreshLayout = view.findViewById(R.id.tracker_refresh_layout);
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
        durationType = TrackDurationType.WEEK;
        lastUpdateTimes = new HashMap<>();
        rowParams = new TableRow.LayoutParams(0, (int) Utils.convertDpToPixel(30, context), 1f);
        trackerTable.removeAllViews();

        getRsn(rsnEditText);
    }

    @Override
    public boolean wasRequesting() {
        return wasRequesting;
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

        updateUser();
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
        boolean skipRequest = lastUpdateTimes.containsKey(rsn) && (System.currentTimeMillis() - lastUpdateTimes.get(rsn) < Constants.REFRESH_COOLDOWN_LONG_MS);

        Utils.getString(Constants.TRACKER_UPDATE_URL(rsn), TRACK_REQUEST_TAG, skipRequest, new Utils.VolleyCallback() {
            @Override
            public void onSuccess(String result) {
                if (result != null) {
                    lastUpdateTimes.put(rsn, System.currentTimeMillis());
                }
                Utils.getString(Constants.TRACKER_URL(rsn, durationType.getValue()), TRACK_REQUEST_TAG, new Utils.VolleyCallback() {
                    @Override
                    public void onSuccess(String result) {
                        refreshLayout.setRefreshing(false);
                        trackerTable.removeAllViews();
                        Pattern pattern = Pattern.compile("<tr.*?column_skill(.*?)</tr>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.MULTILINE);
                        Matcher m = pattern.matcher(result);
                        if (!m.find()) {
                            updateUserFromApi(rsn);
                            return;
                        }
                        try {
                            TableLayout trackerTable = view.findViewById(R.id.tracker_table);
                            int skillId = 0;
                            while (m.find() && skillId <= SkillType.CONSTRUCTION.id) {
                                skillId++;
                                String skillRow = m.group();
                                Pattern statsPattern = Pattern.compile("<td title='(.*?)'>(.*?)</td>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.MULTILINE);
                                Matcher statsMatcher = statsPattern.matcher(skillRow);

                                List<TrackValuePair> valuePairs = new ArrayList<>();
                                while (statsMatcher.find()) {
                                    valuePairs.add(new TrackValuePair(statsMatcher.group(1), statsMatcher.group(2)));
                                }
                                int expGain = valuePairs.get(0).gains;
                                int rankGains = valuePairs.get(1).gains;
                                int currentLvl = Utils.safeLongToInt(valuePairs.get(2).currentValue);
                                int lvlGain = valuePairs.get(2).gains;

                                trackerTable.addView(createRow(skillId, currentLvl - lvlGain, currentLvl, rankGains, expGain));
                            }
                            hideTrackError();
                            view.findViewById(R.id.tracker_data_layout).setVisibility(View.VISIBLE);
                        }
                        catch (Exception ex) {
                            updateUserFromApi(rsn);
                            Logger.log(ex, "failed to parse html track data", result);
                        }
                    }

                    @Override
                    public void onError(VolleyError error) {
                        updateUserFromApi(rsn);
                    }

                    @Override
                    public void always() {
                        wasRequesting = false;
                    }
                });
            }

            @Override
            public void onError(VolleyError error) {
                updateUserFromApi(rsn);
            }

            @Override
            public void always() {
                wasRequesting = false;
            }
        });
    }

    private void updateUserFromApi(final String rsn) {
        Utils.getString(Constants.TRACKER_API_URL(rsn, durationType.getValue()), TRACK_REQUEST_TAG, new Utils.VolleyCallback() {
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
                        handleTrackData(cachedData.data);
                        return;
                    }
                    TrackData trackData = new TrackData();
                    trackData.rsn = rsn;
                    trackData.durationType = durationType;
                    trackData.data = trackerResult;
                    trackData.dateModified = System.currentTimeMillis();
                    hideTrackError();
                    view.findViewById(R.id.tracker_data_layout).setVisibility(View.VISIBLE);
                    AppDb.getInstance(context).insertOrUpdateTrackData(trackData);
                    handleTrackData(trackerResult);
                }
            }

            @Override
            public void onError(VolleyError error) {
                refreshLayout.setRefreshing(false);
                TrackData cachedData = AppDb.getInstance(context).getTrackData(rsn, durationType);
                lastLoadedFromCache = true;
                if (cachedData == null) {
                    String statusCode = String.valueOf(Utils.getStatusCode(error));
                    showToast(resources.getString(R.string.failed_to_obtain_data, "track data", statusCode), Toast.LENGTH_LONG);
                    return;
                }
                trackerTable.removeAllViews();
                showTrackError(resources.getString(R.string.tracker_load_from_network_error, Utils.convertTime(cachedData.dateModified)));
                view.findViewById(R.id.tracker_data_layout).setVisibility(View.VISIBLE);
                handleTrackData(cachedData.data);
            }

            @Override
            public void always() {
                wasRequesting = false;
            }
        });
    }

    public void handleTrackData(String trackerResult) {
        String[] lines = trackerResult.split("\n");
        if (lines.length < Constants.REQUIRED_STATS_LENGTH) {
            showToast(resources.getString(R.string.player_not_found), Toast.LENGTH_LONG);
            return;
        }

        TableLayout trackerTable = view.findViewById(R.id.tracker_table);
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
    }

    private TableRow createRow(int skillId, int startLvl, int endLvl, int rankGains, int expGains) {
        TableRow.LayoutParams imageParams = new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 0.5f);
        imageParams.gravity = Gravity.CENTER;

        TableRow row = new TableRow(context);

        ImageView skillImageView = new ImageView(context);
        skillImageView.setImageDrawable(resources.getDrawable(SkillType.fromId(skillId - 1).drawable));
        skillImageView.setLayoutParams(imageParams);
        row.addView(skillImageView);

        TextView rankTextView = new TextView(context);
        rankTextView.setText(String.valueOf(rankGains));
        rankTextView.setGravity(Gravity.CENTER);
        rankTextView.setLayoutParams(rowParams);
        rankTextView.setTextColor(context.getResources().getColor(R.color.text));
        if (rankGains < 0)
            rankTextView.setTextColor(resources.getColor(R.color.red));
        if (rankGains > 0) {
            rankTextView.setTextColor(resources.getColor(R.color.green));
            rankTextView.setText(String.format("+%s", Utils.formatNumber(rankGains)));
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

    @Override
    public void cancelRunningTasks() {
        AppController.getInstance().cancelPendingRequests(TRACK_REQUEST_TAG);
    }
}
