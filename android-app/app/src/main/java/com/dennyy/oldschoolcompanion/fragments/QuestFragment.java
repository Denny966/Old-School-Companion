package com.dennyy.oldschoolcompanion.fragments;

import android.os.Bundle;
import android.view.*;
import com.dennyy.oldschoolcompanion.R;
import com.dennyy.oldschoolcompanion.interfaces.QuestListeners;
import com.dennyy.oldschoolcompanion.models.General.Quest;
import com.dennyy.oldschoolcompanion.viewhandlers.QuestViewHandler;

import java.util.ArrayList;

public class QuestFragment extends BaseFragment {

    private static final String WEBVIEW_STATE_KEY = "webview_state";
    private static final String WAS_REQUESTING_KEY = "was_requesting";

    private QuestViewHandler questViewHandler;

    public QuestFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        view = inflater.inflate(R.layout.quest_layout, container, false);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_quests, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                questViewHandler.clearHistory();
                questViewHandler.webView.loadUrl(questViewHandler.webView.getUrl());
                return true;
            case R.id.action_scroll_to_top:
                questViewHandler.scrollToTop();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onBackClick() {
        if (questViewHandler.webView.canGoBack()) {
            questViewHandler.webView.goBack();
            return true;
        }
        else if (questViewHandler.isWebViewVisible()) {
            questViewHandler.hideWebView();
            return true;
        }
        else {
            return super.onBackClick();
        }
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        toolbarTitle.setText(getString(R.string.quest_guide));

        questViewHandler = new QuestViewHandler(getActivity(), view, false, new QuestListeners.LoadedListener() {
            @Override
            public void onQuestsLoaded(ArrayList<Quest> loadedQuests) {
                loadFragment(savedInstanceState);
            }

            @Override
            public void onQuestsLoadError() {
            }
        });
    }

    private void loadFragment(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            questViewHandler.restoreWebView(savedInstanceState.getBundle(WEBVIEW_STATE_KEY));
            if (questViewHandler.wasRequesting()) {
                questViewHandler.webView.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Bundle bundle = new Bundle();
        if (questViewHandler == null) return;
        questViewHandler.webView.saveState(bundle);
        outState.putBundle(WEBVIEW_STATE_KEY, bundle);
        outState.putBoolean(WAS_REQUESTING_KEY, questViewHandler.wasRequesting());
    }

    @Override
    public void onDetach() {
        super.onDetach();
        questViewHandler.cancelRunningTasks();
    }
}
