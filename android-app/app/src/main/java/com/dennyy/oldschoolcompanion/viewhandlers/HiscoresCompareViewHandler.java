package com.dennyy.oldschoolcompanion.viewhandlers;

import android.content.Context;
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
import com.dennyy.oldschoolcompanion.AppController;
import com.dennyy.oldschoolcompanion.R;
import com.dennyy.oldschoolcompanion.customviews.ClearableEditText;
import com.dennyy.oldschoolcompanion.customviews.HiscoreTypeSelectorLayout;
import com.dennyy.oldschoolcompanion.customviews.LineIndicatorButton;
import com.dennyy.oldschoolcompanion.enums.CompareMode;
import com.dennyy.oldschoolcompanion.enums.HiscoreType;
import com.dennyy.oldschoolcompanion.enums.SkillType;
import com.dennyy.oldschoolcompanion.database.AppDb;
import com.dennyy.oldschoolcompanion.helpers.Constants;
import com.dennyy.oldschoolcompanion.helpers.Utils;
import com.dennyy.oldschoolcompanion.interfaces.HiscoreTypeSelectedListener;
import com.dennyy.oldschoolcompanion.models.General.PlayerStats;
import com.dennyy.oldschoolcompanion.models.General.Skill;
import com.dennyy.oldschoolcompanion.models.Hiscores.UserStats;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;


public class HiscoresCompareViewHandler extends BaseViewHandler implements View.OnClickListener, HiscoreTypeSelectedListener {
    private EditText rsnEditText;
    private EditText rsn2EditText;
    private TableLayout hiscoresTable;
    private TableLayout hiscoresMinigameTable;
    private SwipeRefreshLayout refreshLayout;
    private TableRow.LayoutParams rowParams;
    private HiscoreTypeSelectorLayout hiscoreTypeSelectorLayout;
    private HashMap<CompareMode, Integer> comparisonIndicators;

    public HiscoreType selectedHiscore = HiscoreType.NORMAL;
    public CompareMode selectedComparison = CompareMode.LEVEL;
    public UserStats playerOneStats;
    public UserStats playerTwoStats;

    private long lastRefreshTimeMs;
    private int refreshCount;

    private static final String COMPARE_REQUEST_P1_TAG = "comparerequest";
    private static final String COMPARE_REQUEST_P2_TAG = "comparerequest2";

    public HiscoresCompareViewHandler(final Context context, View view) {
        super(context, view);
        rowParams = new TableRow.LayoutParams(0, (int) Utils.convertDpToPixel(35, context), 1f);

        hiscoreTypeSelectorLayout = view.findViewById(R.id.hiscore_type_selector);
        hiscoreTypeSelectorLayout.setOnTypeSelectedListener(this);
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
        hiscoresTable = view.findViewById(R.id.hiscores_compare_table);
        hiscoresMinigameTable = view.findViewById(R.id.hiscores_compare_minigame_table);
        refreshLayout = view.findViewById(R.id.hiscores_compare_refresh_layout);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (allowUpdateUser())
                    getPlayerOneStats();
            }
        });
        view.findViewById(R.id.hiscores_compare_lookup_button).setOnClickListener(this);

        comparisonIndicators = new HashMap<>();
        comparisonIndicators.put(CompareMode.LEVEL, R.id.hiscores_compare_lvl);
        comparisonIndicators.put(CompareMode.RANK, R.id.hiscores_compare_rank);
        comparisonIndicators.put(CompareMode.EXP, R.id.hiscores_compare_exp);

        for (Map.Entry<CompareMode, Integer> entry : comparisonIndicators.entrySet()) {
            view.findViewById(entry.getValue()).setOnClickListener(this);
        }
        getRsn(rsnEditText);
        clearTables();
    }

    @Override
    public boolean wasRequesting() {
        return wasRequesting;
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
            case R.id.hiscores_compare_exp:
            case R.id.hiscores_compare_lvl:
            case R.id.hiscores_compare_rank:
                updateUserFromComparisonType(id);
                break;
        }
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
        hiscoreTypeSelectorLayout.setHiscoreType(selectedHiscore);

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
        cancelRunningTasks();
        refreshLayout.setRefreshing(true);
        wasRequesting = true;
        Utils.getString(hiscoreTypeSelectorLayout.getHiscoresUrl() + rsn, COMPARE_REQUEST_P1_TAG, new Utils.VolleyCallback() {
            @Override
            public void onSuccess(String result) {
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
                    getPlayerTwoStats(rsn, cachedData == null ? "" : cachedData.stats, rsn2);
                }
                else {
                    showToast(resources.getString(R.string.failed_to_obtain_data, "player 1 data", error.getClass().getSimpleName()), Toast.LENGTH_LONG);
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
        wasRequesting = true;
        Utils.getString(hiscoreTypeSelectorLayout.getHiscoresUrl() + rsn2, COMPARE_REQUEST_P2_TAG, new Utils.VolleyCallback() {
            @Override
            public void onSuccess(String result) {
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
                    clearTables();
                    handleHiscoresData(rsn, playerOneStats, rsn2, cachedData == null ? "" : cachedData.stats);
                }
                else
                    showToast(resources.getString(R.string.failed_to_obtain_data, "player 2 data", error.getClass().getSimpleName()), Toast.LENGTH_LONG);
                refreshLayout.setRefreshing(false);
            }

            @Override
            public void always() {
                wasRequesting = false;
                refreshLayout.setRefreshing(false);
            }
        });
    }

    public void handleHiscoresData(String rsn, String result1, String rsn2, String result2) {
        PlayerStats playerOneStats = new PlayerStats(result1);
        PlayerStats playerTwoStats = new PlayerStats(result2);
        if (playerOneStats.isUnranked() && playerTwoStats.isUnranked()) {
            showToast(resources.getString(R.string.hiscores_compare_both_not_ranked), Toast.LENGTH_LONG);
            refreshLayout.setRefreshing(false);
            return;
        }
        view.findViewById(R.id.hiscores_compare_data_layout).setVisibility(View.VISIBLE);
        rsnEditText.setText(rsn);
        rsn2EditText.setText(rsn2);
        ((TextView) view.findViewById(R.id.hiscores_compare_player_one_name)).setText(rsn);
        ((TextView) view.findViewById(R.id.hiscores_compare_player_two_name)).setText(rsn2);
        ((TextView) view.findViewById(R.id.hiscores_compare_minigame_player_one_name)).setText(rsn);
        ((TextView) view.findViewById(R.id.hiscores_compare_minigame_player_two_name)).setText(rsn2);
        addRows(playerOneStats, playerTwoStats);
    }

    private void addRows(final PlayerStats playerOneStats, final PlayerStats playerTwoStats) {
        if (selectedComparison == CompareMode.LEVEL || selectedComparison == CompareMode.RANK)
            hiscoresTable.addView(createRow(-1, (int) playerOneStats.getCombat().getLevel(), (int) playerTwoStats.getCombat().getLevel(), false));
        if (selectedComparison == CompareMode.EXP)
            hiscoresTable.addView(createRow(-1, playerOneStats.getCombatExp(), playerTwoStats.getCombatExp(), false));

        Set<SkillType> keySet = playerOneStats.keySet();
        if (keySet.size() < playerTwoStats.size()) {
            keySet = playerTwoStats.keySet();
        }
        for (SkillType skillType : keySet) {
            int skillId = skillType.id;
            Skill playerOneSkill = playerOneStats.getSkill(skillType);
            Skill playerTwoSkill = playerTwoStats.getSkill(skillType);
            if (selectedComparison == CompareMode.LEVEL) {
                if (playerOneSkill.isMinigame()) {
                    hiscoresMinigameTable.addView(createRow(skillId, playerOneSkill.getScore(), playerTwoSkill.getScore(), true));
                }
                else if (playerOneSkill.isOverall()) {
                    hiscoresTable.addView(createRow(skillId, playerOneStats.getTotalLevel(), playerTwoStats.getTotalLevel(), false));
                }
                else {
                    hiscoresTable.addView(createRow(skillId, playerOneSkill.getLevel(), playerTwoSkill.getLevel(), false));
                }
            }
            else if (selectedComparison == CompareMode.RANK) {
                if (playerOneSkill.isMinigame()) {
                    hiscoresMinigameTable.addView(createRow(skillId, playerOneSkill.getRank(), playerTwoSkill.getRank(), true));
                }
                else {
                    hiscoresTable.addView(createRow(skillId, playerOneSkill.getRank(), playerTwoSkill.getRank(), false));
                }
            }
            else if (selectedComparison == CompareMode.EXP) {
                if (playerOneSkill.isMinigame()) {
                    hiscoresMinigameTable.addView(createRow(skillId, playerOneSkill.getScore(), playerTwoSkill.getScore(), true));
                }
                else if (playerOneSkill.isOverall()) {
                    hiscoresTable.addView(createRow(skillId, playerOneStats.getTotalExp(), playerTwoStats.getTotalExp(), false));
                }
                else {
                    hiscoresTable.addView(createRow(skillId, playerOneSkill.getExp(), playerTwoSkill.getExp(), false));
                }
            }
        }
    }

    private TableRow createRow(int skillId, long playerOneResult, long playerTwoResult, boolean isMinigameRow) {
        TableRow.LayoutParams imageParams = new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 0.5f);
        imageParams.gravity = Gravity.CENTER;

        TableRow row = new TableRow(context);

        ImageView skillImageView = new ImageView(context);
        skillImageView.setImageDrawable(resources.getDrawable(SkillType.fromId(skillId).drawable));
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
    public void cancelRunningTasks() {
        AppController.getInstance().cancelPendingRequests(COMPARE_REQUEST_P1_TAG);
        AppController.getInstance().cancelPendingRequests(COMPARE_REQUEST_P2_TAG);
    }

    @Override
    public void onHiscoreTypeSelected(HiscoreType type) {
        if (!allowUpdateUser())
            return;
        selectedHiscore = type;
        updateIndicators();
        getPlayerOneStats();
    }
}