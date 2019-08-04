package com.dennyy.oldschoolcompanion.viewhandlers;

import android.Manifest;
import android.app.DownloadManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.PointF;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.ImageViewState;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.dennyy.oldschoolcompanion.BuildConfig;
import com.dennyy.oldschoolcompanion.R;
import com.dennyy.oldschoolcompanion.adapters.WorldmapCitiesAdapter;
import com.dennyy.oldschoolcompanion.helpers.Constants;
import com.dennyy.oldschoolcompanion.helpers.Logger;
import com.dennyy.oldschoolcompanion.interfaces.WorldmapCityClickListener;
import com.dennyy.oldschoolcompanion.models.Worldmap.City;
import me.zhanghai.android.materialprogressbar.MaterialProgressBar;

import java.io.File;

import static android.content.Context.DOWNLOAD_SERVICE;

public class WorldmapViewHandler extends BaseViewHandler implements SubsamplingScaleImageView.OnImageEventListener, WorldmapCityClickListener, View.OnClickListener, View.OnTouchListener {
    private SubsamplingScaleImageView worldmapView;
    private MaterialProgressBar progressBar;
    private TextView worldmapInfo;
    private ListView listView;
    private ImageButton navbarMenu;
    private float listViewWidth;
    private static final int minimumDpi = 50;

    public WorldmapViewHandler(Context context, View view, boolean isFloatingView) {
        super(context, view, isFloatingView);
        worldmapView = view.findViewById(R.id.worldmap_view);
        worldmapInfo = view.findViewById(R.id.worldmap_floating_view_info);
        progressBar = view.findViewById(R.id.progressBar);
        listView = view.findViewById(R.id.worldmap_listview);
        navbarMenu = view.findViewById(R.id.worldmap_navbar_menu);
        listViewWidth = context.getResources().getDimension(R.dimen.worldmap_listview_width);
        listView.setAdapter(new WorldmapCitiesAdapter(context, this));
        worldmapView.setOnImageEventListener(this);
        worldmapView.setOnTouchListener(this);
        worldmapView.setMinimumDpi(minimumDpi);
        toggleMenu();
        if (isFloatingView) {
            navbarMenu.setVisibility(View.VISIBLE);
            navbarMenu.findViewById(R.id.worldmap_navbar_menu).setOnClickListener(this);
            if (worldmapExists() && !storagePermissionDenied()) {
                loadWorldmap(null);
            }
            else {
                navbarMenu.setVisibility(View.GONE);
                worldmapInfo.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
            }
        }
    }

    public void downloadWorldmap(boolean forceDownload) {
        if (forceDownload) {
            deleteWorldmap();
        }
        try {
            Uri url = Uri.parse("https://cdn.runescape.com/assets/img/external/oldschool/web/osrs_world_map_july18_2019.PNG");
            DownloadManager.Request request = new DownloadManager.Request(url);
            request.setTitle(context.getResources().getString(R.string.downloading_worldmap));
            request.setVisibleInDownloadsUi(false);
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOCUMENTS, Constants.WORLDMAP_FILE_PATH);

            DownloadManager downloadManager = (DownloadManager) context.getSystemService(DOWNLOAD_SERVICE);
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putLong(Constants.WORLDMAP_DOWNLOAD_KEY, downloadManager.enqueue(request));
            editor.apply();

            progressBar.setVisibility(View.VISIBLE);
            showToast(getString(R.string.downloading_worldmap), Toast.LENGTH_SHORT);
        }
        catch (Exception e) {
            Logger.log(e);
            showToast(getString(R.string.download_worldmap_failed, e.getMessage()), Toast.LENGTH_LONG);
        }
    }

    public ImageViewState getWorldmapState() {
        return worldmapView.getState();
    }

    public void loadWorldmap(ImageViewState state) {
        worldmapInfo.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        if (isFloatingView) {
            navbarMenu.setVisibility(View.VISIBLE);
        }
        worldmapView.setImage(ImageSource.uri(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + Constants.WORLDMAP_FILE_PATH), state);
    }

    public boolean worldmapExists() {
        //create path
        File directoryFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), Constants.WORLDMAP_DIRECTORY_PATH);
        directoryFile.mkdirs();

        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), Constants.WORLDMAP_FILE_PATH);
        return file.exists();
    }

    private void deleteWorldmap() {
        try {
            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), Constants.WORLDMAP_FILE_PATH);
            if (file.exists()) {
                file.delete();
            }
        }
        catch (Exception e) {
            Logger.log("worldmap deletion failed", e);
        }
    }

    @Override
    public void onWorldmapCityClick(int index, City city) {
        if (worldmapView.isReady()) {
            toggleMenu();
            worldmapView.setMinimumDpi(160); // library default
            worldmapView.animateScaleAndCenter(worldmapView.getMaxScale(), new PointF(city.location.x, city.location.y)).start();
            worldmapView.setMinimumDpi(minimumDpi);
        }
    }

    public void toggleMenu() {
        float currentTranslation = listView.getTranslationX();
        listView.animate().translationX((currentTranslation < 1 ? 1 : 0) * listViewWidth).setInterpolator(new AccelerateInterpolator(2));
    }

    private void hideMenu() {
        float currentTranslation = listView.getTranslationX();
        if (currentTranslation < 1) {
            listView.animate().translationX(listViewWidth).setInterpolator(new AccelerateInterpolator(2));
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.worldmap_navbar_menu) {
            toggleMenu();
        }
    }

    @Override
    public void onReady() {

    }

    @Override
    public void onImageLoaded() {
        hideProgressBar();
    }

    @Override
    public void onPreviewLoadError(Exception e) {

    }

    @Override
    public void onImageLoadError(Exception e) {
        showToast(getString(R.string.worldmap_load_failed), Toast.LENGTH_SHORT);
        hideProgressBar();
        deleteWorldmap();
    }

    @Override
    public void onTileLoadError(Exception e) {
    }

    @Override
    public void onPreviewReleased() {

    }

    @Override
    public boolean wasRequesting() {
        return false;
    }

    @Override
    public void cancelRunningTasks() {

    }

    public void hideProgressBar() {
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        hideMenu();
        return !BuildConfig.DEBUG && gestureDetector.onTouchEvent(motionEvent);
    }

    private final GestureDetector gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            if (worldmapView.isReady()) {
                Logger.log(String.format("%s", worldmapView.viewToSourceCoord(e.getX(), e.getY())));
            }
            return true;
        }
    });

    public boolean storagePermissionDenied() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED;
    }
}