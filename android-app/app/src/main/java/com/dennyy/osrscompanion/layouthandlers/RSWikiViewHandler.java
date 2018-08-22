package com.dennyy.osrscompanion.layouthandlers;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.dennyy.osrscompanion.R;
import com.dennyy.osrscompanion.helpers.Utils;

import im.delight.android.webview.AdvancedWebView;

public class RSWikiViewHandler extends BaseViewHandler implements AdvancedWebView.Listener, View.OnClickListener {

    public AdvancedWebView webView;

    private ProgressBar progressBar;
    private boolean clearHistory;
    private CountDownTimer pageFinishedTimer;
    private String currentUrl;
    private TextView navBarTitle;
    private ImageButton navBarLeft;
    private ImageButton navBarRight;

    public RSWikiViewHandler(final Context context, View view, boolean isFloatingView) {
        super(context, view);

        currentUrl = "https://oldschoolrunescape.wikia.com/wiki/Old_School_RuneScape_Wiki";
        webView = view.findViewById(R.id.webview);
        progressBar = view.findViewById(R.id.progressBar);
        navBarTitle = view.findViewById(R.id.webview_navbar_title);
        navBarLeft = view.findViewById(R.id.webview_navbar_left);
        navBarRight = view.findViewById(R.id.webview_navbar_right);
        if (isFloatingView) {
            navBarLeft.setOnClickListener(this);
            navBarRight.setOnClickListener(this);
            view.findViewById(R.id.webview_navbar).setVisibility(View.VISIBLE);
        }
        initWebView();
    }


    public void initWebView() {
        webView.addPermittedHostname("oldschoolrunescape.wikia.com");
        webView.setThirdPartyCookiesEnabled(false);
        webView.setMixedContentAllowed(false);
        webView.loadUrl(currentUrl);

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
    }


    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.webview_navbar_left:
                if (webView.canGoBack()) {
                    webView.goBack();
                }
                break;
            case R.id.webview_navbar_right:
                if (webView.canGoForward()) {
                    webView.goForward();
                }
                break;
        }
    }

    @Override
    public void onPageStarted(String url, Bitmap favicon) {
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onPageFinished(String url) {
        navBarTitle.setText(Utils.isNullOrEmpty(webView.getTitle()) ? getString(R.string.osrs_wiki) : webView.getTitle());
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
        hideElementsByClass("fandom-app-smart-banner", "wds-global-footer", "edit-section");
        webView.loadUrl("javascript:(function() { document.getElementsByClassName('wds-global-navigation')[0].style.position='initial'; })()");

        wasRequesting = false;
        progressBar.setProgress(progressBar.getMax());
        webView.setVisibility(View.VISIBLE);
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
            webView.loadUrl("javascript:(function() { document.getElementsByClassName('" + className + "')[0].remove(); })()");
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
        if (webView != null) {
            if (pageFinishedTimer != null) {
                pageFinishedTimer.cancel();
            }
            webView.clearHistory();
            webView.clearCache(true);
            webView.loadUrl("about:blank");
            webView.onPause();
            webView.removeAllViews();
            webView.destroyDrawingCache();
            webView.pauseTimers();
            webView.destroy();
            webView = null;
        }
    }

    public void cleanup() {
        clearHistory = true;
    }
}
