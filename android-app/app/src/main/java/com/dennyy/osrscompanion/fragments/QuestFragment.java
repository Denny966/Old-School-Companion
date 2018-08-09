package com.dennyy.osrscompanion.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.dennyy.osrscompanion.R;
import com.dennyy.osrscompanion.layouthandlers.QuestViewHandler;
import com.dennyy.osrscompanion.models.General.Quest;

import java.util.ArrayList;

public class QuestFragment extends BaseFragment {
    private static final String WEBVIEW_STATE_KEY = "webview_state";
    private static final String SELECTED_QUEST_INDEX_KEY = "selected_quest_index";
    private static final String WAS_REQUESTING_KEY = "was_requesting";

    private View view;
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
        inflater.inflate(R.menu.menu_refresh_only, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                questViewHandler.cleanup();
                questViewHandler.webView.loadUrl(questViewHandler.webView.getUrl());
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
        else {
            return super.onBackClick();
        }
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        toolbarTitle.setText(getString(R.string.quest_guide));

        questViewHandler = new QuestViewHandler(getActivity(), view, new QuestViewHandler.QuestsLoadedCallback() {
            @Override
            public void onItemsLoaded(ArrayList<Quest> ignored) {
                loadFragment(savedInstanceState);
            }

            @Override
            public void onLoadError() { }
        });

    }

    private void loadFragment(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            questViewHandler.restoreWebView(savedInstanceState.getBundle(WEBVIEW_STATE_KEY));
            questViewHandler.selectedQuestIndex = savedInstanceState.getInt(SELECTED_QUEST_INDEX_KEY);
            questViewHandler.questSelectorSpinner.setSelection(questViewHandler.selectedQuestIndex);
            if (questViewHandler.wasRequesting()) {
                questViewHandler.webView.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Bundle bundle = new Bundle();
        questViewHandler.webView.saveState(bundle);
        outState.putBundle(WEBVIEW_STATE_KEY, bundle);
        outState.putInt(SELECTED_QUEST_INDEX_KEY, questViewHandler.selectedQuestIndex);
        outState.putBoolean(WAS_REQUESTING_KEY, questViewHandler.wasRequesting());
    }

    @Override
    public void onDetach() {
        super.onDetach();
        questViewHandler.cancelVolleyRequests();
    }
}
