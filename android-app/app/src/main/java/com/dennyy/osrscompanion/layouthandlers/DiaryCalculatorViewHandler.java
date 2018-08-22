package com.dennyy.osrscompanion.layouthandlers;

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
import com.dennyy.osrscompanion.AppController;
import com.dennyy.osrscompanion.R;
import com.dennyy.osrscompanion.adapters.DiaryListAdapter;
import com.dennyy.osrscompanion.customviews.ClearableEditText;
import com.dennyy.osrscompanion.customviews.HiscoreTypeSelectorLayout;
import com.dennyy.osrscompanion.enums.DiaryType;
import com.dennyy.osrscompanion.enums.HiscoreType;
import com.dennyy.osrscompanion.helpers.AppDb;
import com.dennyy.osrscompanion.helpers.Constants;
import com.dennyy.osrscompanion.helpers.Utils;
import com.dennyy.osrscompanion.models.AchievementDiary.Diary;
import com.dennyy.osrscompanion.models.AchievementDiary.DiaryLevel;
import com.dennyy.osrscompanion.models.AchievementDiary.DiaryRequirement;
import com.dennyy.osrscompanion.models.Hiscores.UserStats;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

import me.zhanghai.android.materialprogressbar.MaterialProgressBar;

public class DiaryCalculatorViewHandler extends BaseViewHandler implements HiscoreTypeSelectorLayout.HiscoreTypeSelectedListener, View.OnClickListener {
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

    public DiaryCalculatorViewHandler(final Context context, final View view) {
        super(context, view);

        rsnEditText = ((ClearableEditText) view.findViewById(R.id.rsn_input)).getEditText();
        refreshLayout = view.findViewById(R.id.progressBar);
        diaryListView = view.findViewById(R.id.expandable_diary_listview);
        adapter = new DiaryListAdapter(context, new Diary());
        diaryListView.setAdapter(adapter);
        diaryListView.collapseGroup(lastExpandedPosition);
        diaryListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            @Override
            public void onGroupExpand(int groupPosition) {
                if (lastExpandedPosition != -1 && groupPosition != lastExpandedPosition) {
                    diaryListView.collapseGroup(lastExpandedPosition);
                }
                lastExpandedPosition = groupPosition;
                setListViewHeight(diaryListView, groupPosition);
            }
        });
        view.findViewById(R.id.get_stats_button).setOnClickListener(this);
        hiscoreTypeSelectorLayout = view.findViewById(R.id.hiscore_type_selector);
        hiscoreTypeSelectorLayout.setOnTypeSelectedListener(this);
        selectedHiscoreType = hiscoreTypeSelectorLayout.getHiscoreType();


        initializeListeners();
        if (!defaultRsn.isEmpty()) {
            initializeUser(defaultRsn);
        }
        hideKeyboard();
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
    }

    public void handleHiscoresData(String result) {
        String[] stats = result.split("\n");
        if (stats.length < 20) {
            showToast(resources.getString(R.string.player_not_found), Toast.LENGTH_LONG);
            return;
        }
        Diary diaries = getDiaries(stats);
        adapter.updateList(diaries);
        setListViewHeight(diaryListView);
    }

    private Diary getDiaries(String[] stats) {
        DiaryType[] levelMap = new DiaryType[]{ DiaryType.EASY, DiaryType.MEDIUM, DiaryType.HARD, DiaryType.ELITE };
        Diary diary = new Diary();

        try {
            JSONObject diariesJson = new JSONObject(Utils.readFromAssets(context, "diary_reqs.json"));
            Iterator diariesIterator = diariesJson.keys();
            while (diariesIterator.hasNext()) {
                String diaryName = (String) diariesIterator.next();
                JSONObject diaryJson = diariesJson.getJSONObject(diaryName);
                ArrayList<DiaryLevel> diaryLevels = new ArrayList<>();
                for (int j = 0; j < 4; j++) {
                    Iterator skills = diaryJson.keys();
                    DiaryLevel diaryLevel = new DiaryLevel();
                    diaryLevel.diaryType = levelMap[j];
                    int i = 1;
                    while (skills.hasNext()) {
                        String skillName = (String) skills.next();
                        String[] line = stats[i].split(",");
                        int level = Integer.parseInt(line[1]);
                        int reqLvl = diaryJson.getJSONArray(skillName).getInt(j);
                        i++;
                        if (level < reqLvl) {
                            DiaryRequirement diaryRequirement = new DiaryRequirement(skillName, level, reqLvl);
                            diaryLevel.missingRequirements.add(diaryRequirement);
                        }
                        if (!skills.hasNext()) {
                            diaryLevels.add(diaryLevel);
                        }
                    }
                }
                diary.put(diaryName, diaryLevels);
            }
        }
        catch (JSONException e) {
            return null;
        }
        return diary;
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
        defaultRsn = rsn;
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
                    showToast(resources.getString(R.string.failed_to_obtain_data, "stats", error.getMessage()), Toast.LENGTH_LONG);
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
    public void cancelVolleyRequests() {
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
}
