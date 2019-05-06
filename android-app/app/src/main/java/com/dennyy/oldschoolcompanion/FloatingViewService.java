package com.dennyy.oldschoolcompanion;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.dennyy.oldschoolcompanion.enums.ReloadTimerSource;
import com.dennyy.oldschoolcompanion.helpers.*;
import com.dennyy.oldschoolcompanion.models.FloatingViews.FloatingView;
import com.dennyy.oldschoolcompanion.models.Notes.NoteChangeEvent;
import com.dennyy.oldschoolcompanion.models.Timers.ReloadTimersEvent;
import com.dennyy.oldschoolcompanion.models.TodoList.ReloadTodoListEvent;
import com.dennyy.oldschoolcompanion.models.Worldmap.WorldmapDownloadedEvent;
import com.dennyy.oldschoolcompanion.viewhandlers.*;
import com.flipkart.chatheads.ChatHead;
import com.flipkart.chatheads.arrangement.ChatHeadArrangement;
import com.flipkart.chatheads.arrangement.MinimizedArrangement;
import com.flipkart.chatheads.config.FloatingViewPreferences;
import com.flipkart.chatheads.container.DefaultChatHeadManager;
import com.flipkart.chatheads.container.WindowManagerContainer;
import com.flipkart.chatheads.interfaces.ChatHeadManager;
import com.flipkart.chatheads.interfaces.ChatHeadManagerListener;
import com.flipkart.chatheads.interfaces.ChatHeadViewAdapter;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.*;

import static com.dennyy.oldschoolcompanion.helpers.Constants.SORT_DELIMITER;

public class FloatingViewService extends Service implements WindowManagerContainer.ArrangementChangeListener, ChatHeadManagerListener {
    public static final Map<String, FloatingView> MAP = new LinkedHashMap<>();
    public static final String DEFAULT_SEPARATOR = "~";

    private static final String STOP_SERVICE_PARAMETER = "stop";
    private static final int NOTIFICATION_ID = 1337;

    private DefaultChatHeadManager chatHeadManager;
    private WindowManagerContainer windowManagerContainer;
    private Map<String, View> viewCache = new HashMap<>();

    private NotesViewHandler notesViewHandler;
    private TimersViewHandler timersViewHandler;
    private WorldmapViewHandler worldmapViewHandler;
    private TodoViewHandler todoViewHandler;

    public FloatingViewService() {

    }

    public static void init(Context context) {
        if (MAP.size() > 0) {
            return;
        }
        String[] floatingViews = context.getResources().getStringArray(R.array.view_name_value);
        String[] floatingViewNames = context.getResources().getStringArray(R.array.view_names);
        TypedArray floatingViewDrawables = context.getResources().obtainTypedArray(R.array.view_name_drawables);
        TypedArray floatingViewLayouts = context.getResources().obtainTypedArray(R.array.view_name_layouts);

        for (int i = 0; i < floatingViews.length; i++) {
            String id = floatingViews[i];
            String name = floatingViewNames[i];
            int drawableId = floatingViewDrawables.getResourceId(i, -1);
            int layoutId = floatingViewLayouts.getResourceId(i, -1);
            FloatingView floatingView = new FloatingView(id, name, drawableId, layoutId);
            MAP.put(id, floatingView);
        }
        updateSortOrder();
        floatingViewDrawables.recycle();
        floatingViewLayouts.recycle();
    }

    public static void updateSortOrder() {
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(AppController.getInstance().getApplicationContext());
        try {
            Set<String> hashSet = preferences.getStringSet(Constants.PREF_FLOATING_VIEWS_SORT_ORDER, new HashSet<String>());
            for (String sortOrder : hashSet) {
                String[] values = sortOrder.split(SORT_DELIMITER);
                MAP.get(values[0]).setSortOrder(Integer.parseInt(values[1]));
            }
        }
        catch (Exception e) {
            Logger.log("error updating floating views sortorder", e);
        }
    }

    @Override
    public void onCreate() {
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(FloatingViewService.this);
        EventBus.getDefault().register(this);
        init(this);
        AdBlocker.init(this);
        GeHelper.init(this);
        windowManagerContainer = new WindowManagerContainer(this);
        windowManagerContainer.setListener(this);
        chatHeadManager = new DefaultChatHeadManager(this, windowManagerContainer, getFloatingViewPreferences(preferences));
        chatHeadManager.setArrangement(MinimizedArrangement.class, null);
        chatHeadManager.setViewAdapter(new ChatHeadViewAdapter() {
            @Override
            public View attachView(String key, ChatHead chatHead, ViewGroup parent) {
                View cachedView = viewCache.get(key);
                if (cachedView == null) {
                    FloatingView floatingView = MAP.get(key);
                    if (floatingView == null) {
                        throw new IllegalStateException("Floating view doesn't exist for " + key);
                    }
                    LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
                    cachedView = inflater.inflate(floatingView.layoutId, parent, false);
                    if (key.equals(getString(R.string.floating_view_math_calc))) {
                        new CalculatorViewHandler(FloatingViewService.this, cachedView);
                    }
                    else if (key.equals(getString(R.string.floating_view_ge))) {
                        new GrandExchangeViewHandler(FloatingViewService.this, cachedView, true, null);
                    }
                    else if (key.equals(getString(R.string.floating_view_hiscores_lookup))) {
                        new HiscoresLookupViewHandler(FloatingViewService.this, cachedView);
                    }
                    else if (key.equals(getString(R.string.floating_view_hiscores_compare))) {
                        new HiscoresCompareViewHandler(FloatingViewService.this, cachedView);
                    }
                    else if (key.equals(getString(R.string.floating_view_tracker))) {
                        new TrackerViewHandler(FloatingViewService.this, cachedView);
                    }
                    else if (key.equals(getString(R.string.floating_view_treasure_trails))) {
                        new TreasureTrailViewHandler(FloatingViewService.this, cachedView, true, null);
                    }
                    else if (key.equals(getString(R.string.floating_view_notes))) {
                        notesViewHandler = new NotesViewHandler(FloatingViewService.this, cachedView, true);
                    }
                    else if (key.equals(getString(R.string.floating_view_cmb_calc))) {
                        new CombatCalculatorViewHandler(FloatingViewService.this, cachedView);
                    }
                    else if (key.equals(getString(R.string.floating_view_exp_calc))) {
                        new ExpCalculatorViewHandler(FloatingViewService.this, cachedView, true);
                    }
                    else if (key.equals(getString(R.string.floating_view_skill_calc))) {
                        new SkillCalculatorViewHandler(FloatingViewService.this, cachedView, true);
                    }
                    else if (key.equals(getString(R.string.floating_view_quest_guide))) {
                        new QuestViewHandler(FloatingViewService.this, cachedView, true, null);
                    }
                    else if (key.equals(getString(R.string.floating_view_fairy_rings))) {
                        new FairyRingViewHandler(FloatingViewService.this, cachedView);
                    }
                    else if (key.equals(getString(R.string.floating_view_diary_calc))) {
                        new DiaryCalculatorViewHandler(FloatingViewService.this, cachedView, null);
                    }
                    else if (key.equals(getString(R.string.floating_view_osrs_wiki))) {
                        new RSWikiViewHandler(FloatingViewService.this, cachedView, true);
                    }
                    else if (key.equals(getString(R.string.floating_view_osrs_news))) {
                        new OSRSNewsViewHandler(FloatingViewService.this, cachedView, true);
                    }
                    else if (key.equals(getString(R.string.floating_view_timers))) {
                        timersViewHandler = new TimersViewHandler(FloatingViewService.this, cachedView, true);
                    }
                    else if (key.equals(getString(R.string.floating_view_worldmap))) {
                        worldmapViewHandler = new WorldmapViewHandler(FloatingViewService.this, cachedView, true);
                    }
                    else if (key.equals(getString(R.string.floating_view_todo_list))) {
                        todoViewHandler = new TodoViewHandler(FloatingViewService.this, cachedView, true);
                    }
                    else if (key.equals(getString(R.string.floating_view_bestiary))) {
                        new BestiaryViewHandler(FloatingViewService.this, cachedView, true);
                    }
                    else if (key.equals(getString(R.string.floating_view_alch_overview))) {
                        new AlchOverviewViewHandler(FloatingViewService.this, cachedView, true);
                    }
                    viewCache.put(key, cachedView);
                }
                parent.addView(cachedView);
                return cachedView;
            }

            @Override
            public void detachView(String key, ChatHead chatHead, ViewGroup parent) {
                View cachedView = viewCache.get(key);
                if (cachedView != null) {
                    parent.removeView(cachedView);
                }
            }

            @Override
            public void removeView(String key, ChatHead chatHead, ViewGroup parent) {
                View cachedView = viewCache.get(key);
                if (cachedView != null) {
                    viewCache.remove(key);
                    parent.removeView(cachedView);
                }
            }

            @Override
            public Drawable getChatHeadDrawable(String key) {
                FloatingView floatingView = MAP.get(key);
                int resourceId = R.drawable.default_floating_view;
                if (floatingView != null) {
                    resourceId = floatingView.drawableId;
                }
                Drawable drawable = getResources().getDrawable(resourceId);
                return drawable;
            }
        });

        String floatingViews = preferences.getString(Constants.PREF_FLOATING_VIEWS, "");
        String[] selected = floatingViews.split(DEFAULT_SEPARATOR);
        if (Utils.isNullOrEmpty(floatingViews) || selected.length < 1) {
            this.onDestroy();
            return;
        }
        List<FloatingView> selection = new ArrayList<>();
        for (String id : selected) {
            selection.add(MAP.get(id));
        }
        Collections.sort(selection);
        for (FloatingView floatingView : selection) {
            chatHeadManager.addChatHead(floatingView.id, false);
        }

        chatHeadManager.setFullscreenChangeListener(new DefaultChatHeadManager.FullscreenChangeListener() {
            @Override
            public void onEnterFullscreen() {
                boolean landScapeOnly = preferences.getBoolean(Constants.PREF_LANDSCAPE_ONLY, false);

                if (landScapeOnly && windowManagerContainer.getOrientation() != Configuration.ORIENTATION_LANDSCAPE) {
                    chatHeadManager.hideAllChatHeads();
                }
                else {
                    chatHeadManager.showAllChatHeads();
                }
            }

            @Override
            public void onExitFullscreen() {
                boolean landScapeOnly = preferences.getBoolean(Constants.PREF_LANDSCAPE_ONLY, false);
                boolean fullscreenOnly = preferences.getBoolean(Constants.PREF_FULLSCREEN_ONLY, false);

                if ((landScapeOnly && windowManagerContainer.getOrientation() != Configuration.ORIENTATION_LANDSCAPE) || fullscreenOnly) {
                    chatHeadManager.hideAllChatHeads();
                }
                else {
                    chatHeadManager.showAllChatHeads();
                }
            }
        });
        chatHeadManager.setChatHeadManagerListener(this);
        runAsForeground();
    }

    private void runAsForeground() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, BuildConfig.APPLICATION_ID)
                .setSmallIcon(R.drawable.persistent_notification_icon)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.oldschoolcompanion))
                .setContentTitle(getResources().getString(R.string.app_name))
                .setContentText(getResources().getString(R.string.floating_view_service_running))
                .setContentIntent(pendingIntent);

        startForeground(NOTIFICATION_ID, builder.build());
    }

    @Subscribe
    public void onNoteChangeEvent(NoteChangeEvent event) {
        if (notesViewHandler != null && !event.isFloatingView) {
            notesViewHandler.setNote(event.note);
        }
    }

    @Subscribe
    public void reloadTimers(ReloadTimersEvent event) {
        if (timersViewHandler != null && event.source != ReloadTimerSource.FLOATINTG_VIEW) {
            timersViewHandler.reloadTimers();
        }
    }

    @Subscribe
    public void onWorldmapDownloaded(WorldmapDownloadedEvent event) {
        if (worldmapViewHandler != null) {
            worldmapViewHandler.loadWorldmap(null);
        }
    }

    @Subscribe
    public void reloadTodoList(ReloadTodoListEvent event) {
        if (todoViewHandler != null && (!event.isFloatingView || event.forceReload)) {
            todoViewHandler.reloadTodoList();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getBooleanExtra(STOP_SERVICE_PARAMETER, false)) {
            this.onDestroy();
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        unregisterReceiver();
        windowManagerContainer.destroy();
        stopForeground(true);
        stopSelf();
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Override
    public void onArrangementChanged(ChatHeadArrangement arrangement) {

    }

    private FloatingViewPreferences getFloatingViewPreferences(SharedPreferences preferences) {
        float inactiveAlpha = 0.2f + (preferences.getInt(Constants.PREF_OPACITY, 3) * 0.1f);
        boolean startRightSide = preferences.getBoolean(Constants.PREF_RIGHT_SIDE, false);
        boolean alignFloatingViewsLeft = preferences.getBoolean(Constants.PREF_PADDING_SIDE, true);
        int alignmentMargin = preferences.getInt(Constants.PREF_PADDING, 0) * 5;
        alignmentMargin = (int) Utils.convertDpToPixel(alignmentMargin, FloatingViewService.this);
        int sizeDp = 10 + (preferences.getInt(Constants.PREF_SIZE, 8) * 5);
        boolean isHardwareAccelerated = preferences.getBoolean(Constants.PREF_HW_ACCELERATION, true);
        boolean showCloseButton = preferences.getBoolean(Constants.PREF_SHOW_CLOSE_BUTTON, true);
        float overlayOpacity = 0.2f + (preferences.getInt(Constants.PREF_OVERLAY_OPACITY, 8) * 0.1f);
        FloatingViewPreferences floatingViewPreferences = new FloatingViewPreferences(startRightSide, alignFloatingViewsLeft, alignmentMargin, inactiveAlpha, MAP.size(), sizeDp, isHardwareAccelerated, showCloseButton, overlayOpacity);
        return floatingViewPreferences;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void unregisterReceiver() {
        try {
            unregisterReceiver(windowManagerContainer.getReceiver());
        }
        catch (Exception ignored) {

        }
    }

    @Override
    public void onAllFloatingViewsClosed() {
        this.onDestroy();
    }
}