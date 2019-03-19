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
import com.dennyy.oldschoolcompanion.enums.HiscoreType;
import com.dennyy.oldschoolcompanion.enums.SkillType;
import com.dennyy.oldschoolcompanion.database.AppDb;
import com.dennyy.oldschoolcompanion.helpers.Constants;
import com.dennyy.oldschoolcompanion.helpers.Utils;
import com.dennyy.oldschoolcompanion.interfaces.HiscoreTypeSelectedListener;
import com.dennyy.oldschoolcompanion.models.General.Combat;
import com.dennyy.oldschoolcompanion.models.General.PlayerStats;
import com.dennyy.oldschoolcompanion.models.General.Skill;
import com.dennyy.oldschoolcompanion.models.Hiscores.UserStats;


public class HiscoresLookupViewHandler extends BaseViewHandler implements View.OnClickListener, HiscoreTypeSelectedListener {

    public String hiscoresData;
    public HiscoreType selectedHiscore = HiscoreType.NORMAL;

    private static final String HISCORES_REQUEST_TAG = "hiscoresrequest";
    private EditText rsnEditText;
    private TableLayout hiscoresTable;
    private TableLayout hiscoresMinigameTable;
    private SwipeRefreshLayout refreshLayout;
    private TableRow.LayoutParams rowParams;
    private HiscoreTypeSelectorLayout hiscoreTypeSelectorLayout;

    private long lastRefreshTimeMs;
    private int refreshCount;

    public HiscoresLookupViewHandler(final Context context, View view) {
        super(context, view);

        rowParams = new TableRow.LayoutParams(0, (int) Utils.convertDpToPixel(35, context), 1f);
        hiscoreTypeSelectorLayout = view.findViewById(R.id.hiscore_type_selector);
        rsnEditText = ((ClearableEditText) view.findViewById(R.id.hiscores_rsn_input)).getEditText();
        hiscoresTable = view.findViewById(R.id.hiscores_table);
        hiscoresMinigameTable = view.findViewById(R.id.hiscores_minigame_table);
        refreshLayout = view.findViewById(R.id.hiscores_refresh_layout);

        initializeListeners();
        initializeCachedUser();
    }

    private void initializeListeners() {
        hiscoreTypeSelectorLayout.setOnTypeSelectedListener(this);
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

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (allowUpdateUser())
                    updateUser();
            }
        });
        view.findViewById(R.id.hiscores_lookup_button).setOnClickListener(this);
    }

    private void initializeCachedUser() {
        String inputRsn = getRsn(rsnEditText);
        if (Utils.isNullOrEmpty(inputRsn)) {
            return;
        }
        rsnEditText.setText(inputRsn);
        UserStats cachedData = AppDb.getInstance(context).getUserStats(inputRsn, selectedHiscore);
        if (cachedData == null) {
            return;
        }
        hiscoresData = cachedData.stats;
        showToast(resources.getString(R.string.last_updated_at, Utils.convertTime(cachedData.dateModified)), Toast.LENGTH_LONG);
        handleHiscoresData(hiscoresData);
        view.findViewById(R.id.hiscores_data_layout).setVisibility(View.VISIBLE);

    }

    @Override
    public boolean wasRequesting() {
        return wasRequesting;
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        switch (id) {
            case R.id.hiscores_lookup_button:
                Utils.hideKeyboard(context, rsnEditText);
                if (allowUpdateUser())
                    updateUser();
                break;
        }
    }

    public boolean allowUpdateUser() {
        long refreshPeriod = System.currentTimeMillis() - lastRefreshTimeMs;
        if (rsnEditText.getText().toString().isEmpty()) {
            showToast(resources.getString(R.string.empty_rsn_error), Toast.LENGTH_SHORT);
            refreshLayout.setRefreshing(false);
            return false;
        }
        if (refreshPeriod >= Constants.REFRESH_COOLDOWN_MS) {
            refreshCount = 0;
        }
        if (refreshPeriod < Constants.REFRESH_COOLDOWN_MS && refreshCount >= Constants.MAX_REFRESH_COUNT) {
            double timeLeft = (Constants.REFRESH_COOLDOWN_MS - refreshPeriod) / 1000;
            showToast(resources.getString(R.string.wait_before_refresh, (int) Math.ceil(timeLeft + 0.5)), Toast.LENGTH_SHORT);
            refreshLayout.setRefreshing(false);
            return false;
        }
        activateRefreshCooldown();
        return true;
    }

    public void updateUser() {
        final String rsn = rsnEditText.getText().toString();
        refreshLayout.setRefreshing(true);
        wasRequesting = true;
        Utils.getString(hiscoreTypeSelectorLayout.getHiscoresUrl() + rsn, HISCORES_REQUEST_TAG, new Utils.VolleyCallback() {
            @Override
            public void onSuccess(String result) {
                hiscoresData = result;
                refreshLayout.setRefreshing(false);
                handleHiscoresData(result);
                AppDb.getInstance(context).insertOrUpdateUserStats(new UserStats(rsn, result, selectedHiscore));
                view.findViewById(R.id.hiscores_data_layout).setVisibility(View.VISIBLE);
            }

            @Override
            public void onError(VolleyError error) {
                refreshLayout.setRefreshing(false);
                if (error.networkResponse != null && error.networkResponse.statusCode == 404) {
                    showToast(resources.getString(R.string.player_not_found), Toast.LENGTH_LONG);
                    AppDb.getInstance(context).insertOrUpdateUserStats(new UserStats(rsn, "", selectedHiscore));
                }
                else if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                    UserStats cachedData = AppDb.getInstance(context).getUserStats(rsn, selectedHiscore);
                    if (cachedData == null) {
                        showToast(resources.getString(R.string.failed_to_obtain_data, "hiscore data", resources.getString(R.string.network_error)), Toast.LENGTH_LONG);
                        return;
                    }

                    showToast(resources.getString(R.string.using_cached_data, Utils.convertTime(cachedData.dateModified)), Toast.LENGTH_LONG);
                    view.findViewById(R.id.hiscores_data_layout).setVisibility(View.VISIBLE);
                    handleHiscoresData(cachedData.stats);
                }
                else {
                    String statusCode = String.valueOf(Utils.getStatusCode(error));
                    showToast(resources.getString(R.string.failed_to_obtain_data, "hiscore data", statusCode), Toast.LENGTH_LONG);
                }
            }

            @Override
            public void always() {
                wasRequesting = false;
            }
        });
    }

    public void handleHiscoresData(String result) {
        clearTables();
        PlayerStats playerStats = new PlayerStats(result);
        if (playerStats.isUnranked()) {
            showToast(resources.getString(R.string.player_not_found), Toast.LENGTH_LONG);
            return;
        }
        Combat combat = playerStats.getCombat();
        hiscoresTable.addView(createRow(-1, (int) combat.getLevel(), -1, playerStats.getCombatExp(), false));

        for (SkillType skillType : playerStats.keySet()) {
            Skill skill = playerStats.getSkill(skillType);
            if (skill.isMinigame()) {
                hiscoresMinigameTable.addView(createRow(skill.getId(), -1, skill.getRank(), skill.getScore(), true));
            }
            else if (skill.isOverall()) {
                hiscoresTable.addView(createRow(skill.getId(), playerStats.getTotalLevel(), skill.getRank(), playerStats.getTotalExp(), skill.isMinigame()));
            }
            else {
                hiscoresTable.addView(createRow(skill.getId(), skill.getLevel(), skill.getRank(), skill.getExp(), skill.isMinigame()));
            }
        }
    }

    private TableRow createRow(int skillId, int lvl, int rank, long exp, boolean isMinigameRow) {
        TableRow.LayoutParams imageParams = new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 0.5f);
        imageParams.gravity = Gravity.CENTER;

        TableRow row = new TableRow(context);

        ImageView skillImageView = new ImageView(context);
        skillImageView.setImageDrawable(resources.getDrawable(SkillType.fromId(skillId).drawable));
        skillImageView.setLayoutParams(imageParams);
        row.addView(skillImageView);
        if (!isMinigameRow) {
            TextView lvlTextView = new TextView(context);
            lvlTextView.setText(lvl < 0 ? "-" : String.valueOf(lvl));
            lvlTextView.setGravity(Gravity.CENTER);
            lvlTextView.setLayoutParams(rowParams);
            lvlTextView.setTextColor(context.getResources().getColor(R.color.text));
            row.addView(lvlTextView);
        }

        TextView rankTextView = new TextView(context);
        rankTextView.setText(rank < 0 ? "-" : Utils.formatNumber(rank));
        rankTextView.setGravity(Gravity.CENTER);
        rankTextView.setLayoutParams(rowParams);
        rankTextView.setTextColor(context.getResources().getColor(R.color.text));
        row.addView(rankTextView);

        TextView expTextView = new TextView(context);
        expTextView.setText(exp < 0 ? "-" : Utils.formatNumber(exp));
        expTextView.setGravity(Gravity.CENTER);
        expTextView.setLayoutParams(rowParams);
        expTextView.setTextColor(context.getResources().getColor(R.color.text));
        row.addView(expTextView);

        return row;
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

    @Override
    public void cancelRunningTasks() {
        AppController.getInstance().cancelPendingRequests(HISCORES_REQUEST_TAG);
    }

    public void updateIndicators() {
        hiscoreTypeSelectorLayout.setHiscoreType(selectedHiscore);
    }

    @Override
    public void onHiscoreTypeSelected(HiscoreType type) {
        if (!allowUpdateUser())
            return;
        selectedHiscore = type;
        updateUser();
    }
}