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
import com.dennyy.osrscompanion.layouthandlers.CombatCalculatorViewHandler;
import com.dennyy.osrscompanion.layouthandlers.DiaryCalculatorViewHandler;
import com.dennyy.osrscompanion.layouthandlers.ExpCalculatorViewHandler;
import com.dennyy.osrscompanion.layouthandlers.FairyRingViewHandler;
import com.dennyy.osrscompanion.layouthandlers.GrandExchangeViewHandler;
import com.dennyy.osrscompanion.layouthandlers.HiscoresCompareViewHandler;
import com.dennyy.osrscompanion.layouthandlers.HiscoresLookupViewHandler;
import com.dennyy.osrscompanion.layouthandlers.NotesViewHandler;
import com.dennyy.osrscompanion.layouthandlers.QuestViewHandler;
import com.dennyy.osrscompanion.layouthandlers.RSWikiViewHandler;
import com.dennyy.osrscompanion.layouthandlers.SkillCalculatorViewHandler;
import com.dennyy.osrscompanion.layouthandlers.TrackerViewHandler;
import com.dennyy.osrscompanion.layouthandlers.TreasureTrailViewHandler;
import com.flipkart.chatheads.ChatHead;
import com.flipkart.chatheads.ChatHeadViewAdapter;
import com.flipkart.chatheads.arrangement.ChatHeadArrangement;
import com.flipkart.chatheads.arrangement.MaximizedArrangement;
import com.flipkart.chatheads.arrangement.MinimizedArrangement;
import com.flipkart.chatheads.config.FloatingViewPreferences;
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
    private final static String combatCalculatorHeadName = CombatCalculatorViewHandler.class.getSimpleName();
    private final static String expListHeadName = ExpCalculatorViewHandler.class.getSimpleName();
    private final static String skillCalcHeadName = SkillCalculatorViewHandler.class.getSimpleName();
    private final static String questHeadName = QuestViewHandler.class.getSimpleName();
    private final static String fairyRingHeadName = FairyRingViewHandler.class.getSimpleName();
    private final static String diaryCalcHeadName = DiaryCalculatorViewHandler.class.getSimpleName();
    private final static String rswikiHeadName = RSWikiViewHandler.class.getSimpleName();

    private DefaultChatHeadManager<String> chatHeadManager;
    private WindowManagerContainer windowManagerContainer;
    private Map<String, View> viewCache = new HashMap<>();
    private Map<String, Integer> iconsMap = new HashMap<>();
    private Map<String, String> namesMap = new HashMap<>();

    private CalculatorViewHandler calculatorViewHandler;
    private GrandExchangeViewHandler grandExchangeViewHandler;
    private NotesViewHandler notesViewHandler;

    public FloatingViewService() {

    }

    @Override
    public void onCreate() {
        super.onCreate();
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(FloatingViewService.this);
        initIconsMap();
        initNamesMap();

        windowManagerContainer = new WindowManagerContainer(this);
        windowManagerContainer.setListener(this);
        chatHeadManager = new DefaultChatHeadManager<>(this, windowManagerContainer, getFloatingViewPreferences(preferences));
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
                        grandExchangeViewHandler = new GrandExchangeViewHandler(FloatingViewService.this, cachedView, null);
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
                        cachedView = inflater.inflate(R.layout.treasure_trails_layout, parent, false);
                        new TreasureTrailViewHandler(FloatingViewService.this, cachedView, null);
                    }
                    else if (key.equals(notesHeadName)) {
                        cachedView = inflater.inflate(R.layout.notes_layout, parent, false);
                        notesViewHandler = new NotesViewHandler(FloatingViewService.this, cachedView);
                    }
                    else if (key.equals(combatCalculatorHeadName)) {
                        cachedView = inflater.inflate(R.layout.combat_calculator_layout, parent, false);
                        new CombatCalculatorViewHandler(FloatingViewService.this, cachedView);
                    }
                    else if (key.equals(expListHeadName)) {
                        cachedView = inflater.inflate(R.layout.exp_calc_layout, parent, false);
                        new ExpCalculatorViewHandler(FloatingViewService.this, cachedView);
                    }
                    else if (key.equals(skillCalcHeadName)) {
                        cachedView = inflater.inflate(R.layout.skill_calculator_layout, parent, false);
                        new SkillCalculatorViewHandler(FloatingViewService.this, cachedView);
                    }
                    else if (key.equals(questHeadName)) {
                        cachedView = inflater.inflate(R.layout.quest_layout, parent, false);
                        new QuestViewHandler(FloatingViewService.this, cachedView, null);
                    }
                    else if (key.equals(fairyRingHeadName)) {
                        cachedView = inflater.inflate(R.layout.fairy_ring_layout, parent, false);
                        new FairyRingViewHandler(FloatingViewService.this, cachedView);
                    }
                    else if (key.equals(diaryCalcHeadName)) {
                        cachedView = inflater.inflate(R.layout.diary_calculator_layout, parent, false);
                        new DiaryCalculatorViewHandler(FloatingViewService.this, cachedView);
                    }
                    else if (key.equals(rswikiHeadName)) {
                        cachedView = inflater.inflate(R.layout.rswiki_layout, parent, false);
                        new RSWikiViewHandler(FloatingViewService.this, cachedView,true);
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
                int resourceId = iconsMap.get(key);
                Drawable drawable = getResources().getDrawable(resourceId);
                return drawable;

            }
        });

        String[] selected = preferences.getString("pref_floating_views", "").split("~");
        String[] availableFloatingViews = getResources().getStringArray(R.array.view_name_value);
        for (String selection : selected) {
            Utils.containsCaseInsensitive(selection, availableFloatingViews);
            chatHeadManager.addChatHead(namesMap.get(selection), false, false);
        }

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
        grandExchangeViewHandler.reloadOnOrientationChanged();
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

    private FloatingViewPreferences getFloatingViewPreferences(SharedPreferences preferences) {
        float inactiveAlpha = 0.2f + (preferences.getInt(Constants.PREF_OPACITY, 3) * 0.1f);
        boolean startRightSide = preferences.getBoolean(Constants.PREF_RIGHT_SIDE, false);
        boolean alignFloatingViewsLeft = preferences.getBoolean(Constants.PREF_ALIGN_LEFT, true);
        int alignmentMargin = preferences.getInt(Constants.PREF_ALIGN_MARGIN, 0) * 5;
        alignmentMargin = (int) Utils.convertDpToPixel(alignmentMargin, FloatingViewService.this);

        FloatingViewPreferences floatingViewPreferences = new FloatingViewPreferences(startRightSide, alignFloatingViewsLeft, alignmentMargin, inactiveAlpha);
        return floatingViewPreferences;
    }

    private void initIconsMap() {
        iconsMap.put(calcHeadName, R.drawable.calculator_floating_view);
        iconsMap.put(geHeadName, R.drawable.ge_floating_view);
        iconsMap.put(hiscoreLookupHeadName, R.drawable.hiscore_lookup_floating_view);
        iconsMap.put(hiscoreCompareHeadName, R.drawable.hiscore_compare_floating_view);
        iconsMap.put(trackerHeadName, R.drawable.tracker_floating_view);
        iconsMap.put(treasureTrailHeadName, R.drawable.treasure_trails_floating_view);
        iconsMap.put(notesHeadName, R.drawable.notes_floating_view);
        iconsMap.put(combatCalculatorHeadName, R.drawable.cmb_calc_floating_view);
        iconsMap.put(expListHeadName, R.drawable.exp_list_floating_view);
        iconsMap.put(skillCalcHeadName, R.drawable.skill_calc_floating_view);
        iconsMap.put(questHeadName, R.drawable.quest_guide_floating_view);
        iconsMap.put(fairyRingHeadName, R.drawable.fairy_ring_floating_view);
        iconsMap.put(diaryCalcHeadName, R.drawable.diary_calc_floating_view);
        iconsMap.put(rswikiHeadName, R.drawable.rswiki_floating_view);
    }

    private void initNamesMap() {
        namesMap.put("ge", geHeadName);
        namesMap.put("tracker", trackerHeadName);
        namesMap.put("hiscores_lookup", hiscoreLookupHeadName);
        namesMap.put("hiscores_compare", hiscoreCompareHeadName);
        namesMap.put("math_calc", calcHeadName);
        namesMap.put("treasuretrails", treasureTrailHeadName);
        namesMap.put("notes", notesHeadName);
        namesMap.put("cmb_calc", combatCalculatorHeadName);
        namesMap.put("exp_calc", expListHeadName);
        namesMap.put("skill_calc", skillCalcHeadName);
        namesMap.put("quest_guide", questHeadName);
        namesMap.put("fairy_ring", fairyRingHeadName);
        namesMap.put("diary_calc", diaryCalcHeadName);
        namesMap.put("osrs_wiki", rswikiHeadName);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}