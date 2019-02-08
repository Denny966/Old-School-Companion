package com.flipkart.chatheads.container;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;

import com.facebook.rebound.SpringConfigRegistry;
import com.facebook.rebound.SpringSystem;
import com.flipkart.chatheads.ChatHead;
import com.flipkart.chatheads.ChatHeads;
import com.flipkart.chatheads.ChatHeadsContainer;
import com.flipkart.chatheads.R;
import com.flipkart.chatheads.arrangement.ChatHeadArrangement;
import com.flipkart.chatheads.arrangement.MaximizedArrangement;
import com.flipkart.chatheads.arrangement.MinimizedArrangement;
import com.flipkart.chatheads.config.ChatHeadConfig;
import com.flipkart.chatheads.config.ChatHeadDefaultConfig;
import com.flipkart.chatheads.config.FloatingViewPreferences;
import com.flipkart.chatheads.custom.ContentView;
import com.flipkart.chatheads.interfaces.ChatHeadContainer;
import com.flipkart.chatheads.interfaces.ChatHeadListener;
import com.flipkart.chatheads.interfaces.ChatHeadManager;
import com.flipkart.chatheads.interfaces.ChatHeadViewAdapter;
import com.flipkart.chatheads.interfaces.ChatHeadSelectedListener;
import com.flipkart.chatheads.utils.SpringConfigsHolder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class DefaultChatHeadManager implements ChatHeadManager {

    private static final int OVERLAY_TRANSITION_DURATION = 200;
    private final Map<Class<? extends ChatHeadArrangement>, ChatHeadArrangement> arrangements = new HashMap<>(3);
    private final Context context;
    private final WindowManagerContainer windowManagerContainer;
    private ChatHeads chatHeads;
    private int maxWidth;
    private int maxHeight;
    private ChatHeadArrangement activeArrangement;
    private ChatHeadViewAdapter viewAdapter;
    private ChatHeadOverlayView overlayView;
    private ChatHeadSelectedListener chatHeadSelectedListener;
    private boolean overlayVisible;
    private SpringSystem springSystem;
    private FragmentManager fragmentManager;
    private Fragment currentFragment;
    private ChatHeadConfig config;
    private ChatHeadListener listener;
    private Bundle activeArrangementBundle;
    private ArrangementChangeRequest requestedArrangement;
    private DisplayMetrics displayMetrics;
    private ContentView contentView;
    private FullscreenChangeListener fullscreenChangeListener;
    private FloatingViewPreferences floatingViewPreferences;


    public DefaultChatHeadManager(Context context, WindowManagerContainer windowManagerContainer, FloatingViewPreferences floatingViewPreferences) {
        this.context = context;
        this.windowManagerContainer = windowManagerContainer;
        this.displayMetrics = windowManagerContainer.getDisplayMetrics();
        this.floatingViewPreferences = floatingViewPreferences;
        init(context, new ChatHeadDefaultConfig(context, floatingViewPreferences));
    }

    public ChatHeadContainer getWindowManagerContainer() {
        return windowManagerContainer;
    }

    @Override
    public DisplayMetrics getDisplayMetrics() {
        return displayMetrics;
    }


    @Override
    public ChatHeadListener getListener() {
        return listener;
    }

    @Override
    public void setListener(ChatHeadListener listener) {
        this.listener = listener;
    }

    @Override
    public ChatHeads getChatHeads() {
        return chatHeads;
    }

    public ChatHeadsContainer getChatHeadsContainer() {
        return this.windowManagerContainer.getChatHeadsContainer();
    }

    @Override
    public ChatHeadViewAdapter getViewAdapter() {
        return viewAdapter;
    }

    @Override
    public void setViewAdapter(ChatHeadViewAdapter chatHeadViewAdapter) {
        this.viewAdapter = chatHeadViewAdapter;
    }

    @Override
    public int getMaxWidth() {
        return maxWidth;
    }

    @Override
    public int getMaxHeight() {
        return maxHeight;
    }

    @Override
    public Context getContext() {
        return context;
    }

    @Override
    public Class<? extends ChatHeadArrangement> getArrangementType() {
        if (activeArrangement != null) {
            return activeArrangement.getClass();
        }
        else if (requestedArrangement != null) {
            return requestedArrangement.getArrangement();
        }
        return null;
    }

    @Override
    public ChatHeadArrangement getActiveArrangement() {
        if (activeArrangement != null) {
            return activeArrangement;
        }
        return null;
    }

    @Override
    public void selectChatHead(ChatHead chatHead) {
        if (activeArrangement != null)
            activeArrangement.selectChatHead(chatHead);
    }

    @Override
    public void selectChatHead(String key) {
        ChatHead chatHead = chatHeads.getByKey(key);
        if (chatHead != null) {
            selectChatHead(chatHead);
        }
    }

    @Override
    public void onMeasure(int height, int width) {
        boolean needsLayout = false;
        if (height != maxHeight && width != maxWidth) {
            needsLayout = true; // both changed, must be screen rotation.
        }
        maxHeight = height;
        maxWidth = width;

        int closeButtonCenterX = (int) ((float) width * 0.5f);
        int closeButtonCenterY = (int) ((float) height * 0.9f);


        if (maxHeight > 0 && maxWidth > 0) {
            if (requestedArrangement != null) {
                setArrangementImpl(requestedArrangement);
                requestedArrangement = null;
            }
            else {
                if (needsLayout) {
                    // this means height changed and we need to redraw.
                    setArrangementImpl(new ArrangementChangeRequest(activeArrangement.getClass(), null, false));
                }
            }
        }
    }

    @Override
    public ChatHead addChatHead(String key, boolean animated) {
        ChatHead chatHead = chatHeads.getByKey(key);
        if (chatHead == null) {
            chatHead = new ChatHead(key, this, springSystem, getContext());
            chatHeads.add(chatHead);
            ViewGroup.LayoutParams layoutParams = windowManagerContainer.createLayoutParams(getConfig().getHeadWidth(), getConfig().getHeadHeight(), Gravity.START | Gravity.TOP, 0);
            windowManagerContainer.addView(chatHead, layoutParams);
            reloadDrawable(key);
            if (activeArrangement != null) {
                activeArrangement.onChatHeadAdded(chatHead, animated);
            }
            if (listener != null) {
                listener.onChatHeadAdded(key);
            }
        }
        return chatHead;
    }


    @Override
    public void reloadDrawable(String key) {
        Drawable chatHeadDrawable = viewAdapter.getChatHeadDrawable(key);
        if (chatHeadDrawable != null) {
            chatHeads.getByKey(key).setImageDrawable(viewAdapter.getChatHeadDrawable(key));
        }
    }

    @Override
    public void removeAllChatHeads(boolean userTriggered) {
        for (Iterator<ChatHead> iterator = chatHeads.iterator(); iterator.hasNext(); ) {
            ChatHead chatHead = iterator.next();
            iterator.remove();
            onChatHeadRemoved(chatHead, userTriggered);
        }
    }

    @Override
    public boolean removeChatHead(String key, boolean userTriggered) {
        ChatHead chatHead = chatHeads.getByKey(key);
        if (chatHead != null) {
            chatHeads.remove(chatHead);
            onChatHeadRemoved(chatHead, userTriggered);
            return true;
        }
        return false;
    }

    private void onChatHeadRemoved(ChatHead chatHead, boolean userTriggered) {
        if (chatHead != null && chatHead.getParent() != null) {
            chatHead.onRemove();
            windowManagerContainer.removeView(chatHead);
            if (activeArrangement != null)
                activeArrangement.onChatHeadRemoved(chatHead);
            if (listener != null) {
                listener.onChatHeadRemoved(chatHead.getKey(), userTriggered);
            }
        }
    }

    @Override
    public ChatHeadOverlayView getOverlayView() {
        return overlayView;
    }

    public void hideAllChatHeads() {
        getChatHeadsContainer().hide();
        windowManagerContainer.hideMotionCaptureView();
        setArrangement(MinimizedArrangement.class, null);
    }

    public void showAllChatHeads() {
        getChatHeadsContainer().show();
        windowManagerContainer.showMotionCaptureView();
    }

    private void init(Context context, ChatHeadConfig chatHeadDefaultConfig) {
        chatHeads = new ChatHeads(floatingViewPreferences.getFloatingViewCount());
        this.config = chatHeadDefaultConfig; //TODO : needs cleanup
        springSystem = SpringSystem.create();
        windowManagerContainer.onInitialized(this);
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(metrics);
        this.displayMetrics = metrics;
        contentView = new ContentView(context);
        contentView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        windowManagerContainer.addView(contentView, contentView.getLayoutParams());
        contentView.setVisibility(View.GONE);
        arrangements.put(MinimizedArrangement.class, new MinimizedArrangement(this));
        arrangements.put(MaximizedArrangement.class, new MaximizedArrangement(this));
        setupOverlay(context);
        setConfig(chatHeadDefaultConfig);

        SpringConfigRegistry.getInstance().addSpringConfig(SpringConfigsHolder.DRAGGING, "dragging mode");
        SpringConfigRegistry.getInstance().addSpringConfig(SpringConfigsHolder.NOT_DRAGGING, "not dragging mode");
    }

    private void setupOverlay(Context context) {
        overlayView = new ChatHeadOverlayView(context);
        overlayView.setBackgroundResource(R.drawable.overlay_transition);
        final ViewGroup.LayoutParams layoutParams = getWindowManagerContainer().createLayoutParams(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT, Gravity.NO_GRAVITY, 0);
        getWindowManagerContainer().addView(overlayView, layoutParams);

        final View dummyView = new View(context);
        dummyView.setBackgroundColor(Color.parseColor("white"));
        final WindowManager.LayoutParams dummyParams = windowManagerContainer.createContainerLayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, false);
        windowManagerContainer.getWindowManager().addView(dummyView, dummyParams);
        final ViewTreeObserver vto = dummyView.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int dummyViewHeight = dummyView.getHeight();
                int screenHeight = windowManagerContainer.getDisplayMetrics().heightPixels;
                // Fullscreen calculation hack based on a dummy view
                if (fullscreenChangeListener == null) {
                    return;
                }
                if (dummyViewHeight >= screenHeight) {
                    // 'fullscreen'
                    fullscreenChangeListener.onEnterFullscreen();
                }
                else {
                    fullscreenChangeListener.onExitFullscreen();
                }
            }
        });
    }

    @Override
    public ContentView getContentView() {
        return contentView;
    }


    @Override
    public ChatHeadArrangement getArrangement(Class<? extends ChatHeadArrangement> arrangementType) {
        return arrangements.get(arrangementType);
    }

    @Override
    public void setArrangement(final Class<? extends ChatHeadArrangement> arrangement, Bundle extras) {
        setArrangement(arrangement, extras, true);
    }

    @Override
    public void setArrangement(final Class<? extends ChatHeadArrangement> arrangement, Bundle extras, boolean animated) {
        this.requestedArrangement = new ArrangementChangeRequest(arrangement, extras, animated);
        windowManagerContainer.requestLayout();
    }

    /**
     * Should only be called after onMeasure
     *
     * @param requestedArrangementParam
     */
    private void setArrangementImpl(ArrangementChangeRequest requestedArrangementParam) {
        boolean hasChanged = false;
        ChatHeadArrangement requestedArrangement = arrangements.get(requestedArrangementParam.getArrangement());
        ChatHeadArrangement oldArrangement = null;
        ChatHeadArrangement newArrangement = requestedArrangement;
        Bundle extras = requestedArrangementParam.getExtras();
        if (activeArrangement != requestedArrangement)
            hasChanged = true;
        if (extras == null)
            extras = new Bundle();

        if (activeArrangement != null) {
            extras.putAll(activeArrangement.getRetainBundle());
            activeArrangement.onDeactivate(maxWidth, maxHeight);
            oldArrangement = activeArrangement;
        }
        activeArrangement = requestedArrangement;
        activeArrangementBundle = extras;
        requestedArrangement.onActivate(this, extras, maxWidth, maxHeight, requestedArrangementParam.isAnimated());
        if (hasChanged) {
            windowManagerContainer.onArrangementChanged(oldArrangement, newArrangement);
            if (listener != null)
                listener.onChatHeadArrangementChanged(oldArrangement, newArrangement);
        }
    }

    @Override
    public void hideOverlayView(boolean animated) {
        if (overlayVisible) {
            TransitionDrawable drawable = (TransitionDrawable) overlayView.getBackground();
            int duration = OVERLAY_TRANSITION_DURATION;
            if (!animated)
                duration = 0;
            drawable.reverseTransition(duration);
            overlayView.setClickable(false);
            overlayVisible = false;
        }
    }

    @Override
    public void showOverlayView(boolean animated) {
        if (!overlayVisible) {
            TransitionDrawable drawable = (TransitionDrawable) overlayView.getBackground();
            int duration = OVERLAY_TRANSITION_DURATION;
            if (!animated)
                duration = 0;
            drawable.startTransition(duration);
            overlayView.setClickable(true);
            overlayVisible = true;
        }
    }

    @Override
    public void setOnChatHeadSelectedListener(ChatHeadSelectedListener chatHeadSelectedListener) {
        this.chatHeadSelectedListener = chatHeadSelectedListener;
    }

    @Override
    public boolean onItemSelected(ChatHeadsContainer chatHead) {
        return false;

    }

    @Override
    public boolean onChatHeadSelected(ChatHead chatHead) {
        return chatHeadSelectedListener != null && chatHeadSelectedListener.onChatHeadSelected(chatHead.getKey(), chatHead);
    }

    @Override
    public void bringToFront(ChatHead chatHead) {
        if (activeArrangement != null) {
            activeArrangement.bringToFront(chatHead);
        }
    }

    @Override
    public void recreateView(String key) {
        ChatHead chatHead = chatHeads.getByKey(key);
        detachView(chatHead, getContentView());
        removeView(chatHead, getContentView());
        if (activeArrangement != null) {
            activeArrangement.onReloadFragment(chatHead);
        }
    }

    @Override
    public SpringSystem getSpringSystem() {
        return springSystem;
    }

    @Override
    public View attachView(ChatHead activeChatHead, ViewGroup parent) {
        View view = viewAdapter.attachView(activeChatHead.getKey(), activeChatHead, parent);
        return view;
    }

    @Override
    public void removeView(ChatHead chatHead, ViewGroup parent) {
        viewAdapter.removeView(chatHead.getKey(), chatHead, parent);
    }


    @Override
    public void detachView(ChatHead chatHead, ViewGroup parent) {
        viewAdapter.detachView(chatHead.getKey(), chatHead, parent);
    }


    @Override
    public ChatHeadConfig getConfig() {
        return config;
    }

    @Override
    public void setConfig(ChatHeadConfig config) {
        this.config = config;
        for (Map.Entry<Class<? extends ChatHeadArrangement>, ChatHeadArrangement> arrangementEntry : arrangements.entrySet()) {
            arrangementEntry.getValue().onConfigChanged(config);
        }
    }

    @Override
    public Parcelable onSaveInstanceState(Parcelable superState) {
        SavedState savedState = new SavedState(superState);
        if (activeArrangement != null) {
            savedState.setActiveArrangement(activeArrangement.getClass());
            savedState.setActiveArrangementBundle(activeArrangement.getRetainBundle());
        }
        savedState.setChatHeads(chatHeads.getKeys());
        return savedState;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        if (state instanceof SavedState) {
            SavedState savedState = (SavedState) state;
            final Class activeArrangementClass = savedState.getActiveArrangement();
            final Bundle activeArrangementBundle = savedState.getActiveArrangementBundle();
            final ArrayList<String> keys = savedState.getChatHeads();
            for (String key : keys) {
                addChatHead(key, false);
            }
            if (activeArrangementClass != null) {
                setArrangement(activeArrangementClass, activeArrangementBundle, false);
            }
        }
    }


    public void setFullscreenChangeListener(FullscreenChangeListener listener) {
        fullscreenChangeListener = listener;
    }

    @Override
    public FloatingViewPreferences getFloatingViewPreferences() {
        return floatingViewPreferences;
    }

    public interface FullscreenChangeListener {
        void onExitFullscreen();

        void onEnterFullscreen();
    }

    static class SavedState extends View.BaseSavedState {
        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {

            @Override
            public SavedState createFromParcel(Parcel source) {
                return new SavedState(source);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
        private Class<? extends ChatHeadArrangement> activeArrangement;
        private Bundle activeArrangementBundle;
        private ArrayList<String> keys;

        public SavedState(Parcel source) {
            super(source);
            activeArrangement = (Class<? extends ChatHeadArrangement>) source.readSerializable();
            activeArrangementBundle = source.readBundle();
            keys = (ArrayList<String>) source.readSerializable();
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }

        public Class<? extends ChatHeadArrangement> getActiveArrangement() {
            return activeArrangement;
        }

        public void setActiveArrangement(Class<? extends ChatHeadArrangement> activeArrangement) {
            this.activeArrangement = activeArrangement;
        }

        public Bundle getActiveArrangementBundle() {
            return activeArrangementBundle;
        }

        public void setActiveArrangementBundle(Bundle activeArrangementBundle) {
            this.activeArrangementBundle = activeArrangementBundle;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeSerializable(activeArrangement);
            dest.writeBundle(activeArrangementBundle);
            dest.writeSerializable(keys);
        }

        public ArrayList<String> getChatHeads() {
            return keys;
        }

        public void setChatHeads(ArrayList<String> keys) {
            this.keys = keys;
        }
    }

    private class ArrangementChangeRequest {
        private final Bundle extras;
        private final Class<? extends ChatHeadArrangement> arrangement;
        private final boolean animated;

        public ArrangementChangeRequest(Class<? extends ChatHeadArrangement> arrangement, Bundle extras, boolean animated) {
            this.arrangement = arrangement;
            this.extras = extras;
            this.animated = animated;
        }

        public Bundle getExtras() {
            return extras;
        }

        public Class<? extends ChatHeadArrangement> getArrangement() {
            return arrangement;
        }

        public boolean isAnimated() {
            return animated;
        }
    }
}
