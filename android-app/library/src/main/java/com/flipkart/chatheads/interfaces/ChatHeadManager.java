package com.flipkart.chatheads.interfaces;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.rebound.SpringSystem;
import com.flipkart.chatheads.ChatHead;
import com.flipkart.chatheads.ChatHeads;
import com.flipkart.chatheads.ChatHeadsContainer;
import com.flipkart.chatheads.arrangement.ChatHeadArrangement;
import com.flipkart.chatheads.config.ChatHeadConfig;
import com.flipkart.chatheads.config.FloatingViewPreferences;
import com.flipkart.chatheads.container.ChatHeadOverlayView;
import com.flipkart.chatheads.custom.ContentView;

public interface ChatHeadManager {
    ChatHeadListener getListener();

    void setListener(ChatHeadListener listener);

    ChatHeads getChatHeads();

    ChatHeadViewAdapter getViewAdapter();

    void setViewAdapter(ChatHeadViewAdapter chatHeadViewAdapter);

    Class<? extends ChatHeadArrangement> getArrangementType();

    ChatHeadArrangement getActiveArrangement();

    /**
     * Selects the chat head. Very similar to performing touch up on it.
     *
     * @param chatHead
     */
    void selectChatHead(ChatHead chatHead);

    void selectChatHead(String key);

    /**
     * Should be called when measuring of the container is done.
     * Typically called from onMeasure or onLayout
     * Only when {@link ChatHeadContainer#getContainerHeight()} && {@link ChatHeadContainer#getContainerWidth()} returns a positive value will arrangements start working
     *
     * @param height
     * @param width
     */
    void onMeasure(int height, int width);

    /**
     * Adds and returns the created chat head
     *
     * @return
     */
    ChatHead addChatHead(String key, boolean animated);


    void reloadDrawable(String key);

    /**
     * @param userTriggered if true this means that the chat head was removed by user action (drag to bottom)
     */
    void removeAllChatHeads(boolean userTriggered);

    /**
     * Removed the chat head and calls the onChatHeadRemoved listener
     *
     * @param key
     * @param userTriggered if true this means that the chat head was removed by user action (drag to bottom)
     * @return
     */
    boolean removeChatHead(String key, boolean userTriggered);

    ChatHeadOverlayView getOverlayView();

    ChatHeadArrangement getArrangement(Class<? extends ChatHeadArrangement> arrangementType);

    void setArrangement(Class<? extends ChatHeadArrangement> arrangement, Bundle extras);

    void setArrangement(Class<? extends ChatHeadArrangement> arrangement, Bundle extras, boolean animated);

    void setOnChatHeadSelectedListener(ChatHeadSelectedListener chatHeadSelectedListener);

    boolean onItemSelected(ChatHeadsContainer chatHeadsContainer);

    boolean onChatHeadSelected(ChatHead chatHead);

    void recreateView(String key);

    SpringSystem getSpringSystem();

    View attachView(ChatHead activeChatHead, ViewGroup parent);

    void detachView(ChatHead chatHead, ViewGroup parent);

    void removeView(ChatHead chatHead, ViewGroup parent);

    ChatHeadConfig getConfig();

    void setConfig(ChatHeadConfig config);

    void hideOverlayView(boolean animated);

    void showOverlayView(boolean animated);

    void bringToFront(ChatHead chatHead);

    ContentView getContentView();

    ChatHeadContainer getWindowManagerContainer();

    DisplayMetrics getDisplayMetrics();

    int getMaxWidth();

    int getMaxHeight();

    Context getContext();

    Parcelable onSaveInstanceState(Parcelable superState);

    void onRestoreInstanceState(Parcelable state);

    FloatingViewPreferences getFloatingViewPreferences();

    ChatHeadsContainer getChatHeadsContainer();
}