package com.dennyy.osrscompanion.layouthandlers;

import android.content.Context;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.NoConnectionError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.dennyy.osrscompanion.AppController;
import com.dennyy.osrscompanion.R;
import com.dennyy.osrscompanion.adapters.ActionsAdapter;
import com.dennyy.osrscompanion.adapters.NothingSelectedSpinnerAdapter;
import com.dennyy.osrscompanion.adapters.SkillSelectorSpinnerAdapter;
import com.dennyy.osrscompanion.customviews.ClearableEditText;
import com.dennyy.osrscompanion.customviews.HiscoreTypeSelectorLayout;
import com.dennyy.osrscompanion.enums.HiscoreType;
import com.dennyy.osrscompanion.helpers.ActionsDb;
import com.dennyy.osrscompanion.helpers.AppDb;
import com.dennyy.osrscompanion.helpers.Constants;
import com.dennyy.osrscompanion.helpers.RsUtils;
import com.dennyy.osrscompanion.helpers.Utils;
import com.dennyy.osrscompanion.models.General.Action;
import com.dennyy.osrscompanion.models.Hiscores.UserStats;

import java.util.ArrayList;
import java.util.Arrays;

public class SkillCalculatorViewHandler extends BaseViewHandler implements HiscoreTypeSelectorLayout.HiscoreTypeSelectedListener, View.OnClickListener, AdapterView.OnItemSelectedListener {
    public String hiscoresData;
    public HiscoreType selectedHiscoreType;
    public int selectedSkillId;
    public int fromLvl;
    public int toLvl;
    public int fromExp;
    public int toExp;

    private static final String HISCORES_REQUEST_TAG = "skill_calc_hiscores_request";
    private HiscoreTypeSelectorLayout hiscoreTypeSelectorLayout;
    private EditText rsnEditText;
    private long lastRefreshTimeMs;
    private int refreshCount;
    private SwipeRefreshLayout refreshLayout;
    private Spinner skillSelectorSpinner;
    private ActionsAdapter adapter;
    private ArrayList<Integer> skills;

    public SkillCalculatorViewHandler(final Context context, View view) {
        super(context, view);

        rsnEditText = ((ClearableEditText) view.findViewById(R.id.rsn_input)).getEditText();
        refreshLayout = view.findViewById(R.id.refresh_layout);

        view.findViewById(R.id.get_stats_button).setOnClickListener(this);
        hiscoreTypeSelectorLayout = view.findViewById(R.id.hiscore_type_selector);
        hiscoreTypeSelectorLayout.setOnTypeSelectedListener(this);
        selectedHiscoreType = hiscoreTypeSelectorLayout.getHiscoreType();

        skillSelectorSpinner = view.findViewById(R.id.skill_selector_spinner);
        skills = new ArrayList<>(Arrays.asList(6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 21, 22, 23));

        skillSelectorSpinner.setAdapter(new NothingSelectedSpinnerAdapter(new SkillSelectorSpinnerAdapter(context, skills), getString(R.string.select_a_skill), context));
        skillSelectorSpinner.setOnItemSelectedListener(this);

        ListView actionsListView = view.findViewById(R.id.actions_listview);
        adapter = new ActionsAdapter(context, new ArrayList<Action>());
        actionsListView.setAdapter(adapter);

        initializeListeners();
        if (!defaultRsn.isEmpty()) {
            initializeUser(defaultRsn);
        }
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Utils.hideKeyboard(context, SkillCalculatorViewHandler.super.view);
            }
        }, 500);
    }

    private void initializeUser(String rsn) {
        rsnEditText.setText(rsn);
        UserStats cachedData = AppDb.getInstance(context).getUserStats(rsn, hiscoreTypeSelectorLayout.getHiscoreType());
        if (cachedData == null) {
            return;
        }
        showToast(resources.getString(R.string.last_updated_at, Utils.convertTime(cachedData.dateModified)), Toast.LENGTH_LONG);
        handleHiscoresData(cachedData.stats);
    }

    private void initializeListeners() {
        rsnEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND || actionId == EditorInfo.IME_ACTION_DONE) {
                    if (allowUpdateUser()) {
                        updateUser();
                    }
                    Utils.hideKeyboard(context, view);
                    return true;
                }
                return false;
            }
        });
        view.findViewById(R.id.calc_with_target_level).setOnClickListener(this);
        view.findViewById(R.id.calc_with_target_exp).setOnClickListener(this);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (allowUpdateUser())
                    updateUser();
            }
        });
    }


    public void handleHiscoresData(String result) {
        String[] stats = result.split("\n");
        if (stats.length < 20) {
            showToast(resources.getString(R.string.player_not_found), Toast.LENGTH_LONG);
            return;
        }
        int selectedIndex = skillSelectorSpinner.getSelectedItemPosition() - 1;
        if (selectedIndex < 0) {
            return;
        }
        int selectedSkillId = skills.get(selectedIndex);
        for (int i = 0; i < stats.length; i++) {
            String[] line = stats[i].split(",");
            if (selectedSkillId == i && line.length == 3) {
                int level = Integer.parseInt(line[1]);
                long exp = Long.parseLong(line[2]);

                setValueToEditText(R.id.current_lvl, level);
                setValueToEditText(R.id.target_lvl, Math.min(99, level + 1));
                setValueToEditText(R.id.current_exp, Math.max(0, exp));
                setValueToEditText(R.id.target_exp, Math.min(Constants.MAX_EXP, exp + 1));
            }
        }
        adapter.updateListFromLvl(getLevelFromEditText(R.id.current_lvl), getLevelFromEditText(R.id.target_lvl));
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
        Utils.getString(hiscoreTypeSelectorLayout.getHiscoresUrl() + rsn, HISCORES_REQUEST_TAG, new Utils.VolleyCallback() {
            @Override
            public void onSuccess(String result) {
                hiscoresData = result;
                refreshLayout.setRefreshing(false);
                handleHiscoresData(result);
                AppDb.getInstance(context).insertOrUpdateUserStats(new UserStats(rsn, result, selectedHiscoreType));
            }

            @Override
            public void onError(VolleyError error) {
                refreshLayout.setRefreshing(false);
                if (error.networkResponse != null && error.networkResponse.statusCode == 404) {
                    showToast(resources.getString(R.string.player_not_found), Toast.LENGTH_LONG);
                    AppDb.getInstance(context).insertOrUpdateUserStats(new UserStats(rsn, "", selectedHiscoreType));
                }
                else if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                    UserStats cachedData = AppDb.getInstance(context).getUserStats(rsn, selectedHiscoreType);
                    if (cachedData == null) {
                        showToast(resources.getString(R.string.failed_to_obtain_data, "hiscore data", resources.getString(R.string.network_error)), Toast.LENGTH_LONG);
                        return;
                    }

                    showToast(resources.getString(R.string.using_cached_data, Utils.convertTime(cachedData.dateModified)), Toast.LENGTH_LONG);
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
    public void onHiscoreTypeSelected(HiscoreType type) {
        selectedHiscoreType = type;
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
            case R.id.get_stats_button:
                if (allowUpdateUser()) {
                    updateUser();
                }
                Utils.hideKeyboard(context, view);
                break;
            case R.id.calc_with_target_level:
                calculateWithTargetLevel();
                break;
            case R.id.calc_with_target_exp:
                calculateWithTargetExp();
                break;
        }
    }

    private void calculateWithTargetLevel() {
        if (isEditTextEmpty(R.id.current_lvl) && isEditTextEmpty(R.id.target_lvl)) {
            showToast(getString(R.string.enter_valid_target_lvl), Toast.LENGTH_SHORT);
            return;
        }
        if (getValueFromEditText(R.id.current_lvl, 1, 99) > getValueFromEditText(R.id.target_lvl, 1, 99)) {
            showToast(getString(R.string.lvl_from_higher_than_to), Toast.LENGTH_SHORT);
            return;
        }
        if (isEditTextEmpty(R.id.target_lvl)) {
            setValueToEditText(R.id.target_lvl, Math.min(getValueFromEditText(R.id.current_lvl, 1, 99) + 1, 99));
        }
        else if (isEditTextEmpty(R.id.current_lvl)) {
            setValueToEditText(R.id.current_lvl, Math.max(getValueFromEditText(R.id.target_lvl, 1, 99) - 1, 1));
        }
        fromLvl = getLevelFromEditText(R.id.current_lvl);
        toLvl = getLevelFromEditText(R.id.target_lvl);
        setValueToEditText(R.id.current_exp, RsUtils.exp(fromLvl));
        setValueToEditText(R.id.target_exp, RsUtils.exp(toLvl));
        adapter.updateListFromLvl(fromLvl, toLvl);
        Utils.hideKeyboard(context, this.view);
    }

    private void calculateWithTargetExp() {
        if (isEditTextEmpty(R.id.current_exp) && isEditTextEmpty(R.id.target_exp)) {
            showToast(getString(R.string.enter_valid_target_exp), Toast.LENGTH_SHORT);
            return;
        }
        if (getValueFromEditText(R.id.current_exp, 0, Constants.MAX_EXP) > getValueFromEditText(R.id.target_exp, 0, Constants.MAX_EXP)) {
            showToast(getString(R.string.exp_from_higher_than_to), Toast.LENGTH_SHORT);
            return;
        }
        if (isEditTextEmpty(R.id.target_exp)) {
            setValueToEditText(R.id.target_exp, Math.min(getValueFromEditText(R.id.current_exp, 0, Constants.MAX_EXP) + 1, Constants.MAX_EXP));
        }
        else if (isEditTextEmpty(R.id.current_exp)) {
            setValueToEditText(R.id.current_exp, Math.max(getValueFromEditText(R.id.target_exp, 0, Constants.MAX_EXP) - 1, 0));
        }

        fromExp = getValueFromEditText(R.id.current_exp, 0, Constants.MAX_EXP);
        toExp = getValueFromEditText(R.id.target_exp, 0, Constants.MAX_EXP);
        setValueToEditText(R.id.current_lvl, RsUtils.lvl(fromExp, true));
        setValueToEditText(R.id.target_lvl, RsUtils.lvl(toExp, true));
        adapter.updateListFromExp(fromExp, toExp);
        Utils.hideKeyboard(context, this.view);
    }

    public void updateIndicators() {
        hiscoreTypeSelectorLayout.setHiscoreType(selectedHiscoreType);
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        int selectedIndex = i - 1;
        if (selectedIndex < 0) {
            return;
        }
        selectedSkillId = skills.get(selectedIndex);
        ArrayList<Action> actions = ActionsDb.getInstance().getActions(selectedSkillId);
        adapter.updateList(actions);
        if (!Utils.isNullOrEmpty(hiscoresData)) {
            handleHiscoresData(hiscoresData);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    private boolean isEditTextEmpty(int viewId) {
        View v = view.findViewById(viewId);
        if (!(v instanceof EditText)) {
            return true;
        }
        String text = ((EditText) v).getText().toString();
        return text.isEmpty();
    }

    private int getLevelFromEditText(int viewId) {
        return getValueFromEditText(viewId, 1, 99);
    }

    public void setValueToEditText(int viewId, long value) {
        View v = view.findViewById(viewId);
        if (!(v instanceof EditText)) {
            return;
        }
        ((EditText) v).setText(String.format("%s", value));
    }

    private int getValueFromEditText(int viewId, int min, int max) {
        View v = view.findViewById(viewId);
        int defaultLevel = 1;
        if (!(v instanceof EditText)) {
            return defaultLevel;
        }
        String text = ((EditText) v).getText().toString();
        try {
            int value = Integer.parseInt(text);
            if (value >= min && value <= max) {
                return value;
            }
            return defaultLevel;
        }
        catch (NumberFormatException e) {
            ((EditText) v).setText(String.valueOf(defaultLevel));
            return defaultLevel;
        }
    }
}
