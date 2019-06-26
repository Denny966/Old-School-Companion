package com.dennyy.oldschoolcompanion.fragments;


import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.dennyy.oldschoolcompanion.R;
import com.dennyy.oldschoolcompanion.asynctasks.CustomTileTasks;
import com.dennyy.oldschoolcompanion.helpers.Utils;
import com.dennyy.oldschoolcompanion.interfaces.CustomTileListeners;
import com.dennyy.oldschoolcompanion.models.CustomTile.CustomTile;
import com.dennyy.oldschoolcompanion.models.General.SerializableTileData;
import com.dennyy.oldschoolcompanion.models.General.TilesUpdatedEvent;
import com.dennyy.oldschoolcompanion.viewhandlers.CustomTileViewHandler;
import org.greenrobot.eventbus.EventBus;

import java.util.List;

public class AddCustomTileFragment extends BaseFragment implements View.OnClickListener {
    private SerializableTileData tileData;

    public AddCustomTileFragment() {
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
        view = inflater.inflate(R.layout.add_custom_layout, container, false);
        return view;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Button button = view.findViewById(R.id.add_custom_tile_add);
        EditText urlEditText = view.findViewById(R.id.add_custom_tile_url);

        button.setOnClickListener(this);
        if (tileData.isCustomTile) {
            toolbarTitle.setText(getResources().getString(R.string.edit_custom_tile));
            ((EditText) view.findViewById(R.id.add_custom_tile_name)).setText(tileData.name);
            urlEditText.setText(tileData.url);
            button.setText(getString(R.string.update));
        }
        else {
            toolbarTitle.setText(getResources().getString(R.string.add_custom_tile));
            urlEditText.setText(getString(R.string.https));
            button.setText(getString(R.string.add));
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.add_custom_tile_add) {
            String name = ((EditText) view.findViewById(R.id.add_custom_tile_name)).getText().toString();
            String url = ((EditText) view.findViewById(R.id.add_custom_tile_url)).getText().toString();
            if (Utils.isNullOrEmpty(name) || Utils.isNullOrEmpty(url) || !Patterns.WEB_URL.matcher(url).matches()) {
                showToast(getString(R.string.custom_tile_invalid_name_or_url), Toast.LENGTH_LONG);
                return;
            }
            int sortOrder = tileData.isCustomTile ? tileData.sortOrder : Integer.MAX_VALUE;
            new CustomTileTasks.InsertOrUpdate(getActivity(), tileData.id, name, sortOrder, url, new CustomTileListeners.CustomTileListener() {
                @Override
                public void onCustomTilesLoaded(List<CustomTile> tiles) {
                    showToast(getString(tileData.isCustomTile ? R.string.update_tile_success:R.string.add_tile_success), Toast.LENGTH_LONG);
                    getActivity().onBackPressed();
                    EventBus.getDefault().post(new TilesUpdatedEvent());
                }

                @Override
                public void onCustomTilesLoadFailed() {
                    showToast(getString(R.string.add_tile_error), Toast.LENGTH_LONG);
                }

                @Override
                public void always() {

                }
            }).execute();
        }
    }
}

