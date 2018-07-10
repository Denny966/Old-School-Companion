package com.dennyy.osrscompanion;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dennyy.osrscompanion.helpers.Constants;
import com.dennyy.osrscompanion.helpers.Utils;
import com.dennyy.osrscompanion.layouthandlers.BaseViewHandler;
import com.dennyy.osrscompanion.layouthandlers.CalculatorViewHandler;
import com.dennyy.osrscompanion.layouthandlers.GrandExchangeViewHandler;
import com.dennyy.osrscompanion.layouthandlers.HiscoresCompareViewHandler;
import com.dennyy.osrscompanion.layouthandlers.HiscoresLookupViewHandler;
import com.dennyy.osrscompanion.layouthandlers.NotesViewHandler;
import com.dennyy.osrscompanion.layouthandlers.TrackerViewHandler;
import com.dennyy.osrscompanion.layouthandlers.TreasureTrailViewHandler;
import com.flipkart.chatheads.ChatHead;
import com.flipkart.chatheads.ChatHeadViewAdapter;
import com.flipkart.chatheads.arrangement.ChatHeadArrangement;
import com.flipkart.chatheads.arrangement.MaximizedArrangement;
import com.flipkart.chatheads.arrangement.MinimizedArrangement;
import com.flipkart.chatheads.container.DefaultChatHeadManager;
import com.flipkart.chatheads.container.WindowManagerContainer;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class FloatingViewService extends Service implements WindowManagerContainer.ArrangementChangeListener {
    private final static String calcHeadName = CalculatorViewHandler.class.getSimpleName();
    private final static String geHeadName = GrandExchangeViewHandler.class.getSimpleName();
    private final static String trackerHeadName = TrackerViewHandler.class.getSimpleName();
    private final static String hiscoreLookupHeadName = HiscoresLookupViewHandler.class.getSimpleName();
    private final static String hiscoreCompareHeadName = HiscoresCompareViewHandler.class.getSimpleName();
    private final static String treasureTrailHeadName = TreasureTrailViewHandler.class.getSimpleName();
    private final static String notesHeadName = NotesViewHandler.class.getSimpleName();

    private DefaultChatHeadManager<String> chatHeadManager;
    private WindowManagerContainer windowManagerContainer;
    private Map<String, View> viewCache = new HashMap<>();

    private CalculatorViewHandler calculatorViewHandler;
    private GrandExchangeViewHandler grandExchangeViewHandler;
    private NotesViewHandler notesViewHandler;

    public FloatingViewService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(FloatingViewService.this);

        windowManagerContainer = new WindowManagerContainer(this);
        windowManagerContainer.setListener(this);
        chatHeadManager = new DefaultChatHeadManager<>(this, windowManagerContainer, preferences.getBoolean(Constants.PREF_RIGHT_SIDE, false));
        chatHeadManager.setArrangement(MinimizedArrangement.class, null);
        chatHeadManager.setViewAdapter(new ChatHeadViewAdapter<String>() {
            @Override
            public View attachView(String key, ChatHead<? extends Serializable> chatHead, ViewGroup parent) {
                View cachedView = viewCache.get(key);
                if (cachedView == null) {
                    LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
                    if (key.equals(calcHeadName)) {
                        cachedView = inflater.inflate(R.layout.calculator_layout, parent, false);
                        calculatorViewHandler = new CalculatorViewHandler(FloatingViewService.this, cachedView);
                    }
                    else if (key.equals(geHeadName)) {
                        cachedView = inflater.inflate(R.layout.grand_exchange_layout, parent, false);
                        grandExchangeViewHandler = new GrandExchangeViewHandler(FloatingViewService.this, cachedView);
                    }
                    else if (key.equals(hiscoreLookupHeadName)) {
                        cachedView = inflater.inflate(R.layout.hiscores_lookup_layout, parent, false);
                        new HiscoresLookupViewHandler(FloatingViewService.this, cachedView);
                    }
                    else if (key.equals(hiscoreCompareHeadName)) {
                        cachedView = inflater.inflate(R.layout.hiscores_compare_layout, parent, false);
                        new HiscoresCompareViewHandler(FloatingViewService.this, cachedView);
                    }
                    else if (key.equals(trackerHeadName)) {
                        cachedView = inflater.inflate(R.layout.tracker_layout, parent, false);
                        new TrackerViewHandler(FloatingViewService.this, cachedView);
                    }
                    else if (key.equals(treasureTrailHeadName)) {
                        cachedView = inflater.inflate(R.layout.clue_scroll_layout, parent, false);
                        new TreasureTrailViewHandler(FloatingViewService.this, cachedView, null);
                    }
                    else if (key.equals(notesHeadName)) {
                        cachedView = inflater.inflate(R.layout.notes_layout, parent, false);
                        notesViewHandler = new NotesViewHandler(FloatingViewService.this, cachedView);
                    }
                    viewCache.put(key, cachedView);
                }
                parent.addView(cachedView);
                return cachedView;
            }

            @Override
            public void detachView(String key, ChatHead<? extends Serializable> chatHead, ViewGroup parent) {
                View cachedView = viewCache.get(key);
                if (cachedView != null) {
                    parent.removeView(cachedView);
                }
            }

            @Override
            public void removeView(String key, ChatHead<? extends Serializable> chatHead, ViewGroup parent) {
                View cachedView = viewCache.get(key);
                if (cachedView != null) {
                    viewCache.remove(key);
                    parent.removeView(cachedView);
                }
            }

            @Override
            public Drawable getChatHeadDrawable(String key) {
                Drawable drawable = null;

                if (key.equals(calcHeadName)) {
                    drawable = getResources().getDrawable(R.drawable.calculator_floating_view);
                }
                else if (key.equals(geHeadName)) {
                    drawable = getResources().getDrawable(R.drawable.ge_floating_view);
                }
                else if (key.equals(hiscoreLookupHeadName)) {
                    drawable = getResources().getDrawable(R.drawable.hiscore_lookup_floating_view);
                }
                else if (key.equals(hiscoreCompareHeadName)) {
                    drawable = getResources().getDrawable(R.drawable.hiscore_compare_floating_view);
                }
                else if (key.equals(trackerHeadName)) {
                    drawable = getResources().getDrawable(R.drawable.tracker_floating_view);
                }
                else if (key.equals(treasureTrailHeadName)) {
                    drawable = getResources().getDrawable(R.drawable.treasure_trails_floating_view);
                }
                else if (key.equals(notesHeadName)) {
                    drawable = getResources().getDrawable(R.drawable.notes_floating_view);
                }
                return drawable;

            }
        });

        chatHeadManager.setInactiveAlpha(0.2f + (preferences.getInt("pref_opacity", 3) * 0.1f));
        String selected = preferences.getString("pref_floating_views", "");
        if (Utils.containsCaseInsensitive("ge", selected))
            chatHeadManager.addChatHead(geHeadName, false, false);
        if (Utils.containsCaseInsensitive("tracker", selected))
            chatHeadManager.addChatHead(trackerHeadName, false, false);
        if (Utils.containsCaseInsensitive("hiscores_lookup", selected))
            chatHeadManager.addChatHead(hiscoreLookupHeadName, false, false);
        if (Utils.containsCaseInsensitive("hiscores_compare", selected))
            chatHeadManager.addChatHead(hiscoreCompareHeadName, false, false);
        if (Utils.containsCaseInsensitive("calc", selected))
            chatHeadManager.addChatHead(calcHeadName, false, false);
        if (Utils.containsCaseInsensitive("treasuretrails", selected))
            chatHeadManager.addChatHead(treasureTrailHeadName, false, false);
        if (Utils.containsCaseInsensitive("notes", selected))
            chatHeadManager.addChatHead(notesHeadName, false, false);

        chatHeadManager.setFullscreenChangeListener(new DefaultChatHeadManager.FullscreenChangeListener() {
            @Override
            public void onEnterFullscreen() {
                boolean landScapeOnly = preferences.getBoolean(Constants.PREF_LANDSCAPE_ONLY, false);

                if (landScapeOnly && windowManagerContainer.getOrientation() != Configuration.ORIENTATION_LANDSCAPE) {
                    chatHeadManager.hideAllChatheads();
                }
                else {
                    chatHeadManager.showAllChatheads();
                }
            }

            @Override
            public void onExitFullscreen() {
                boolean landScapeOnly = preferences.getBoolean(Constants.PREF_LANDSCAPE_ONLY, false);
                boolean fullscreenOnly = preferences.getBoolean(Constants.PREF_FULLSCREEN_ONLY, false);

                if ((landScapeOnly && windowManagerContainer.getOrientation() != Configuration.ORIENTATION_LANDSCAPE) || fullscreenOnly) {
                    chatHeadManager.hideAllChatheads();
                }
                else {
                    chatHeadManager.showAllChatheads();
                }
            }
        });
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        updateCalculatorLayout(calcHeadName, calculatorViewHandler);
        updateGrandExchangeLayout(geHeadName, grandExchangeViewHandler);
    }

    private ViewGroup removeFloatingView(String viewName, BaseViewHandler viewHandler) {
        View oldView = viewCache.get(viewName);
        if (oldView == null || viewHandler == null)
            return null;
        ViewGroup parent = (ViewGroup) oldView.getParent();
        if (parent != null) {
            parent.removeView(oldView);
        }
        oldView = null;
        viewHandler.cancelVolleyRequests();
        return parent;
    }

    private void updateCalculatorLayout(String viewName, BaseViewHandler viewHandler) {
        ViewGroup parent = removeFloatingView(viewName, viewHandler);
        if (parent == null)
            return;
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View newView = inflater.inflate(R.layout.calculator_layout, parent, false);
        calculatorViewHandler.updateView(newView);
        calculatorViewHandler.reloadData();

        viewCache.put(calcHeadName, newView);
    }


    private void updateGrandExchangeLayout(String viewName, BaseViewHandler viewHandler) {
        ViewGroup parent = removeFloatingView(viewName, viewHandler);
        if (parent == null)
            return;
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View newView = inflater.inflate(R.layout.grand_exchange_layout, parent, false);
        grandExchangeViewHandler.updateView(newView);
        if (grandExchangeViewHandler.geItemData != null && grandExchangeViewHandler.geupdateData != null && grandExchangeViewHandler.geGraphData != null)
            grandExchangeViewHandler.reloadData();
        if (grandExchangeViewHandler.wasRequesting()) {
            grandExchangeViewHandler.updateItem();
        }

        viewCache.put(geHeadName, newView);
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(windowManagerContainer.getReceiver());
        windowManagerContainer.destroy();
        stopSelf();
        super.onDestroy();
    }

    @Override
    public void onArrangementChanged(ChatHeadArrangement arrangement) {
        if (notesViewHandler != null && arrangement instanceof MaximizedArrangement)
            notesViewHandler.loadNote();
    }
}