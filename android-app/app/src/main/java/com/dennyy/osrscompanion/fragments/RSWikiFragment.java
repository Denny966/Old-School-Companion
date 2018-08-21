package com.dennyy.osrscompanion.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.dennyy.osrscompanion.R;
import com.dennyy.osrscompanion.layouthandlers.RSWikiViewHandler;

public class RSWikiFragment extends BaseFragment {
    private static final String WEBVIEW_STATE_KEY = "rswiki_webview_state";

    private View view;
    private RSWikiViewHandler rsWikiViewHandler;

    public RSWikiFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        view = inflater.inflate(R.layout.rswiki_layout, container, false);

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
                rsWikiViewHandler.cleanup();
                rsWikiViewHandler.webView.loadUrl(rsWikiViewHandler.webView.getUrl());
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onBackClick() {
        if (rsWikiViewHandler.webView.canGoBack()) {
            rsWikiViewHandler.webView.goBack();
            return true;
        }
        else {
            return super.onBackClick();
        }
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        toolbarTitle.setText(getString(R.string.osrs_wiki));

        rsWikiViewHandler = new RSWikiViewHandler(getActivity(), view, false);
        if (savedInstanceState != null) {
            rsWikiViewHandler.restoreWebView(savedInstanceState.getBundle(WEBVIEW_STATE_KEY));
            if (rsWikiViewHandler.wasRequesting()) {
                rsWikiViewHandler.webView.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Bundle bundle = new Bundle();
        rsWikiViewHandler.webView.saveState(bundle);
        outState.putBundle(WEBVIEW_STATE_KEY, bundle);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        rsWikiViewHandler.cancelVolleyRequests();
    }
}
