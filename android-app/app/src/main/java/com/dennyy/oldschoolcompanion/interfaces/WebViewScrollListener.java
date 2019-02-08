package com.dennyy.oldschoolcompanion.interfaces;

import com.dennyy.oldschoolcompanion.customviews.ObservableWebView;

public interface WebViewScrollListener {
    void onWebViewScrollDown(ObservableWebView observableWebView, int y, int oldY);

    void onWebViewScrollUp(ObservableWebView observableWebView, int y, int oldY);
}
