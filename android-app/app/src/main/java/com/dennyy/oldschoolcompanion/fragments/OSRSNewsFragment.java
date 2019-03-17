package com.dennyy.oldschoolcompanion.fragments;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.dennyy.oldschoolcompanion.R;
import com.dennyy.oldschoolcompanion.viewhandlers.OSRSNewsViewHandler;

public class OSRSNewsFragment extends BaseFragment {

    private static final String WEBVIEW_STATE_KEY = "osrsnews_webview_state";
    private static final String WEBVIEW_VISIBLE_KEY = "webview_visible_key";

    private OSRSNewsViewHandler osrsNewsViewHandler;

    public OSRSNewsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        view = inflater.inflate(R.layout.rsnews_layout, container, false);

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
                if (osrsNewsViewHandler.isWebViewHidden() && osrsNewsViewHandler.allowRefresh()) {
                    osrsNewsViewHandler.refreshOSRSNews();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        toolbarTitle.setText(getResources().getString(R.string.rsnews));

        osrsNewsViewHandler = new OSRSNewsViewHandler(getActivity(), view, false);
        if (savedInstanceState != null) {
            osrsNewsViewHandler.restoreWebView(savedInstanceState.getBundle(WEBVIEW_STATE_KEY));
            boolean isWebViewHidden = savedInstanceState.getBoolean(WEBVIEW_VISIBLE_KEY);
            if (osrsNewsViewHandler.wasRequesting() || !isWebViewHidden) {
                osrsNewsViewHandler.toggleWebView(true);
            }
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        osrsNewsViewHandler.cancelRunningTasks();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (osrsNewsViewHandler != null) {
            Bundle bundle = new Bundle();
            osrsNewsViewHandler.webView.saveState(bundle);
            outState.putBundle(WEBVIEW_STATE_KEY, bundle);
            outState.putBoolean(WEBVIEW_VISIBLE_KEY, osrsNewsViewHandler.isWebViewHidden());
        }
    }

    @Override
    public boolean onBackClick() {
        if (!osrsNewsViewHandler.isWebViewHidden()) {
            osrsNewsViewHandler.toggleWebView(false);
            return true;
        }
        return super.onBackClick();
    }
}