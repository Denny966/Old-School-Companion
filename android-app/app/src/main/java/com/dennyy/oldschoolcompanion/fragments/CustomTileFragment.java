package com.dennyy.oldschoolcompanion.fragments;

import android.os.Bundle;
import android.view.*;
import com.dennyy.oldschoolcompanion.R;
import com.dennyy.oldschoolcompanion.models.General.SerializableTileData;
import com.dennyy.oldschoolcompanion.viewhandlers.CustomTileViewHandler;

public class CustomTileFragment extends BaseFragment {

    private static final String WEBVIEW_STATE_KEY = "rswiki_webview_state";

    private CustomTileViewHandler viewHandler;
    private SerializableTileData tileData;

    public CustomTileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tileData = (SerializableTileData) getArguments().getSerializable(CustomTileViewHandler.TILE_DATA_PARAMETER);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        view = inflater.inflate(R.layout.custom_tile_layout, container, false);

        return view;
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        toolbarTitle.setText(tileData.name);

        viewHandler = new CustomTileViewHandler(getActivity(), view, tileData.name, tileData.url, false);
        if (savedInstanceState != null) {
            viewHandler.restoreWebView(savedInstanceState.getBundle(WEBVIEW_STATE_KEY));
            if (viewHandler.wasRequesting()) {
                viewHandler.setWebViewVisibiliy(View.VISIBLE);
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_custom_tile, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                viewHandler.cleanup();
                viewHandler.reload();
                return true;

            case R.id.action_exit:
                viewHandler.cleanup();
                viewHandler.loadUrl(tileData.url);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onBackClick() {
        if (viewHandler.canGoBack()) {
            viewHandler.goBack();
            return true;
        }
        return super.onBackClick();
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (viewHandler != null) {
            Bundle bundle = new Bundle();
            viewHandler.saveState(bundle);
            outState.putBundle(WEBVIEW_STATE_KEY, bundle);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        viewHandler.cancelRunningTasks();
    }
}
