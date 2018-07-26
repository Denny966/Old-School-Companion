package com.dennyy.osrscompanion.layouthandlers;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.dennyy.osrscompanion.R;
import com.dennyy.osrscompanion.adapters.NothingSelectedSpinnerAdapter;
import com.dennyy.osrscompanion.adapters.QuestSelectorSpinnerAdapter;
import com.dennyy.osrscompanion.helpers.Utils;
import com.dennyy.osrscompanion.models.General.Quest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import im.delight.android.webview.AdvancedWebView;

public class QuestViewHandler extends BaseViewHandler implements AdvancedWebView.Listener, AdapterView.OnItemSelectedListener, View.OnTouchListener, View.OnClickListener {

    public AdvancedWebView webView;
    public int selectedQuestIndex;
    public Spinner questSelectorSpinner;

    private ProgressBar progressBar;
    private ArrayList<Quest> quests;
    private QuestSelectorSpinnerAdapter questSelectorSpinnerAdapter;
    private boolean canInteract;
    private boolean clearHistory;
    private CountDownTimer pageFinishedTimer;
    private String currentUrl;

    public QuestViewHandler(final Context context, View view, QuestsLoadedCallback questsLoadedCallback) {
        super(context, view);

        webView = view.findViewById(R.id.webview);
        progressBar = view.findViewById(R.id.progressBar);
        view.findViewById(R.id.navigate_back_button).setOnClickListener(this);
        initQuests(questsLoadedCallback);
        initWebView();
    }


    public void initWebView() {
        webView.addPermittedHostname("oldschoolrunescape.wikia.com");
        webView.setThirdPartyCookiesEnabled(false);
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
        webView.setOnTouchListener(this);
    }

    private void initQuests(final QuestsLoadedCallback questsLoadedCallback) {
        new LoadQuests(context, new QuestsLoadedCallback() {
            @Override
            public void onItemsLoaded(ArrayList<Quest> loadedQuests) {
                quests = new ArrayList<>(loadedQuests);
                questSelectorSpinnerAdapter = new QuestSelectorSpinnerAdapter(context, quests);
                questSelectorSpinner.setAdapter(new NothingSelectedSpinnerAdapter(questSelectorSpinnerAdapter, getString(R.string.select_a_quest), context));
                if (questsLoadedCallback != null) {
                    questsLoadedCallback.onItemsLoaded(null);
                }
            }

            @Override
            public void onLoadError() {
                showToast(getString(R.string.failed_to_load_quests), Toast.LENGTH_SHORT);
            }
        }).execute();

        questSelectorSpinner = view.findViewById(R.id.quest_selector_spinner);
        questSelectorSpinner.setOnItemSelectedListener(this);
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        // true = not able to touch
        return view.getId() == R.id.webview && !canInteract;
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.navigate_back_button:
                if (webView.canGoBack()) {
                    webView.goBack();
                }
                break;
        }
    }

    private static class LoadQuests extends AsyncTask<String, Void, ArrayList<Quest>> {
        private WeakReference<Context> context;
        private QuestsLoadedCallback questsLoadedCallback;
        private ArrayList<Quest> quests = new ArrayList<>();

        private LoadQuests(Context context, QuestsLoadedCallback questsLoadedCallback) {
            this.context = new WeakReference<>(context);
            this.questsLoadedCallback = questsLoadedCallback;
        }

        @Override
        protected ArrayList<Quest> doInBackground(String... params) {
            try {
                JSONArray array = new JSONArray(Utils.readFromAssets(context.get(), "quests.json"));
                for (int i = 0; i < array.length(); i++) {
                    JSONObject obj = array.getJSONObject(i);
                    Quest quest = new Quest(obj.getString("name"), obj.getString("url"));
                    quests.add(quest);
                }
                Collections.sort(quests, new Comparator<Quest>() {
                    @Override
                    public int compare(Quest quest1, Quest quest2) {
                        return quest1.name.compareTo(quest2.name);
                    }
                });
            }
            catch (JSONException ignored) {

            }
            return quests;
        }

        @Override
        protected void onPostExecute(ArrayList<Quest> quests) {
            if (quests.size() > 0) {
                questsLoadedCallback.onItemsLoaded(quests);
            }
            else {
                questsLoadedCallback.onLoadError();
            }
        }
    }

    public interface QuestsLoadedCallback {
        void onItemsLoaded(ArrayList<Quest> loadedQuests);

        void onLoadError();
    }

    @Override
    public void onPageStarted(String url, Bitmap favicon) {
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onPageFinished(String url) {
        if (pageFinishedTimer != null) {
            pageFinishedTimer.cancel();
        }
        pageFinishedTimer = new CountDownTimer(1500, 1500) {
            @Override
            public void onTick(long millisUntilFinished) { }

            @Override
            public void onFinish() {
                handlePageTimerFinished();
            }
        }.start();
    }

    private void handlePageTimerFinished() {
        hideElementsByClass("site-head-container", "fandom-app-smart-banner", "site-head-wrapper", "wds-global-footer", "edit-section");
        wasRequesting = false;
        progressBar.setProgress(progressBar.getMax());
        webView.setVisibility(View.VISIBLE);
        canInteract = true;
        if (clearHistory) {
            clearHistory = false;
            webView.clearHistory();
        }

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                progressBar.setVisibility(View.GONE);
            }
        }, 250);
    }

    private void hideElementsByClass(String... classNames) {
        for (String className : classNames) {
            webView.loadUrl("javascript:(function() { document.getElementsByClassName('" + className + "')[0].style.display='none'; })()");
        }
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
        showToast(getString(R.string.external_navigation_prohibited), Toast.LENGTH_SHORT);
    }

    public void restoreWebView(Bundle webViewState) {
        webView.restoreState(webViewState);
    }

    @Override
    public boolean wasRequesting() {
        return wasRequesting;
    }

    @Override
    public void cancelVolleyRequests() {

    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
        if (pos < 1) {
            return;
        }
        webView.setVisibility(View.VISIBLE);
        selectedQuestIndex = pos;
        Quest quest = quests.get(selectedQuestIndex - 1);
        cleanup();
        webView.loadUrl(quest.url);
        canInteract = false;
        wasRequesting = true;
    }

    public void cleanup() {
        clearHistory = true;
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}
