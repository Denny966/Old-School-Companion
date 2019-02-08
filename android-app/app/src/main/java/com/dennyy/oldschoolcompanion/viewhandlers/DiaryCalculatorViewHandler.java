package com.dennyy.oldschoolcompanion.viewhandlers;

import android.content.Context;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.NoConnectionError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.dennyy.oldschoolcompanion.AppController;
import com.dennyy.oldschoolcompanion.R;
import com.dennyy.oldschoolcompanion.adapters.DiaryListAdapter;
import com.dennyy.oldschoolcompanion.asynctasks.LoadDiariesTask;
import com.dennyy.oldschoolcompanion.customviews.ClearableEditText;
import com.dennyy.oldschoolcompanion.customviews.HiscoreTypeSelectorLayout;
import com.dennyy.oldschoolcompanion.database.AppDb;
import com.dennyy.oldschoolcompanion.enums.HiscoreType;
import com.dennyy.oldschoolcompanion.helpers.Constants;
import com.dennyy.oldschoolcompanion.helpers.Utils;
import com.dennyy.oldschoolcompanion.interfaces.DiariesLoadedListener;
import com.dennyy.oldschoolcompanion.interfaces.HiscoreTypeSelectedListener;
import com.dennyy.oldschoolcompanion.models.AchievementDiary.DiariesMap;
import com.dennyy.oldschoolcompanion.models.Hiscores.UserStats;

import me.zhanghai.android.materialprogressbar.MaterialProgressBar;

public class DiaryCalculatorViewHandler extends BaseViewHandler implements HiscoreTypeSelectedListener, View.OnClickListener, ExpandableListView.OnGroupExpandListener, DiariesLoadedListener {
    public String hiscoresData;
    public HiscoreType selectedHiscoreType;
    public int lastExpandedPosition = -1;


    private static final String HISCORES_REQUEST_TAG = "diary_calc_hiscores_request";
    private HiscoreTypeSelectorLayout hiscoreTypeSelectorLayout;
    private EditText rsnEditText;
    private long lastRefreshTimeMs;
    private int refreshCount;
    private MaterialProgressBar refreshLayout;
    private DiaryListAdapter adapter;
    private ExpandableListView diaryListView;
    private DiariesLoadedListener diariesLoadedListener;

    public DiaryCalculatorViewHandler(final Context context, final View view, DiariesLoadedListener callback) {
        super(context, view);

        rsnEditText = ((ClearableEditText) view.findViewById(R.id.rsn_input)).getEditText();
        refreshLayout = view.findViewById(R.id.progressBar);
        diaryListView = view.findViewById(R.id.expandable_diary_listview);
        view.findViewById(R.id.get_stats_button).setOnClickListener(this);
        hiscoreTypeSelectorLayout = view.findViewById(R.id.hiscore_type_selector);
        hiscoreTypeSelectorLayout.setOnTypeSelectedListener(this);
        selectedHiscoreType = hiscoreTypeSelectorLayout.getHiscoreType();
        diariesLoadedListener = callback;

        initializeListeners();
        new LoadDiariesTask(context, this).execute();
        hideKeyboard();
    }

    @Override
    public void onDiariesLoaded(DiariesMap diariesMap) {
        adapter = new DiaryListAdapter(context, diariesMap);
        diaryListView.setAdapter(adapter);
        diaryListView.collapseGroup(lastExpandedPosition);
        diaryListView.setOnGroupExpandListener(DiaryCalculatorViewHandler.this);
        String inputRsn = getRsn(rsnEditText);
        initializeUser(inputRsn);
        if (diariesLoadedListener != null) {
            diariesLoadedListener.onDiariesLoaded(diariesMap);
        }
    }

    @Override
    public void onDiariesLoadError() {
        showToast(getString(R.string.unexpected_error_try_reopen), Toast.LENGTH_LONG);
    }

    private void initializeUser(String rsn) {
        if (!rsn.isEmpty()) {
            rsnEditText.setText(rsn);
            UserStats cachedData = AppDb.getInstance(context).getUserStats(rsn, hiscoreTypeSelectorLayout.getHiscoreType());
            if (cachedData == null) {
                return;
            }
            showToast(resources.getString(R.string.last_updated_at, Utils.convertTime(cachedData.dateModified)), Toast.LENGTH_LONG);
            handleHiscoresData(cachedData.stats);
        }
        setListViewHeight(diaryListView);
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
    }

    public void handleHiscoresData(String result) {
        String[] stats = result.split("\n");
        if (stats.length < Constants.REQUIRED_STATS_LENGTH) {
            showToast(resources.getString(R.string.player_not_found), Toast.LENGTH_LONG);
            return;
        }

        adapter.updateStats(stats);
        setListViewHeight(diaryListView);
    }

    private void setListViewHeight(ExpandableListView listView) {
        diaryListView.measure(View.MeasureSpec.makeMeasureSpec(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));

        int listviewHeight = diaryListView.getMeasuredHeight() * adapter.getGroupCount() + (adapter.getGroupCount() * diaryListView.getDividerHeight());
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = listviewHeight;
        listView.setLayoutParams(params);
        listView.requestLayout();
    }

    private void setListViewHeight(ExpandableListView listView, int group) {
        int totalHeight = 0;
        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.EXACTLY);
        for (int i = 0; i < adapter.getGroupCount(); i++) {
            View groupItem = adapter.getGroupView(i, false, null, listView);
            groupItem.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);

            totalHeight += groupItem.getMeasuredHeight();
            if (((listView.isGroupExpanded(i)) && (i != group)) || ((!listView.isGroupExpanded(i)) && (i == group))) {
                for (int j = 0; j < adapter.getChildrenCount(i); j++) {
                    View listItem = adapter.getChildView(i, j, false, null, listView);
                    listItem.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
                    totalHeight += listItem.getMeasuredHeight();
                }
            }
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        int height = totalHeight + (listView.getDividerHeight() * (adapter.getGroupCount() - 1));
        if (height < 10)
            height = 200;
        params.height = height;
        listView.setLayoutParams(params);
        listView.requestLayout();
    }

    public boolean allowUpdateUser() {
        long refreshPeriod = System.currentTimeMillis() - lastRefreshTimeMs;
        if (rsnEditText.getText().toString().isEmpty()) {
            showToast(resources.getString(R.string.empty_rsn_error), Toast.LENGTH_SHORT);
            refreshLayout.setVisibility(View.GONE);
            return false;
        }
        if (refreshPeriod >= Constants.REFRESH_COOLDOWN_MS) {
            refreshCount = 0;
        }
        if (refreshPeriod < Constants.REFRESH_COOLDOWN_MS && refreshCount >= Constants.MAX_REFRESH_COUNT) {
            double timeLeft = (Constants.REFRESH_COOLDOWN_MS - refreshPeriod) / 1000;
            showToast(resources.getString(R.string.wait_before_refresh, (int) Math.ceil(timeLeft + 0.5)), Toast.LENGTH_SHORT);
            refreshLayout.setVisibility(View.GONE);
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
        refreshLayout.setVisibility(View.VISIBLE);
        wasRequesting = true;
        Utils.getString(hiscoreTypeSelectorLayout.getHiscoresUrl() + rsn, HISCORES_REQUEST_TAG, new Utils.VolleyCallback() {
            @Override
            public void onSuccess(String result) {
                hiscoresData = result;
                handleHiscoresData(result);
                AppDb.getInstance(context).insertOrUpdateUserStats(new UserStats(rsn, result, selectedHiscoreType));
            }

            @Override
            public void onError(VolleyError error) {
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
                    showToast(resources.getString(R.string.failed_to_obtain_data, "stats", error.getClass().getSimpleName()), Toast.LENGTH_LONG);
            }

            @Override
            public void always() {
                wasRequesting = false;
                refreshLayout.setVisibility(View.GONE);
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
    public void cancelRunningTasks() {
        AppController.getInstance().cancelPendingRequests(HISCORES_REQUEST_TAG);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.get_stats_button:
                hideKeyboard();
                if (allowUpdateUser())
                    updateUser();
                break;
        }
    }

    public void updateIndicators() {
        hiscoreTypeSelectorLayout.setHiscoreType(selectedHiscoreType);
    }

    @Override
    public void onGroupExpand(int groupPosition) {
        if (lastExpandedPosition != -1 && groupPosition != lastExpandedPosition) {
            diaryListView.collapseGroup(lastExpandedPosition);
        }
        lastExpandedPosition = groupPosition;
        setListViewHeight(diaryListView, groupPosition);
    }
}