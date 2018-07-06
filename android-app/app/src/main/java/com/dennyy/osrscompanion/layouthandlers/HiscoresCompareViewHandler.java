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

import com.android.volley.NoConnectionError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.dennyy.osrscompanion.AppController;
import com.dennyy.osrscompanion.R;
import com.dennyy.osrscompanion.customviews.ClearableEditText;
import com.dennyy.osrscompanion.customviews.LineIndicatorButton;
import com.dennyy.osrscompanion.enums.CompareMode;
import com.dennyy.osrscompanion.enums.HiscoreMode;
import com.dennyy.osrscompanion.helpers.AppDb;
import com.dennyy.osrscompanion.helpers.Constants;
import com.dennyy.osrscompanion.helpers.RsUtils;
import com.dennyy.osrscompanion.helpers.Utils;
import com.dennyy.osrscompanion.models.Hiscores.TotalAndCombatInfo;
import com.dennyy.osrscompanion.models.Hiscores.UserStats;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class HiscoresCompareViewHandler extends BaseViewHandler implements View.OnClickListener {
    private EditText rsnEditText;
    private EditText rsn2EditText;
    private TableLayout hiscoresTable;
    private TableLayout hiscoresMinigameTable;
    private NestedScrollView scrollView;
    private SwipeRefreshLayout refreshLayout;
    private TableRow.LayoutParams rowParams;
    private HashMap<HiscoreMode, Integer> indicators;

    public HiscoreMode selectedHiscore = HiscoreMode.NORMAL;
    private HashMap<CompareMode, Integer> comparisonIndicators;

    public CompareMode selectedComparison = CompareMode.LEVEL;
    public UserStats playerOneStats;
    public UserStats playerTwoStats;
    private boolean lastLoadedFromCache;

    private long lastRefreshTimeMs;
    private int refreshCount;

    private static final String COMPARE_REQUEST_P1_TAG = "comparerequest";
    private static final String COMPARE_REQUEST_P2_TAG = "comparerequest2";
    private boolean wasRequestingp2;

    public HiscoresCompareViewHandler(final Context context, View view) {
        super(context, view);
        rowParams = new TableRow.LayoutParams(0, (int) Utils.convertDpToPixel(35, context), 1f);

        scrollView = (NestedScrollView) view.findViewById(R.id.hiscores_compare_scrollview);
        rsnEditText = ((ClearableEditText) view.findViewById(R.id.hiscores_compare_rsn_1)).getEditText();
        rsn2EditText = ((ClearableEditText) view.findViewById(R.id.hiscores_compare_rsn_2)).getEditText();
        rsnEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND || actionId == EditorInfo.IME_ACTION_DONE) {
                    rsn2EditText.clearFocus();
                    rsn2EditText.requestFocus();
                    return true;
                }
                return false;
            }
        });
        rsn2EditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND || actionId == EditorInfo.IME_ACTION_DONE) {
                    if (allowUpdateUser())
                        getPlayerOneStats();
                    Utils.hideKeyboard(context, v);
                    return true;
                }
                return false;
            }
        });
        hiscoresTable = (TableLayout) view.findViewById(R.id.hiscores_compare_table);
        hiscoresMinigameTable = (TableLayout) view.findViewById(R.id.hiscores_compare_minigame_table);
        refreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.hiscores_compare_refresh_layout);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (allowUpdateUser())
                    getPlayerOneStats();
            }
        });
        view.findViewById(R.id.hiscores_compare_lookup_button).setOnClickListener(this);
        indicators = new HashMap<>();
        comparisonIndicators = new HashMap<>();

        indicators.put(HiscoreMode.NORMAL, R.id.hiscores_compare_normal);
        indicators.put(HiscoreMode.IRONMAN, R.id.hiscores_compare_ironman);
        indicators.put(HiscoreMode.HCIM, R.id.hiscores_compare_hardcore_ironman);
        indicators.put(HiscoreMode.UIM, R.id.hiscores_compare_ultimate_ironman);
        indicators.put(HiscoreMode.DMM, R.id.hiscores_compare_dmm);
        indicators.put(HiscoreMode.SDMM, R.id.hiscores_compare_sdmm);

        comparisonIndicators.put(CompareMode.LEVEL, R.id.hiscores_compare_lvl);
        comparisonIndicators.put(CompareMode.RANK, R.id.hiscores_compare_rank);
        comparisonIndicators.put(CompareMode.EXP, R.id.hiscores_compare_exp);

        for (Map.Entry<CompareMode, Integer> entry : comparisonIndicators.entrySet()) {
            view.findViewById(entry.getValue()).setOnClickListener(this);
        }

        for (Map.Entry<HiscoreMode, Integer> entry : indicators.entrySet()) {
            view.findViewById(entry.getValue()).setOnClickListener(this);
        }
        if (!defaultRsn.isEmpty())
            rsnEditText.setText(defaultRsn);
        clearTables();
    }

    @Override
    public boolean wasRequesting() {
        return wasRequesting || wasRequestingp2;
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        switch (id) {
            case R.id.hiscores_compare_lookup_button:
                Utils.hideKeyboard(context, rsnEditText);
                if (allowUpdateUser())
                    getPlayerOneStats();
                break;
            case R.id.hiscores_compare_normal:
            case R.id.hiscores_compare_ironman:
            case R.id.hiscores_compare_hardcore_ironman:
            case R.id.hiscores_compare_ultimate_ironman:
            case R.id.hiscores_compare_sdmm:
            case R.id.hiscores_compare_dmm:
                updateUserFromHiscoreType(id);

                break;
            case R.id.hiscores_compare_exp:
            case R.id.hiscores_compare_lvl:
            case R.id.hiscores_compare_rank:
                updateUserFromComparisonType(id);
                break;
        }
    }

    private void updateUserFromHiscoreType(int selectedButtonResourceId) {
        if (!allowUpdateUser())
            return;
        HiscoreMode mode = getHiscoresMode(selectedButtonResourceId);
        if (selectedHiscore == mode)
            return;

        selectedHiscore = mode;
        updateIndicators();
        getPlayerOneStats();
    }

    private void updateUserFromComparisonType(int selectedButtonResourceId) {
        CompareMode mode = getComparisonMode(selectedButtonResourceId);
        if (selectedComparison == mode)
            return;

        selectedComparison = mode;
        updateIndicators();
        if (playerOneStats != null && playerTwoStats != null) {
            clearTables();
            handleHiscoresData(playerOneStats.rsn, playerOneStats.stats, playerTwoStats.rsn, playerTwoStats.stats);
        }
    }

    public void updateIndicators() {
        for (Map.Entry<HiscoreMode, Integer> entry : indicators.entrySet()) {
            ((LineIndicatorButton) view.findViewById(entry.getValue())).setActive(false);
        }
        ((LineIndicatorButton) view.findViewById(indicators.get(selectedHiscore))).setActive(true);
        for (Map.Entry<CompareMode, Integer> entry : comparisonIndicators.entrySet()) {
            ((LineIndicatorButton) view.findViewById(entry.getValue())).setActive(false);
        }
        ((LineIndicatorButton) view.findViewById(comparisonIndicators.get(selectedComparison))).setActive(true);
    }

    public boolean allowUpdateUser() {
        long refreshPeriod = System.currentTimeMillis() - lastRefreshTimeMs;
        if (rsnEditText.getText().toString().isEmpty()) {
            refreshLayout.setRefreshing(false);
            showToast(resources.getString(R.string.empty_rsn_error), Toast.LENGTH_SHORT);
            return false;
        }
         if (rsn2EditText.getText().toString().isEmpty()) {
            refreshLayout.setRefreshing(false);
            showToast(resources.getString(R.string.empty_second_rsn_error), Toast.LENGTH_SHORT);
            return false;
        }
        if (refreshPeriod >= Constants.REFRESH_COOLDOWN_MS) {
            refreshCount = 0;
        }
        if (refreshPeriod < Constants.REFRESH_COOLDOWN_MS && refreshCount >= Constants.MAX_REFRESH_COUNT) {
            refreshLayout.setRefreshing(false);
            double timeLeft = (Constants.REFRESH_COOLDOWN_MS - refreshPeriod) / 1000;
            showToast(resources.getString(R.string.wait_before_refresh, (int) Math.ceil(timeLeft + 0.5)), Toast.LENGTH_SHORT);
            return false;
        }
        activateRefreshCooldown();
        return true;
    }

    public void getPlayerOneStats() {
        final String rsn = rsnEditText.getText().toString();
        final String rsn2 = rsn2EditText.getText().toString();
        cancelVolleyRequests();
        refreshLayout.setRefreshing(true);
        wasRequesting = true;
        Utils.getString(getHiscoresUrl(selectedHiscore) + rsn, COMPARE_REQUEST_P1_TAG, new Utils.VolleyCallback() {
            @Override
            public void onSuccess(String result) {
                lastLoadedFromCache = false;
                getPlayerTwoStats(rsn, result, rsn2);
                playerOneStats = new UserStats(rsn, result, selectedHiscore);
                AppDb.getInstance(context).insertOrUpdateUserStats(playerOneStats);
            }

            @Override
            public void onError(VolleyError error) {
                if (error.networkResponse != null && error.networkResponse.statusCode == 404) {
                    getPlayerTwoStats(rsn, "", rsn2);
                    playerOneStats = new UserStats(rsn, "", selectedHiscore);
                    AppDb.getInstance(context).insertOrUpdateUserStats(playerOneStats);
                }
                else if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                    UserStats cachedData = AppDb.getInstance(context).getUserStats(rsn, selectedHiscore);
                    if (cachedData == null) {
                        showToast(resources.getString(R.string.failed_to_obtain_data, "player 1 data", resources.getString(R.string.network_error)), Toast.LENGTH_LONG);
                    }
                    else {
                        showToast(resources.getString(R.string.using_cached_data, Utils.convertTime(cachedData.dateModified)), Toast.LENGTH_LONG);
                    }
                    lastLoadedFromCache = true;
                    getPlayerTwoStats(rsn, cachedData == null ? "" : cachedData.stats, rsn2);
                }
                else {
                    showToast(resources.getString(R.string.failed_to_obtain_data, "player 1 data", error.getMessage()), Toast.LENGTH_LONG);
                    refreshLayout.setRefreshing(false);
                }
            }

            @Override
            public void always() {
                wasRequesting = false;
            }
        });
    }

    private void getPlayerTwoStats(final String rsn, final String playerOneStats, final String rsn2) {
        wasRequestingp2 = true;
        Utils.getString(getHiscoresUrl(selectedHiscore) + rsn2, COMPARE_REQUEST_P2_TAG, new Utils.VolleyCallback() {
            @Override
            public void onSuccess(String result) {
                lastLoadedFromCache = false;
                refreshLayout.setRefreshing(false);
                clearTables();
                handleHiscoresData(rsn, playerOneStats, rsn2, result);
                playerTwoStats = new UserStats(rsn2, result, selectedHiscore);
                AppDb.getInstance(context).insertOrUpdateUserStats(playerTwoStats);
            }

            @Override
            public void onError(VolleyError error) {
                if (error.networkResponse != null && error.networkResponse.statusCode == 404) {
                    clearTables();
                    handleHiscoresData(rsn, playerOneStats, rsn2, "");
                    playerTwoStats = new UserStats(rsn2, "", selectedHiscore);
                    AppDb.getInstance(context).insertOrUpdateUserStats(playerTwoStats);
                    return;
                }
                else if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                    UserStats cachedData = AppDb.getInstance(context).getUserStats(rsn2, selectedHiscore);
                    if (cachedData == null) {
                        showToast(resources.getString(R.string.failed_to_obtain_data, "player 2 data", resources.getString(R.string.network_error)), Toast.LENGTH_LONG);
                    }
                    else {
                        showToast(resources.getString(R.string.using_cached_data, Utils.convertTime(cachedData.dateModified)), Toast.LENGTH_LONG);
                    }
                    lastLoadedFromCache = true;
                    clearTables();
                    handleHiscoresData(rsn, playerOneStats, rsn2, cachedData == null ? "" : cachedData.stats);
                }
                else
                    showToast(resources.getString(R.string.failed_to_obtain_data, "player 2 data", error.getMessage()), Toast.LENGTH_LONG);
                refreshLayout.setRefreshing(false);
            }

            @Override
            public void always() {
                wasRequestingp2 = false;
                refreshLayout.setRefreshing(false);
            }
        });
    }

    public void handleHiscoresData(String rsn, String result1, String rsn2, String result2) {
        String[] playerOneStats = result1.split("\n");
        String[] playerTwoStats = result2.split("\n");
        if (result1.isEmpty() && result2.isEmpty()) {
            showToast(resources.getString(R.string.hiscores_compare_both_not_ranked), Toast.LENGTH_LONG);
            refreshLayout.setRefreshing(false);
            return;
        }
        view.findViewById(R.id.hiscores_compare_data_layout).setVisibility(View.VISIBLE);
        ((TextView) view.findViewById(R.id.hiscores_compare_player_one_name)).setText(rsn);
        ((TextView) view.findViewById(R.id.hiscores_compare_player_two_name)).setText(rsn2);
        ((TextView) view.findViewById(R.id.hiscores_compare_minigame_player_one_name)).setText(rsn);
        ((TextView) view.findViewById(R.id.hiscores_compare_minigame_player_two_name)).setText(rsn2);
        addRows(playerOneStats, playerTwoStats);
//        scrollView.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                scrollView.smoothScrollTo(0, (int) Utils.convertDpToPixel(145, context));
//
//            }
//        }, 100);
    }

    private void addRows(final String[] playerOneStats, final String[] playerTwoStats) {
        TotalAndCombatInfo playerOneInfo = new TotalAndCombatInfo(playerOneStats);
        TotalAndCombatInfo playerTwoInfo = new TotalAndCombatInfo(playerTwoStats);
        if (selectedComparison == CompareMode.LEVEL || selectedComparison == CompareMode.RANK)
            hiscoresTable.addView(createRow(-1, (int) playerOneInfo.getCombatLevel(), (int) playerTwoInfo.getCombatLevel(), false));
        if (selectedComparison == CompareMode.EXP)
            hiscoresTable.addView(createRow(-1, (int) playerOneInfo.getCombatExp(), (int) playerTwoInfo.getCombatExp(), false));
        List<Integer> lengths = new ArrayList<>();
        lengths.add(playerOneStats.length);
        lengths.add(playerTwoStats.length);
        int max = Collections.max(lengths);
        for (int i = 0; i < max; i++) {
            String[] playerOneLine = playerOneStats.length > i ? playerOneStats[i].split(",") : new String[]{};
            String[] playerTwoLine = playerTwoStats.length > i ? playerTwoStats[i].split(",") : new String[]{};
            lengths.clear();
            lengths.add(playerOneLine.length);
            lengths.add(playerTwoLine.length);
            int lineMax = Collections.max(lengths);
            if (lineMax == 3) {
                int playerOneRank = playerOneLine.length > 2 ? Integer.parseInt(playerOneLine[0]) : -1;
                int playerOneLevel = playerOneLine.length > 2 ? Integer.parseInt(playerOneLine[1]) : -1;
                long playerOneExp = playerOneLine.length > 2 ? Long.parseLong(playerOneLine[2]) : -1;

                int playerTwoRank = playerTwoLine.length > 2 ? Integer.parseInt(playerTwoLine[0]) : -1;
                int playerTwoLevel = playerTwoLine.length > 2 ? Integer.parseInt(playerTwoLine[1]) : -1;
                long playerTwoExp = playerTwoLine.length > 2 ? Long.parseLong(playerTwoLine[2]) : -1;

                if (selectedComparison == CompareMode.LEVEL) {
                    // use own calculated total level if the hiscores doesn't provide it
                    playerOneLevel = i == 0 && playerOneLevel == 0 ? playerOneInfo.getTotalLevel() : playerOneLevel;
                    playerTwoLevel = i == 0 && playerTwoLevel == 0 ? playerTwoInfo.getTotalLevel() : playerTwoLevel;
                    hiscoresTable.addView(createRow(i, playerOneLevel, playerTwoLevel, false));
                }
                if (selectedComparison == CompareMode.RANK)
                    hiscoresTable.addView(createRow(i, playerOneRank, playerTwoRank, false));
                if (selectedComparison == CompareMode.EXP) {
                    // use own calculated total exp if the hiscores doesn't provide it
                    playerOneExp = i == 0 && playerOneExp == 0 ? playerOneInfo.getTotalExp() : playerOneExp;
                    playerTwoExp = i == 0 && playerTwoExp == 0 ? playerTwoInfo.getTotalExp() : playerTwoExp;
                    hiscoresTable.addView(createRow(i, playerOneExp, playerTwoExp, false));
                }
            }
            // minigames
            if (lineMax == 2) {
                int playerOneRank = playerOneLine.length > 1 ? Integer.parseInt(playerOneLine[0]) : -1;
                int playerOneScore = playerOneLine.length > 1 ? Integer.parseInt(playerOneLine[1]) : -1;

                int playerTwoRank = playerTwoLine.length > 1 ? Integer.parseInt(playerTwoLine[0]) : -1;
                int playerTwoScore = playerTwoLine.length > 1 ? Integer.parseInt(playerTwoLine[1]) : -1;

                if (selectedComparison == CompareMode.RANK)
                    hiscoresMinigameTable.addView(createRow(i, playerOneRank, playerTwoRank, true));
                if (selectedComparison == CompareMode.EXP || selectedComparison == CompareMode.LEVEL)
                    hiscoresMinigameTable.addView(createRow(i, playerOneScore, playerTwoScore, true));
            }
        }
    }

    private TableRow createRow(int skillId, long playerOneResult, long playerTwoResult, boolean isMinigameRow) {
        TableRow.LayoutParams imageParams = new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 0.5f);
        imageParams.gravity = Gravity.CENTER;

        TableRow row = new TableRow(context);

        ImageView skillImageView = new ImageView(context);
        skillImageView.setImageDrawable(resources.getDrawable(RsUtils.getSkillResourceId(skillId)));
        skillImageView.setLayoutParams(isMinigameRow ? rowParams : imageParams);
        row.addView(skillImageView);

        TextView lvlTextView = new TextView(context);
        lvlTextView.setText(playerOneResult < 0 ? "-" : Utils.formatNumber(playerOneResult));
        lvlTextView.setGravity(Gravity.CENTER);
        lvlTextView.setLayoutParams(rowParams);
        lvlTextView.setTextColor(context.getResources().getColor(R.color.text));
        row.addView(lvlTextView);

        ImageView diffImageView = new ImageView(context);
        diffImageView.setImageDrawable(resources.getDrawable(getComparisonDrawable(skillId, playerOneResult, playerTwoResult)));
        diffImageView.setLayoutParams(imageParams);
        row.addView(diffImageView);

        TextView expTextView = new TextView(context);
        expTextView.setText(playerTwoResult < 0 ? "-" : Utils.formatNumber(playerTwoResult));
        expTextView.setGravity(Gravity.CENTER);
        expTextView.setLayoutParams(rowParams);
        expTextView.setTextColor(context.getResources().getColor(R.color.text));
        row.addView(expTextView);

        return row;
    }

    private int getComparisonDrawable(int skillId, long value1, long value2) {
        if (selectedComparison == CompareMode.RANK && skillId != -1) { //and must not be combat
            if (value1 > 0 && value2 <= 0)
                return R.drawable.arrow_up;
            if (value1 <= 0 && value2 > 0)
                return R.drawable.arrow_down;
            if (value1 < value2)
                return R.drawable.arrow_up;
            if (value1 > value2)
                return R.drawable.arrow_down;
            return R.drawable.arrow_equal;
        }
        if (value1 > value2)
            return R.drawable.arrow_up;
        else if (value1 < value2)
            return R.drawable.arrow_down;
        return R.drawable.arrow_equal;
    }

    private void activateRefreshCooldown() {
        if (refreshCount == 0)
            lastRefreshTimeMs = System.currentTimeMillis();
        refreshCount++;
    }

    private void clearTables() {
        hiscoresTable.removeAllViews();
        hiscoresMinigameTable.removeAllViews();
    }

    private String getHiscoresUrl(HiscoreMode mode) {
        String url;
        switch (mode) {
            case UIM:
                url = Constants.RS_HISCORES_UIM_URL;
                break;
            case IRONMAN:
                url = Constants.RS_HISCORES_IRONMAN_URL;
                break;
            case HCIM:
                url = Constants.RS_HISCORES_HCIM_URL;
                break;
            case DMM:
                url = Constants.RS_HISCORES_DMM_URL;
                break;
            case SDMM:
                url = Constants.RS_HISCORES_SDMM_URL;
                break;
            default:
                url = Constants.RS_HISCORES_URL;
        }
        return url;
    }

    private HiscoreMode getHiscoresMode(int buttonId) {
        HiscoreMode mode;
        switch (buttonId) {
            case R.id.hiscores_compare_ultimate_ironman:
                mode = HiscoreMode.UIM;
                break;
            case R.id.hiscores_compare_ironman:
                mode = HiscoreMode.IRONMAN;
                break;
            case R.id.hiscores_compare_hardcore_ironman:
                mode = HiscoreMode.HCIM;
                break;
            case R.id.hiscores_compare_dmm:
                mode = HiscoreMode.DMM;
                break;
            case R.id.hiscores_compare_sdmm:
                mode = HiscoreMode.SDMM;
                break;
            default:
                mode = HiscoreMode.NORMAL;
                break;
        }
        return mode;
    }

    private CompareMode getComparisonMode(int buttonId) {
        CompareMode mode;
        switch (buttonId) {
            case R.id.hiscores_compare_rank:
                mode = CompareMode.RANK;
                break;
            case R.id.hiscores_compare_exp:
                mode = CompareMode.EXP;
                break;
            default:
                mode = CompareMode.LEVEL;
                break;
        }
        return mode;
    }

    @Override
    public void cancelVolleyRequests() {
        AppController.getInstance().cancelPendingRequests(COMPARE_REQUEST_P1_TAG);
        AppController.getInstance().cancelPendingRequests(COMPARE_REQUEST_P2_TAG);
    }
}
