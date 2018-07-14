package com.dennyy.osrscompanion.layouthandlers;

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
import com.dennyy.osrscompanion.AppController;
import com.dennyy.osrscompanion.R;
import com.dennyy.osrscompanion.customviews.ClearableEditText;
import com.dennyy.osrscompanion.customviews.HiscoreTypeSelectorLayout;
import com.dennyy.osrscompanion.enums.HiscoreType;
import com.dennyy.osrscompanion.helpers.AppDb;
import com.dennyy.osrscompanion.helpers.Constants;
import com.dennyy.osrscompanion.helpers.RsUtils;
import com.dennyy.osrscompanion.helpers.Utils;
import com.dennyy.osrscompanion.models.General.Combat;
import com.dennyy.osrscompanion.models.General.NextLevel;
import com.dennyy.osrscompanion.models.Hiscores.UserStats;

import java.util.ArrayList;
import java.util.List;

public class CombatCalculatorViewHandler extends BaseViewHandler implements HiscoreTypeSelectorLayout.HiscoreTypeSelectedListener, View.OnClickListener {
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
        if (selectedRsn != null && !selectedRsn.isEmpty()) {
            initializeUser(selectedRsn);
        }
        else if (!defaultRsn.isEmpty()) {
            initializeUser(defaultRsn);
        }
    }

    private void initializeUser(String rsn) {
        rsnEditText.setText(rsn);
        UserStats cachedData = AppDb.getInstance(context).getUserStats(rsn, hiscoreTypeSelectorLayout.getHiscoreType());
        if (cachedData == null) {
            return;
        }
        showToast(resources.getString(R.string.last_updated_at, Utils.convertTime(cachedData.dateModified)), Toast.LENGTH_LONG);
        handleHiscoresData(cachedData.stats);
        view.findViewById(R.id.cmb_calc_data_layout).setVisibility(View.VISIBLE);
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

                Handler handler = new Handler(Looper.getMainLooper() /*UI thread*/);
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
                    handler.postDelayed(workRunnable, 500 /*delay*/);
                }
            });
        }
        calculateCombat();
    }


    public void handleHiscoresData(String result) {
        String[] stats = result.split("\n");
        List<Integer> cmb = new ArrayList<>();
        for (int i = 0; i < stats.length; i++) {
            String[] line = stats[i].split(",");
            if (line.length == 3) {
                int level = Integer.parseInt(line[1]);
                cmb.add(level);
            }
            if (i == 8) {
                break;
            }
        }
        int att = cmb.get(1);
        int def = cmb.get(2);
        int str = cmb.get(3);
        int hp = cmb.get(4);
        int range = cmb.get(5);
        int pray = cmb.get(6);
        int mage = cmb.get(7);

        setLevelToEditText(R.id.cmb_calc_att, att);
        setLevelToEditText(R.id.cmb_calc_str, str);
        setLevelToEditText(R.id.cmb_calc_def, def);
        setLevelToEditText(R.id.cmb_calc_hp, hp);
        setLevelToEditText(R.id.cmb_calc_range, range);
        setLevelToEditText(R.id.cmb_calc_mage, mage);
        setLevelToEditText(R.id.cmb_calc_pray, pray);
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
        Combat combat = RsUtils.combat(att, def, str, hp, range, pray, mage);
        NextLevel nextLevel = RsUtils.getNextLevel(att, def, str, hp, range, pray, mage);
        ((TextView) view.findViewById(R.id.cmb_calc_level)).setText(String.format("%s", combat.level));
        ((TextView) view.findViewById(R.id.cmb_calc_class)).setText(String.format("%s", combat.combatClass.getString()));
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
        defaultRsn = rsn;
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
                view.findViewById(R.id.cmb_calc_data_layout).setVisibility(View.VISIBLE);
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
                    view.findViewById(R.id.hiscores_data_layout).setVisibility(View.VISIBLE);
                    handleHiscoresData(cachedData.stats);
                }
                else
                    showToast(resources.getString(R.string.failed_to_obtain_data, "stats", error.getMessage()), Toast.LENGTH_LONG);
            }

            @Override
            public void always() {
                wasRequesting = false;
            }
        });
    }

    @Override
    public void onTypeSelected(HiscoreType type) {
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
    public void cancelVolleyRequests() {
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
                break;
        }
    }

    public void updateIndicators() {
        hiscoreTypeSelectorLayout.setHiscoreType(selectedHiscoreType);
    }
}
