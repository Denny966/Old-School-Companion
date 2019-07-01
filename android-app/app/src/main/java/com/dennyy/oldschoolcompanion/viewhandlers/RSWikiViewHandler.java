package com.dennyy.oldschoolcompanion.viewhandlers;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.*;
import com.dennyy.oldschoolcompanion.R;
import com.dennyy.oldschoolcompanion.customviews.ObservableWebView;
import com.dennyy.oldschoolcompanion.enums.ScrollState;
import com.dennyy.oldschoolcompanion.helpers.AdBlocker;
import com.dennyy.oldschoolcompanion.helpers.Utils;
import com.dennyy.oldschoolcompanion.interfaces.ObservableScrollViewCallbacks;
import im.delight.android.webview.AdvancedWebView;

public class RSWikiViewHandler extends BaseViewHandler implements AdvancedWebView.Listener, View.OnClickListener, ObservableScrollViewCallbacks {

    public ObservableWebView webView;

    private RelativeLayout navBarContainer;
    private ProgressBar progressBar;
    private boolean clearHistory;
    private TextView navBarTitle;
    private final Handler handler = new Handler();
    private Runnable runnable;

    private final Handler navBarHandler = new Handler();
    private Runnable navBarRunnable;

    public RSWikiViewHandler(final Context context, View view, boolean isFloatingView) {
        super(context, view, isFloatingView);

        webView = view.findViewById(R.id.webview);
        progressBar = view.findViewById(R.id.progressBar);
        navBarTitle = view.findViewById(R.id.webview_navbar_title);
        LinearLayout navBar = view.findViewById(R.id.webview_navbar);
        navBarContainer = view.findViewById(R.id.navbar_container);
        ImageButton navBarLeft = view.findViewById(R.id.webview_navbar_left);
        ImageButton navBarRight = view.findViewById(R.id.webview_navbar_right);
        if (isFloatingView) {
            webView.addScrollViewCallbacks(this);
            navBar.findViewById(R.id.webview_navbar_title).setOnClickListener(this);
            navBar.findViewById(R.id.webview_navbar_to_top).setOnClickListener(this);
            navBarLeft.setOnClickListener(this);
            navBarRight.setOnClickListener(this);
            navBarContainer.setVisibility(View.VISIBLE);
        }
        initWebView();
    }


    @SuppressLint("ClickableViewAccessibility")
    private void initWebView() {
        webView.setThirdPartyCookiesEnabled(false);
        webView.setMixedContentAllowed(false);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setDisplayZoomControls(false);
        webView.loadUrl("https://oldschool.runescape.wiki");

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
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.webview_navbar_left:
                if (webView.canGoBack()) {
                    webView.goBack();
                    startHideNavBar();
                }
                break;
            case R.id.webview_navbar_right:
                if (webView.canGoForward()) {
                    webView.goForward();
                    startHideNavBar();
                }
                break;
            case R.id.webview_navbar_title:
            case R.id.webview_navbar_to_top:
                scrollToTop();
                break;
        }
    }

    public void scrollToTop() {
        if (webView == null) return;
        webView.scrollVerticallyTo(0);
    }

    @Override
    public void onPageStarted(String url, Bitmap favicon) {
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onPageFinished(String url) {
        navBarTitle.setText(Utils.isNullOrEmpty(webView.getTitle()) ? getString(R.string.osrs_wiki) : webView.getTitle());
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
        wasRequesting = false;
        progressBar.setProgress(progressBar.getMax());
        webView.setVisibility(View.VISIBLE);
        if (clearHistory) {
            clearHistory = false;
            webView.clearHistory();
        }
        startHideNavBar(250);
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
        showToast(getString(R.string.external_navigation_prohibited, "the wiki"), Toast.LENGTH_SHORT);
    }

    public void restoreWebView(Bundle webViewState) {
        webView.restoreState(webViewState);
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

    public void cleanup() {
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
            startHideNavBar(0);
        }
        else if (scrollState == ScrollState.DOWN) {
            showNavBar();
            startHideNavBar();
        }
        else if ((scrollState == ScrollState.STOP || scrollState == null) && webView.getCurrentScrollY() == 0) {
            showNavBar();
            startHideNavBar();
        }
    }

    private void showNavBar() {
        navBarHandler.removeCallbacks(navBarRunnable);
        navBarContainer.setVisibility(View.VISIBLE);
        navBarContainer.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2));
    }

    private void startHideNavBar() {
        startHideNavBar(2000);
    }

    private void startHideNavBar(int delay) {
        navBarHandler.removeCallbacks(navBarRunnable);
        navBarRunnable = new Runnable() {
            @Override
            public void run() {
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) navBarContainer.getLayoutParams();
                int height = navBarContainer.getHeight() + params.bottomMargin + params.topMargin;
                navBarContainer.animate().translationY(-height).setInterpolator(new AccelerateInterpolator(2));
            }
        };
        navBarHandler.postDelayed(navBarRunnable, delay);
    }
}