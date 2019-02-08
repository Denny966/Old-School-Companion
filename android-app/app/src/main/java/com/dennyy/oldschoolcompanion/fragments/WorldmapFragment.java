package com.dennyy.oldschoolcompanion.fragments;

import android.Manifest;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.davemorrissey.labs.subscaleview.ImageViewState;
import com.dennyy.oldschoolcompanion.R;
import com.dennyy.oldschoolcompanion.helpers.Constants;
import com.dennyy.oldschoolcompanion.models.Worldmap.WorldmapDownloadedEvent;
import com.dennyy.oldschoolcompanion.viewhandlers.WorldmapViewHandler;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import static android.content.Context.DOWNLOAD_SERVICE;

public class WorldmapFragment extends BaseFragment {

    private static final String WORLDMAP_STATE_KEY = "WORLDMAP_STATE_KEY";

    private WorldmapViewHandler worldmapViewHandler;

    public WorldmapFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        view = inflater.inflate(R.layout.worldmap_layout, container, false);

        return view;
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        toolbarTitle.setText(getString(R.string.worldmap));

        worldmapViewHandler = new WorldmapViewHandler(getActivity(), view, false);
        if (worldmapViewHandler.storagePermissionDenied() && !worldmapViewHandler.worldmapExists()) {
            requestStoragePermission();
            return;
        }
        if (!worldmapViewHandler.worldmapExists()) {
            requestDownload(false);
            return;
        }
        ImageViewState state = savedInstanceState != null ? (ImageViewState) savedInstanceState.getSerializable(WORLDMAP_STATE_KEY) : null;
        worldmapViewHandler.loadWorldmap(state);
    }


    private void requestStoragePermission() {
        final Intent storageIntent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getActivity().getPackageName()));
        String title = getResources().getString(R.string.storage_permission_required);
        showInfoDialog(title, getResources().getString(R.string.storage_dialog_info), getResources().getString(R.string.go_to_settings), false, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                try {
                    startActivityForResult(storageIntent, Constants.CODE_STORAGE_PERMISSION);
                }
                catch (ActivityNotFoundException ignored) {
                    startActivity(new Intent(Settings.ACTION_SETTINGS));
                }
            }
        }, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (worldmapViewHandler != null) {
                    worldmapViewHandler.hideProgressBar();
                }
                showToast(getString(R.string.storage_permission_not_granted), Toast.LENGTH_SHORT);
            }
        });
    }

    private void requestDownload(final boolean forceDownload) {
        if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            showToast(getString(R.string.unable_to_write_to_storage), Toast.LENGTH_SHORT);
            return;
        }
        if (worldmapViewHandler.storagePermissionDenied()) {
            requestStoragePermission();
            return;
        }
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        final DownloadManager downloadManager = (DownloadManager) getActivity().getSystemService(DOWNLOAD_SERVICE);
        DownloadManager.Query query = new DownloadManager.Query();
        final long worldmapDownloadId = prefs.getLong(Constants.WORLDMAP_DOWNLOAD_KEY, -1);
        query.setFilterById(worldmapDownloadId);
        Cursor cursor = downloadManager.query(query);
        if (cursor != null) {
            if (cursor.moveToFirst() && cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_RUNNING) {
                cursor.close();
                showToast(getString(R.string.download_in_progress), Toast.LENGTH_SHORT);
                return;
            }
            cursor.close();
        }
        String title = getString(R.string.download_worldmap);
        String description = getString(R.string.download_worldmap_info);
        showInfoDialog(title, description, getResources().getString(R.string.yes), false, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (worldmapDownloadId != -1) {
                    downloadManager.remove(worldmapDownloadId);
                }
                worldmapViewHandler.downloadWorldmap(forceDownload);
            }
        }, null);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.CODE_STORAGE_PERMISSION) {
            boolean storagePermissionDenied = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED;

            if (storagePermissionDenied) {
                showToast(getResources().getString(R.string.storage_permission_not_granted), Toast.LENGTH_SHORT);
            }
            else {
                showToast(getResources().getString(R.string.storage_permission_granted), Toast.LENGTH_SHORT);
                requestDownload(false);
            }
        }
        else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Subscribe
    public void onWorldmapDownloaded(WorldmapDownloadedEvent event) {
        if (worldmapViewHandler != null) {
            worldmapViewHandler.loadWorldmap(null);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_worldmap, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_download_worldmap:
                if (worldmapViewHandler != null) {
                    requestDownload(true);
                }
                return true;

            case R.id.action_show_worldmap_cities:
                if (worldmapViewHandler != null) {
                    worldmapViewHandler.toggleMenu();
                }
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onBackClick() {
        return super.onBackClick();
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        worldmapViewHandler.cancelRunningTasks();
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        ImageViewState state = worldmapViewHandler.getWorldmapState();
        outState.putSerializable(WORLDMAP_STATE_KEY, state);
    }
}