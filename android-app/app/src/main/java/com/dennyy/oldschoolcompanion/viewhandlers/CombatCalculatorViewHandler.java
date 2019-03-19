package com.dennyy.oldschoolcompanion.viewhandlers;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
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
import com.dennyy.oldschoolcompanion.models.General.NextLevel;
import com.dennyy.oldschoolcompanion.models.General.PlayerStats;
import com.dennyy.oldschoolcompanion.models.Hiscores.UserStats;

public class CombatCalculatorViewHandler extends BaseViewHandler implements HiscoreTypeSelectedListener, View.OnClickListener {
    public String hiscoresData;
    public String selectedRsn;
    public HiscoreType selectedHiscoreType;

    private static final String HISCORES_REQUEST_TAG = "cmb_calc_hiscores_request";
    private HiscoreTypeSelectorLayout hiscoreTypeSelectorLayout;
    private EditText rsnEditText;
    private long lastRefreshTimeMs;
    private int refreshCount;
    private SwipeRefreshLayout refreshLayout;

    public CombatCalculatorViewHandler(final Context context, View view) {
        super(context, view);

        rsnEditText = ((ClearableEditText) view.findViewById(R.id.cmb_calc_rsn_input)).getEditText();
        rsnEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND || actionId == EditorInfo.IME_ACTION_DONE) {
                    if (allowUpdateUser()) {
                        updateUser();
                    }
                    Utils.hideKeyboard(context, v);
                    selectedRsn = rsnEditText.getText().toString();
                    return true;
                }
                return false;
            }
        });
        refreshLayout = view.findViewById(R.id.cmb_calc_refresh_layout);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (allowUpdateUser())
                    updateUser();
            }
        });
        view.findViewById(R.id.get_cmb_calc_stats_button).setOnClickListener(this);
        hiscoreTypeSelectorLayout = view.findViewById(R.id.cmb_calc_hiscore_type_selector);
        hiscoreTypeSelectorLayout.setOnTypeSelectedListener(this);
        selectedHiscoreType = hiscoreTypeSelectorLayout.getHiscoreType();
        initializeListeners();
        initializeUser();
    }

    private void initializeUser() {
        final String inputRsn = getRsn(rsnEditText);
        UserStats cachedData = AppDb.getInstance(context).getUserStats(inputRsn, hiscoreTypeSelectorLayout.getHiscoreType());
        if (cachedData == null) {
            return;
        }
        showToast(resources.getString(R.string.last_updated_at, Utils.convertTime(cachedData.dateModified)), Toast.LENGTH_LONG);
        handleHiscoresData(cachedData.stats);
    }

    private void initializeListeners() {
        int[] ids = new int[]{ R.id.cmb_calc_att, R.id.cmb_calc_str, R.id.cmb_calc_def, R.id.cmb_calc_hp, R.id.cmb_calc_range, R.id.cmb_calc_mage, R.id.cmb_calc_pray };
        for (int id : ids) {
            ((EditText) view.findViewById(id)).setText(id == R.id.cmb_calc_hp ? "10" : "1");
            ((EditText) view.findViewById(id)).addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                Handler handler = new Handler(Looper.getMainLooper());
                Runnable workRunnable;

                @Override
                public void afterTextChanged(final Editable editable) {
                    handler.removeCallbacks(workRunnable);
                    workRunnable = new Runnable() {
                        @Override
                        public void run() {
                            if (!editable.toString().isEmpty()) {
                                calculateCombat();
                            }
                        }
                    };
                    handler.postDelayed(workRunnable, 500);
                }
            });
        }
        calculateCombat();
    }


    public void handleHiscoresData(String result) {
        PlayerStats playerStats = new PlayerStats(result);
        if (playerStats.isUnranked()) {
            showToast(resources.getString(R.string.player_not_found), Toast.LENGTH_LONG);
            return;
        }
        setLevelToEditText(R.id.cmb_calc_att, playerStats.getLevel(SkillType.ATTACK));
        setLevelToEditText(R.id.cmb_calc_str, playerStats.getLevel(SkillType.STRENGTH));
        setLevelToEditText(R.id.cmb_calc_def, playerStats.getLevel(SkillType.DEFENCE));
        setLevelToEditText(R.id.cmb_calc_hp, playerStats.getLevel(SkillType.HITPOINTS));
        setLevelToEditText(R.id.cmb_calc_range, playerStats.getLevel(SkillType.RANGED));
        setLevelToEditText(R.id.cmb_calc_mage, playerStats.getLevel(SkillType.MAGIC));
        setLevelToEditText(R.id.cmb_calc_pray, playerStats.getLevel(SkillType.PRAYER));
        calculateCombat();
    }

    private void calculateCombat() {
        int att = getLevelFromEditText(R.id.cmb_calc_att);
        int str = getLevelFromEditText(R.id.cmb_calc_str);
        int def = getLevelFromEditText(R.id.cmb_calc_def);
        int hp = getLevelFromEditText(R.id.cmb_calc_hp);
        int range = getLevelFromEditText(R.id.cmb_calc_range);
        int mage = getLevelFromEditText(R.id.cmb_calc_mage);
        int pray = getLevelFromEditText(R.id.cmb_calc_pray);
        Combat combat = new Combat(att, def, str, hp, range, pray, mage);
        NextLevel nextLevel = combat.getNextLevel();
        ((TextView) view.findViewById(R.id.cmb_calc_level)).setText(String.format("%s", combat.getLevel()));
        ((TextView) view.findViewById(R.id.cmb_calc_class)).setText(String.format("%s", combat.getCombatClass().getString()));
        ((TextView) view.findViewById(R.id.cmb_calc_nextlvl)).setText(String.format("Attack/Strength: %s\n Defence/Hitpoints: %s\n Prayer: %s\n Range: %s\n Mage: %s", nextLevel.AttackOrStrength, nextLevel.DefenceOrHitpoints, nextLevel.Prayer, nextLevel.Range, nextLevel.Mage));
    }

    private int getLevelFromEditText(int viewId) {
        View v = view.findViewById(viewId);
        int defaultLevel = viewId == R.id.cmb_calc_hp ? 10 : 1;
        if (!(v instanceof EditText)) {
            return defaultLevel;
        }
        String text = ((EditText) v).getText().toString();
        try {
            int level = Integer.parseInt(text);
            if (level > 0 && level < 100) {
                return level;
            }
            ((EditText) v).setText(String.valueOf(defaultLevel));
            return defaultLevel;
        }
        catch (NumberFormatException e) {
            ((EditText) v).setText("1");
            return defaultLevel;
        }
    }

    private void setLevelToEditText(int viewId, int level) {
        View v = view.findViewById(viewId);
        if (!(v instanceof EditText)) {
            return;
        }
        ((EditText) v).setText(String.format("%s", level));
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

    private void activateRefreshCooldown() {
        if (refreshCount == 0)
            lastRefreshTimeMs = System.currentTimeMillis();
        refreshCount++;
    }


    public void updateUser() {
        final String rsn = rsnEditText.getText().toString();
        refreshLayout.setRefreshing(true);
        wasRequesting = true;
        final HiscoreType hiscoreType = hiscoreTypeSelectorLayout.getHiscoreType();
        Utils.getString(hiscoreTypeSelectorLayout.getHiscoresUrl() + rsn, HISCORES_REQUEST_TAG, new Utils.VolleyCallback() {
            @Override
            public void onSuccess(String result) {
                hiscoresData = result;
                refreshLayout.setRefreshing(false);
                handleHiscoresData(result);
                AppDb.getInstance(context).insertOrUpdateUserStats(new UserStats(rsn, result, hiscoreType));
            }

            @Override
            public void onError(VolleyError error) {
                refreshLayout.setRefreshing(false);
                if (error.networkResponse != null && error.networkResponse.statusCode == 404) {
                    showToast(resources.getString(R.string.player_not_found), Toast.LENGTH_LONG);
                    AppDb.getInstance(context).insertOrUpdateUserStats(new UserStats(rsn, "", hiscoreType));
                }
                else if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                    UserStats cachedData = AppDb.getInstance(context).getUserStats(rsn, hiscoreType);
                    if (cachedData == null) {
                        showToast(resources.getString(R.string.failed_to_obtain_data, "hiscore data", resources.getString(R.string.network_error)), Toast.LENGTH_LONG);
                        return;
                    }

                    showToast(resources.getString(R.string.using_cached_data, Utils.convertTime(cachedData.dateModified)), Toast.LENGTH_LONG);
                    handleHiscoresData(cachedData.stats);
                }
                else {
                    String statusCode = String.valueOf(Utils.getStatusCode(error));
                    showToast(resources.getString(R.string.failed_to_obtain_data, "stats", statusCode), Toast.LENGTH_LONG);
                }
            }

            @Override
            public void always() {
                wasRequesting = false;
            }
        });
    }

    @Override
    public void onHiscoreTypeSelected(HiscoreType type) {
        if (rsnEditText.getText().toString().isEmpty() || !allowUpdateUser()) {
            return;
        }
        updateUser();
    }

    @Override
    public boolean wasRequesting() {
        return wasRequesting;
    }

    @Override
    public void cancelRunningTasks() {
        AppController.getInstance().cancelPendingRequests(HISCORES_REQUEST_TAG);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.get_cmb_calc_stats_button:
                if (allowUpdateUser()) {
                    updateUser();
                }
                Utils.hideKeyboard(context, this.view);
                break;
        }
    }

    public void updateIndicators() {
        hiscoreTypeSelectorLayout.setHiscoreType(selectedHiscoreType);
    }
}
