package com.dennyy.oldschoolcompanion.viewhandlers;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.*;
import com.dennyy.oldschoolcompanion.AppController;
import com.dennyy.oldschoolcompanion.R;
import com.dennyy.oldschoolcompanion.adapters.ImageSliderAdapter;
import com.dennyy.oldschoolcompanion.adapters.TreasureTrailMapAdapter;
import com.dennyy.oldschoolcompanion.adapters.TreasureTrailSearchAdapter;
import com.dennyy.oldschoolcompanion.customviews.ClearableAutoCompleteTextView;
import com.dennyy.oldschoolcompanion.enums.TreasureTrailType;
import com.dennyy.oldschoolcompanion.helpers.Constants;
import com.dennyy.oldschoolcompanion.helpers.Logger;
import com.dennyy.oldschoolcompanion.helpers.Utils;
import com.dennyy.oldschoolcompanion.interfaces.AdapterImageClickListener;
import com.dennyy.oldschoolcompanion.interfaces.TreasureTrailsLoadedListener;
import com.dennyy.oldschoolcompanion.models.TreasureTrails.TreasureTrail;
import com.dennyy.oldschoolcompanion.models.TreasureTrails.TreasureTrailMap;
import com.dennyy.oldschoolcompanion.models.TreasureTrails.TreasureTrails;
import com.viewpagerindicator.CirclePageIndicator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class TreasureTrailViewHandler extends BaseViewHandler implements View.OnClickListener, AdapterImageClickListener, TreasureTrailsLoadedListener {
    public TreasureTrail treasureTrail;

    private static final String TT_REQUEST_TAG = "TT_REQUEST_TAG";

    private TreasureTrailSearchAdapter adapter;
    private int selectedAdapterIndex = -1;
    private TreasureTrailMapAdapter treasureTrailMapAdapter;
    private AutoCompleteTextView autoCompleteTextView;
    private TreasureTrails allItems = new TreasureTrails();
    private ViewPager viewPager;
    private View dimView;
    private ImageView expandedImageView;
    private TreasureTrailsLoadedListener treasureTrailsLoadedListener;
    private final HashMap<Integer, Integer> containers = new HashMap<>();
    private int activeIcon;

    public TreasureTrailViewHandler(Context context, View view, boolean isFloatingView, TreasureTrailsLoadedListener treasureTrailsLoadedListener) {
        super(context, view, isFloatingView);
        this.treasureTrailsLoadedListener = treasureTrailsLoadedListener;
        if (isFloatingView) {
            loadFloatingViewNavBar();
        }
        else {
            containers.put(R.id.action_tt_main, R.id.tt_data_layout);
            containers.put(R.id.action_tt_maps, R.id.tt_maps_listview);
            containers.put(R.id.action_tt_puzzles, R.id.tt_puzzle_container);
        }
        for (int id : new int[]{ R.id.puzzle_castle, R.id.puzzle_tree, R.id.puzzle_troll, R.id.puzzle_cerberus, R.id.puzzle_gnome, R.id.puzzle_zulrah, R.id.puzzle_tob }) {
            view.findViewById(id).setOnClickListener(this);
        }
        new LoadItems(context, this).execute();
    }

    private void loadFloatingViewNavBar() {
        containers.put(R.id.tt_navbar_main, R.id.tt_data_layout);
        containers.put(R.id.tt_navbar_maps, R.id.tt_maps_listview);
        containers.put(R.id.tt_navbar_puzzles, R.id.tt_puzzle_container);
        RelativeLayout ttNavBar = view.findViewById(R.id.tt_navbar);
        setNavbarIconActive(R.id.tt_navbar_main, false);
        ttNavBar.setVisibility(View.VISIBLE);
        for (int id : containers.keySet()) {
            ttNavBar.findViewById(id).setOnClickListener(this);
        }
    }

    @Override
    public void onTreasureTrailsLoaded(TreasureTrails treasureTrails) {
        allItems = treasureTrails;
        updateView();
        if (treasureTrailsLoadedListener != null) {
            treasureTrailsLoadedListener.onTreasureTrailsLoaded(null);
        }
    }

    @Override
    public void onTreasureTrailsLoadError() {
        showToast(resources.getString(R.string.exception_occurred, "exception", "loading items from file"), Toast.LENGTH_LONG);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void updateView() {
        ListView mapsListView = view.findViewById(R.id.tt_maps_listview);
        expandedImageView = view.findViewById(R.id.expanded_image);
        dimView = view.findViewById(R.id.dim_img_view);
        autoCompleteTextView = ((ClearableAutoCompleteTextView) view.findViewById(R.id.clue_search_input)).getAutoCompleteTextView();
        if (treasureTrail != null)
            autoCompleteTextView.setText(treasureTrail.text);

        if (adapter == null) {
            adapter = new TreasureTrailSearchAdapter(context, allItems.treasureTrails);
        }
        if (treasureTrailMapAdapter == null) {
            treasureTrailMapAdapter = new TreasureTrailMapAdapter(context, allItems.treasureTrailMaps, this);
        }
        mapsListView.setAdapter(treasureTrailMapAdapter);

        autoCompleteTextView.setAdapter(adapter);
        autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Utils.hideKeyboard(context, autoCompleteTextView);
                selectedAdapterIndex = i;
                updateItem();
            }
        });

        autoCompleteTextView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                return false;
            }
        });
        autoCompleteTextView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                autoCompleteTextView.setThreshold(3);
                return false;
            }
        });
        dimView.setOnClickListener(this);
        expandedImageView.setOnClickListener(this);
        viewPager = view.findViewById(R.id.viewPager);
    }

    private void updateItem() {
        treasureTrail = adapter.getItem(selectedAdapterIndex);
        reloadData();
    }

    public void reloadData() {
        if (treasureTrail == null) {
            return;
        }
        if (treasureTrail.type == null) {
            Logger.log(new NullPointerException("Treasure trail type not found for clue " + treasureTrail.text));
            return;
        }
        ((TextView) view.findViewById(R.id.treasure_trail_clue)).setText(treasureTrail.text);
        ((TextView) view.findViewById(R.id.treasure_trail_type)).setText(treasureTrail.type.getValue());

        TextView answerTextView = view.findViewById(R.id.treasure_trail_answer);
        LinearLayout answerLinearLayout = view.findViewById(R.id.tt_answer_layout);
        if (treasureTrail.answer == null) {
            answerLinearLayout.setVisibility(View.GONE);
        }
        else {
            answerTextView.setText(treasureTrail.answer);
            answerLinearLayout.setVisibility(View.VISIBLE);
        }

        TextView npcTextView = view.findViewById(R.id.treasure_trail_npc);
        LinearLayout npcLinearLayout = view.findViewById(R.id.tt_npc_layout);
        if (treasureTrail.npc == null) {
            npcLinearLayout.setVisibility(View.GONE);
        }
        else {
            npcTextView.setText(treasureTrail.npc);
            npcLinearLayout.setVisibility(View.VISIBLE);

        }

        TextView locationTextView = view.findViewById(R.id.treasure_trail_location);
        LinearLayout locationLinearLayout = view.findViewById(R.id.tt_location_layout);
        if (treasureTrail.location == null) {
            locationLinearLayout.setVisibility(View.GONE);
        }
        else {
            locationTextView.setText(treasureTrail.location);
            locationLinearLayout.setVisibility(View.VISIBLE);
        }

        if (treasureTrail.type == TreasureTrailType.COORDINATES) {
            ((TextView) view.findViewById(R.id.treasure_trail_clue)).setText(treasureTrail.getCoordinatesFormatted());
            loadTreasureTrailImages();
            view.findViewById(R.id.tt_images_layout).setVisibility(View.VISIBLE);
        }
        else {
            view.findViewById(R.id.tt_images_layout).setVisibility(View.GONE);
        }

        view.findViewById(R.id.treasure_trail_data).setVisibility(View.VISIBLE);
    }

    private void loadTreasureTrailImages() {
        ArrayList<String> imageUrls = new ArrayList<>();
        imageUrls.add(Constants.CLUE_MAP_URL(treasureTrail.getCoordinatesFormattedForUrl()));
        imageUrls.add(Constants.CLUE_LOC_URL(treasureTrail.getCoordinatesFormattedForUrl()));
        viewPager.setAdapter(new ImageSliderAdapter(AppController.getInstance().getApplicationContext(), imageUrls));
        CirclePageIndicator indicator = view.findViewById(R.id.indicator);
        indicator.setViewPager(viewPager);
        indicator.setRadius(Utils.convertDpToPixel(5, AppController.getInstance().getApplicationContext()));
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.tt_navbar_main:
            case R.id.tt_navbar_maps:
            case R.id.tt_navbar_puzzles:
                setNavbarIconActive(id, false);
                break;
            case R.id.expanded_image:
            case R.id.dim_img_view:
                hideExpandedImageView();
                break;
            case R.id.puzzle_castle:
            case R.id.puzzle_tree:
            case R.id.puzzle_troll:
            case R.id.puzzle_cerberus:
            case R.id.puzzle_gnome:
            case R.id.puzzle_zulrah:
            case R.id.puzzle_tob:
                onClickImage(0, view);
                break;
        }
    }

    public boolean expandedImageViewVisible() {
        return expandedImageView != null && expandedImageView.getVisibility() == View.VISIBLE;
    }

    public void hideExpandedImageView() {
        expandedImageView.setVisibility(View.GONE);
        dimView.setVisibility(View.GONE);
    }

    public void setNavbarIconActive(int iconId, boolean containerOnly) {
        if (iconId == activeIcon)
            return;
        activeIcon = iconId;
        for (Map.Entry<Integer, Integer> entry : containers.entrySet()) {
            int key = entry.getKey();
            int value = entry.getValue();
            if (!containerOnly) {
                view.findViewById(key).setAlpha(0.4f);
            }
            view.findViewById(value).setVisibility(View.GONE);
        }
        if (!containerOnly) {
            view.findViewById(iconId).setAlpha(1.0f);
        }
        view.findViewById(containers.get(iconId)).setVisibility(View.VISIBLE);
    }

    @Override
    public void onClickImage(int index, View view) {
        if (view instanceof ImageView) {
            dimView.setVisibility(View.VISIBLE);
            dimView.bringToFront();
            expandedImageView.setVisibility(View.VISIBLE);
            expandedImageView.setImageDrawable(((ImageView) view).getDrawable());
            expandedImageView.bringToFront();
        }
    }

    private static class LoadItems extends AsyncTask<String, Void, TreasureTrails> {
        private WeakReference<Context> context;
        private TreasureTrailsLoadedListener treasureTrailsLoadedListener;

        private LoadItems(Context context, TreasureTrailsLoadedListener treasureTrailsLoadedListener) {
            this.context = new WeakReference<>(context);
            this.treasureTrailsLoadedListener = treasureTrailsLoadedListener;
        }

        @Override
        protected TreasureTrails doInBackground(String... params) {
            TreasureTrails treasureTrails = new TreasureTrails();
            try {
                String clues = Utils.readFromAssets(context.get(), "clues.json");
                String maps = Utils.readFromAssets(context.get(), "treasure_maps.json");
                JSONObject cluesJsonObject = new JSONObject(clues);
                Iterator cluesIterator = cluesJsonObject.keys();
                while (cluesIterator.hasNext()) {
                    String id = (String) cluesIterator.next();
                    JSONArray jsonArray = cluesJsonObject.getJSONArray(id);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        TreasureTrail treasureTrail = new TreasureTrail();

                        treasureTrail.text = jsonObject.getString("text");
                        treasureTrail.type = TreasureTrailType.fromString(jsonObject.getString("type"));
                        treasureTrail.answer = jsonObject.has("answer") ? jsonObject.getString("answer") : null;
                        treasureTrail.npc = jsonObject.has("npc") ? jsonObject.getString("npc") : null;
                        treasureTrail.location = jsonObject.has("location") ? jsonObject.getString("location") : null;
                        treasureTrails.treasureTrails.add(treasureTrail);
                    }
                }
                JSONArray mapsJsonArray = new JSONArray(maps);
                for (int i = 0; i < mapsJsonArray.length(); i++) {
                    JSONObject jsonObject = mapsJsonArray.getJSONObject(i);
                    TreasureTrailMap treasureTrailMap = new TreasureTrailMap();
                    treasureTrailMap.id = jsonObject.getString("id");
                    treasureTrailMap.location = jsonObject.getString("location");
                    treasureTrails.treasureTrailMaps.add(treasureTrailMap);
                }
            }
            catch (JSONException ex) {
                Logger.log(ex);
            }
            return treasureTrails;
        }

        @Override
        protected void onPostExecute(TreasureTrails items) {
            if (items.treasureTrails.size() > 0 && items.treasureTrailMaps.size() > 0) {
                treasureTrailsLoadedListener.onTreasureTrailsLoaded(items);
            }
            else {
                treasureTrailsLoadedListener.onTreasureTrailsLoadError();
            }
        }
    }

    @Override
    public void cancelRunningTasks() {
        AppController.getInstance().cancelPendingRequests(TT_REQUEST_TAG);
    }

    @Override
    public boolean wasRequesting() {
        return false;
    }

}
