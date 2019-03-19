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
import com.flipkart.chatheads.custom.ChatHeadCloseButton;
import com.flipkart.chatheads.custom.ContentView;

public interface ChatHeadManager {

    ChatHeads getChatHeads();

    void setViewAdapter(ChatHeadViewAdapter chatHeadViewAdapter);

    ChatHeadCloseButton getCloseButton();

    void captureChatHeads();

    ChatHeadArrangement getActiveArrangement();

    void onMeasure(int height, int width);

    ChatHead addChatHead(String key, boolean animated);

    void reloadDrawable(String key);

    void removeAllChatHeads(boolean userTriggered);

    ChatHeadOverlayView getOverlayView();

    void setArrangement(Class<? extends ChatHeadArrangement> arrangement, Bundle extras);

    void setArrangement(Class<? extends ChatHeadArrangement> arrangement, Bundle extras, boolean animated);

    boolean onItemSelected(ChatHeadsContainer chatHeadsContainer);

    boolean onChatHeadSelected(ChatHead chatHead);

    SpringSystem getSpringSystem();

    View attachView(ChatHead activeChatHead, ViewGroup parent);

    void detachView(ChatHead chatHead, ViewGroup parent);

    void removeView(ChatHead chatHead, ViewGroup parent);

    ChatHeadConfig getConfig();

    void setConfig(ChatHeadConfig config);

    void hideOverlayView(boolean animated);

    void showOverlayView(boolean animated);

    ContentView getContentView();

    ChatHeadContainer getWindowManagerContainer();

    DisplayMetrics getDisplayMetrics();

    int getMaxWidth();

    int getMaxHeight();

    Context getContext();

    Parcelable onSaveInstanceState(Parcelable superState);

    void onRestoreInstanceState(Parcelable state);

    void onSizeChanged(int w, int h, int oldw, int oldh);

    FloatingViewPreferences getFloatingViewPreferences();

    ChatHeadsContainer getChatHeadsContainer();

    int[] getChatHeadCoordsForCloseButton(ChatHeadsContainer chatHead);

    double getDistanceCloseButtonFromHead(float rawX, float rawY);
}