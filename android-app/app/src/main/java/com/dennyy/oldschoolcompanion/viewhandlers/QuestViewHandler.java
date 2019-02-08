package com.dennyy.oldschoolcompanion.viewhandlers;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.*;
import com.dennyy.oldschoolcompanion.R;
import com.dennyy.oldschoolcompanion.adapters.QuestListAdapter;
import com.dennyy.oldschoolcompanion.adapters.QuestSourceSpinnerAdapter;
import com.dennyy.oldschoolcompanion.asynctasks.QuestAsyncTasks;
import com.dennyy.oldschoolcompanion.customviews.ObservableWebView;
import com.dennyy.oldschoolcompanion.enums.QuestSortType;
import com.dennyy.oldschoolcompanion.enums.QuestSource;
import com.dennyy.oldschoolcompanion.enums.ScrollState;
import com.dennyy.oldschoolcompanion.helpers.AdBlocker;
import com.dennyy.oldschoolcompanion.helpers.Constants;
import com.dennyy.oldschoolcompanion.helpers.Utils;
import com.dennyy.oldschoolcompanion.interfaces.ObservableScrollViewCallbacks;
import com.dennyy.oldschoolcompanion.interfaces.QuestListeners;
import com.dennyy.oldschoolcompanion.models.General.Quest;
import im.delight.android.webview.AdvancedWebView;

import java.util.ArrayList;
import java.util.Arrays;

public class QuestViewHandler extends BaseViewHandler implements AdvancedWebView.Listener, AdapterView.OnItemSelectedListener, View.OnClickListener, QuestListeners.LoadedListener, ObservableScrollViewCallbacks, QuestListeners.AdapterClickListener {

    public ObservableWebView webView;

    private QuestSource selectedQuestSource;
    private ProgressBar progressBar;
    private ArrayList<QuestSource> questSources;
    private boolean clearHistory;
    private final Handler handler = new Handler();
    private Runnable runnable;
    private QuestListeners.LoadedListener questsLoadedListener;
    private RelativeLayout questSelectorContainer;
    private ExpandableListView questListView;
    private QuestListAdapter adapter;
    private Quest currentQuest;
    private ImageButton scrollToTopButton;
    private Button sortButton;

    private final Handler navBarHandler = new Handler();
    private Runnable navBarRunnable;

    public QuestViewHandler(final Context context, View view, boolean isFloatingView, QuestListeners.LoadedListener questsLoadedListener) {
        super(context, view, isFloatingView);

        selectedQuestSource = QuestSource.fromName(PreferenceManager.getDefaultSharedPreferences(context).getString(Constants.PREF_QUEST_SOURCE, QuestSource.RSWIKI.getName()));
        webView = view.findViewById(R.id.webview);
        webView.addScrollViewCallbacks(this);
        questSelectorContainer = view.findViewById(R.id.quest_selector_container);
        progressBar = view.findViewById(R.id.progressBar);
        questListView = view.findViewById(R.id.expandable_quest_listview);
        scrollToTopButton = view.findViewById(R.id.webview_navbar_to_top);
        sortButton = view.findViewById(R.id.sort_button);
        sortButton.setOnClickListener(this);
        if (isFloatingView) {
            Button backButton = view.findViewById(R.id.navigate_back_button);
            backButton.setVisibility(View.VISIBLE);
            backButton.setOnClickListener(this);
            scrollToTopButton.setOnClickListener(this);
        }
        this.questsLoadedListener = questsLoadedListener;
        new QuestAsyncTasks.QuestLoadTask(context, this).execute();
        initWebView();
        initQuestSourceSpinner();
    }

    private void initQuestSourceSpinner() {
        Spinner questSourceSpinner = view.findViewById(R.id.quest_source_spinner);
        questSourceSpinner.setOnItemSelectedListener(this);
        questSources = new ArrayList<>(Arrays.asList(QuestSource.values()));
        questSourceSpinner.setAdapter(new QuestSourceSpinnerAdapter(context, questSources));
        questSourceSpinner.setSelection(questSources.indexOf(selectedQuestSource));
        questSourceSpinner.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                showQuestSelector();
                return false;
            }
        });
    }

    private void initWebView() {
        webView.setThirdPartyCookiesEnabled(false);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setDisplayZoomControls(false);
        webView.setMixedContentAllowed(false);
        Activity activity = null;
        if (context instanceof Activity) {
            activity = (Activity) context;
        }
        webView.setListener(activity, this);
        webView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int newProgress) {
                progressBar.setProgress(newProgress);
            }

        });
        webView.setWebViewClient(AdBlocker.getWebViewClient());
    }

    @Override
    public void onQuestsLoaded(ArrayList<Quest> loadedQuests) {
        adapter = new QuestListAdapter(context, loadedQuests, this);
        questListView.setAdapter(adapter);
        expandGroups();
        if (questsLoadedListener != null) {
            questsLoadedListener.onQuestsLoaded(null);
        }
    }

    private void expandGroups() {
        for (int i = 0; i < adapter.getGroupCount(); i++) {
            questListView.expandGroup(i);
        }
    }

    @Override
    public void onQuestsLoadError() {
        showToast(getString(R.string.unexpected_error_try_reopen), Toast.LENGTH_SHORT);
    }

    public void scrollToTop() {
        if (webView == null) return;
        webView.scrollVerticallyTo(0);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.navigate_back_button) {
            if (webView.canGoBack()) {
                webView.goBack();
                startHideQuestSelector();
            }
            else if (isWebViewVisible()) {
                hideWebView();
            }
        }
        else if (id == R.id.webview_navbar_to_top) {
            scrollToTop();
        }
        else if (id == R.id.sort_button) {
            if (adapter == null) {
                showToast(getString(R.string.unexpected_error_try_reopen), Toast.LENGTH_SHORT);
                return;
            }
            PopupMenu popupMenu = new PopupMenu(context, view);
            popupMenu.inflate(R.menu.menu_sort_quest);
            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    if (menuItem.getItemId() == R.id.action_sort_alphabetically) {
                        adapter.updateSorting(QuestSortType.NAME);
                    }
                    else if (menuItem.getItemId() == R.id.action_sort_members) {
                        adapter.updateSorting(QuestSortType.MEMBERS);
                    }
                    else if (menuItem.getItemId() == R.id.action_sort_difficulty) {
                        adapter.updateSorting(QuestSortType.DIFFICULTY);
                    }
                    else if (menuItem.getItemId() == R.id.action_sort_length) {
                        adapter.updateSorting(QuestSortType.LENGTH);
                    }
                    else if (menuItem.getItemId() == R.id.action_sort_qp) {
                        adapter.updateSorting(QuestSortType.QP);
                    }
                    else if (menuItem.getItemId() == R.id.action_sort_completion) {
                        adapter.updateSorting(QuestSortType.COMPLETION);
                    }
                    expandGroups();
                    return true;
                }
            });
            popupMenu.show();
        }
    }

    @Override
    public void onPageStarted(String url, Bitmap favicon) {
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onPageFinished(String url) {
        if (clearHistory) {
            clearHistory = false;
            webView.clearHistory();
        }
        wasRequesting = false;
        handler.removeCallbacks(runnable);
        runnable = new Runnable() {
            @Override
            public void run() {
                handlePageTimerFinished();
            }
        };
        handler.postDelayed(runnable, 1500);
    }

    private void handlePageTimerFinished() {
        progressBar.setProgress(progressBar.getMax());
        startHideQuestSelector(250);
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onPageError(int errorCode, String description, String failingUrl) {
        showToast(getString(R.string.unexpected_error, description), Toast.LENGTH_SHORT);
    }

    @Override
    public void onDownloadRequested(String url, String suggestedFilename, String mimeType, long contentLength, String contentDisposition, String userAgent) {

    }

    @Override
    public void onExternalPageRequest(String url) {
        showToast(getString(R.string.external_navigation_prohibited, "the guides"), Toast.LENGTH_SHORT);
    }

    public void restoreWebView(Bundle webViewState) {
        if (webView != null) {
            webView.restoreState(webViewState);
        }
    }

    @Override
    public boolean wasRequesting() {
        return wasRequesting;
    }

    @Override
    public void cancelRunningTasks() {
        handler.removeCallbacks(runnable);
        Utils.clearWebView(webView);
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
        if (adapterView.getId() == R.id.quest_source_spinner) {
            QuestSource questSource = questSources.get(pos);
            if (selectedQuestSource == questSource || adapter == null || adapter.isEmpty()) {
                return;
            }
            selectedQuestSource = questSource;
            if (isWebViewVisible()) {
                selectQuest();
            }
        }
    }

    @Override
    public void onQuestClick(Quest quest) {
        currentQuest = quest;
        selectQuest();
    }

    @Override
    public void onQuestDoneClick(Quest quest, boolean isCompleted) {
        new QuestAsyncTasks.InsertOrUpdateQuestCompletionTask(context, quest.name, isCompleted).execute();
        adapter.updateSorting(adapter.getQuestSortType(), true);
    }

    private void selectQuest() {
        if (currentQuest == null) {
            return;
        }
        clearHistory();
        switch (selectedQuestSource) {
            case RSWIKI:
                loadQuestUrl(currentQuest.url);
                break;
            case RUNEHQ:
                if (currentQuest.hasRuneHqUrl()) {
                    loadQuestUrl(currentQuest.runeHqUrl);
                }
                else {
                    showToast(getString(R.string.no_runehq_guide, currentQuest.name), Toast.LENGTH_LONG);
                }
                break;
        }
        wasRequesting = true;
        showQuestSelector();
        startHideQuestSelector();
    }

    private void loadQuestUrl(String url) {
        sortButton.setVisibility(View.GONE);
        if (isFloatingView) {
            scrollToTopButton.setVisibility(View.VISIBLE);
        }
        webView.setVisibility(View.VISIBLE);
        webView.loadUrl(url);
        questListView.setVisibility(View.GONE);
    }


    public void hideWebView() {
        showQuestSelector();
        sortButton.setVisibility(View.VISIBLE);
        if (isFloatingView) {
            scrollToTopButton.setVisibility(View.GONE);
        }
        webView.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        questListView.setVisibility(View.VISIBLE);
        clearHistory();
        questSelectorContainer.setVisibility(View.VISIBLE);
    }

    public boolean isWebViewVisible() {
        return webView.getVisibility() == View.VISIBLE;
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    public void clearHistory() {
        webView.clearHistory();
        clearHistory = true;
    }

    @Override
    public void onScrollChanged(int scrollY, boolean firstScroll, boolean dragging) {

    }

    @Override
    public void onDownMotionEvent() {

    }

    @Override
    public void onUpOrCancelMotionEvent(ScrollState scrollState) {
        if (scrollState == ScrollState.UP) {
            startHideQuestSelector(0);
        }

        else if (scrollState == ScrollState.DOWN) {
            showQuestSelector();
            startHideQuestSelector();
        }
        else if ((scrollState == ScrollState.STOP || scrollState == null) && webView.getCurrentScrollY() == 0) {
            showQuestSelector();
            startHideQuestSelector();
        }
    }

    private void showQuestSelector() {
        navBarHandler.removeCallbacks(navBarRunnable);
        questSelectorContainer.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2));
        questSelectorContainer.setVisibility(View.VISIBLE);
    }

    private void startHideQuestSelector() {
        startHideQuestSelector(2000);
    }

    private void startHideQuestSelector(int delay) {
        navBarHandler.removeCallbacks(navBarRunnable);
        navBarRunnable = new Runnable() {
            @Override
            public void run() {
                if (webView.getVisibility() != View.VISIBLE) return;
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) questSelectorContainer.getLayoutParams();
                int height = questSelectorContainer.getHeight() + params.bottomMargin + params.topMargin;
                questSelectorContainer.animate().translationY(-height).setInterpolator(new AccelerateInterpolator(2));
            }
        };
        navBarHandler.postDelayed(navBarRunnable, delay);
    }
}
