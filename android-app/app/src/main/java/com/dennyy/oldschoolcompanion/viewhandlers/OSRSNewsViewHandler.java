package com.dennyy.oldschoolcompanion.viewhandlers;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.*;
import com.android.volley.NoConnectionError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.dennyy.oldschoolcompanion.AppController;
import com.dennyy.oldschoolcompanion.R;
import com.dennyy.oldschoolcompanion.adapters.OSRSNewsAdapter;
import com.dennyy.oldschoolcompanion.asynctasks.OSRSNewsTask;
import com.dennyy.oldschoolcompanion.database.AppDb;
import com.dennyy.oldschoolcompanion.helpers.*;
import com.dennyy.oldschoolcompanion.interfaces.OSRSNewsLoadedListener;
import com.dennyy.oldschoolcompanion.models.OSRSNews.OSRSNews;
import com.dennyy.oldschoolcompanion.models.OSRSNews.OSRSNewsDTO;
import im.delight.android.webview.AdvancedWebView;

import java.util.ArrayList;

public class OSRSNewsViewHandler extends BaseViewHandler implements SwipeRefreshLayout.OnRefreshListener, AdapterView.OnItemClickListener, AdvancedWebView.Listener, View.OnClickListener {

    public AdvancedWebView webView;

    private static final String OSRS_NEWS_REQUEST_TAG = "OSRS_NEWS_REQUEST_TAG";
    private OSRSNewsAdapter listViewAdapter;
    private SwipeRefreshLayout refreshLayout;
    private long lastRefreshTimeMs;
    private ProgressBar progressBar;
    private final Handler handler = new Handler();
    private Runnable runnable;
    private LinearLayout webViewContainer;

    public OSRSNewsViewHandler(Context context, final View view, boolean isFloatingView) {
        super(context, view);
        webView = view.findViewById(R.id.webview);
        progressBar = view.findViewById(R.id.progressBar);
        webViewContainer = view.findViewById(R.id.rsnews_webview_container);
        refreshLayout = view.findViewById(R.id.rsnews_refresh_layout);
        refreshLayout.setOnRefreshListener(this);
        ListView listView = view.findViewById(R.id.rsnews_listview);
        listViewAdapter = new OSRSNewsAdapter(context, new ArrayList<OSRSNews>());
        listView.setAdapter(listViewAdapter);
        listView.setOnItemClickListener(this);
        if (isFloatingView) {
            LinearLayout navBar = view.findViewById(R.id.webview_navbar);
            navBar.setOnClickListener(this);
            navBar.setVisibility(View.VISIBLE);
        }
        loadNews();
        initWebView();
    }

    private void loadNews() {
        new OSRSNewsTask(context, new OSRSNewsLoadedListener() {
            @Override
            public void onOSRSNewsLoaded(OSRSNewsDTO osrsNewsDTO) {
                if (osrsNewsDTO != null) {
                    handleRssData(osrsNewsDTO.data);
                }
                refreshOSRSNews();
            }
        }).execute();
    }

    private void initWebView() {
        webView.setThirdPartyCookiesEnabled(false);
        webView.setMixedContentAllowed(false);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setDisplayZoomControls(false);
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

    public void refreshOSRSNews() {
        refreshLayout.setRefreshing(true);
        activateRefreshCooldown();
        Utils.getString(Constants.OSRS_NEWS_URL, OSRS_NEWS_REQUEST_TAG, new Utils.VolleyCallback() {
            @Override
            public void onSuccess(String result) {
                AppDb.getInstance(context).insertOrUpdateOSRSNewsData(result);
                handleRssData(result);
            }

            @Override
            public void onError(VolleyError error) {
                if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                    OSRSNewsDTO cachedData = AppDb.getInstance(context).getOSRSNews();
                    if (cachedData == null) {
                        showToast(resources.getString(R.string.failed_to_obtain_data, "osrs news", resources.getString(R.string.network_error)), Toast.LENGTH_LONG);
                        return;
                    }

                    showToast(resources.getString(R.string.using_cached_data, Utils.convertTime(cachedData.dateModified)), Toast.LENGTH_LONG);
                    handleRssData(cachedData.data);
                }
                else
                    showToast(resources.getString(R.string.failed_to_obtain_data, "osrs news", error.getClass().getSimpleName()), Toast.LENGTH_LONG);
            }

            @Override
            public void always() {
                refreshLayout.setRefreshing(false);
                wasRequesting = false;
            }
        });
    }

    private void handleRssData(String result) {
        OSRSNewsParser parser = new OSRSNewsParser();
        try {
            ArrayList<OSRSNews> news = parser.parse(result);
            listViewAdapter.updateList(news);
        }
        catch (Exception ex) {
            Logger.log(result, ex);
            showToast(getString(R.string.unexpected_error, "unable to reach runescape.com"), Toast.LENGTH_SHORT);
        }
    }

    public void toggleWebView(boolean visible) {
        webViewContainer.setVisibility(visible ? View.VISIBLE : View.GONE);
        refreshLayout.setVisibility(visible ? View.GONE : View.VISIBLE);
        refreshLayout.setEnabled(!visible);
        if (!visible) {
            webView.loadUrl("about:blank");
        }
    }

    public boolean isWebViewHidden() {
        return webViewContainer.getVisibility() != View.VISIBLE;
    }

    public void restoreWebView(Bundle webViewState) {
        if (webView != null) {
            webView.restoreState(webViewState);
        }
    }

    @Override
    public void onRefresh() {
        if (allowRefresh()) {
            refreshOSRSNews();
        }
    }

    public boolean allowRefresh() {
        long refreshPeriod = System.currentTimeMillis() - lastRefreshTimeMs;
        if (refreshPeriod < Constants.REFRESH_COOLDOWN_MS) {
            double timeLeft = (Constants.REFRESH_COOLDOWN_MS - refreshPeriod) / 1000;
            showToast(getString(R.string.wait_before_refresh, (int) Math.ceil(timeLeft + 0.5)), Toast.LENGTH_SHORT);
            refreshLayout.setRefreshing(false);
            return false;
        }
        return true;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int pos, long id) {
        OSRSNews osrsNews = listViewAdapter.getItem(pos);
        webView.loadUrl(osrsNews.url);
        wasRequesting = true;
        toggleWebView(true);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.webview_navbar) {
            toggleWebView(false);
        }
    }

    private void activateRefreshCooldown() {
        lastRefreshTimeMs = System.currentTimeMillis();
    }

    @Override
    public void onPageStarted(String url, Bitmap favicon) {
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onPageFinished(String url) {
        handler.removeCallbacks(runnable);
        runnable = new Runnable() {
            @Override
            public void run() {
                handlePageTimerFinished();
            }
        };
        handler.postDelayed(runnable, 1500);
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
        showToast(getString(R.string.external_navigation_prohibited, "runescape"), Toast.LENGTH_SHORT);
    }

    private void handlePageTimerFinished() {
        wasRequesting = false;
        progressBar.setProgress(progressBar.getMax());
        webView.setVisibility(View.VISIBLE);

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                progressBar.setVisibility(View.GONE);
            }
        }, 250);
    }

    @Override
    public void cancelRunningTasks() {
        AppController.getInstance().cancelPendingRequests(OSRS_NEWS_REQUEST_TAG);
        handler.removeCallbacks(runnable);
        Utils.clearWebView(webView);
    }

    @Override
    public boolean wasRequesting() {
        return wasRequesting;
    }
}