package com.flipkart.chatheads.container;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.flipkart.chatheads.ChatHead;
import com.flipkart.chatheads.interfaces.ChatHeadContainer;
import com.flipkart.chatheads.interfaces.ChatHeadManager;
import com.flipkart.chatheads.ChatHeadsContainer;

public abstract class FrameChatHeadContainer implements ChatHeadContainer {

    private HostFrameLayout frameLayout;
    private final Context context;
    private DisplayMetrics displayMetrics = new DisplayMetrics();
    private ChatHeadsContainer scrollView;

    protected ChatHeadManager manager;

    public FrameChatHeadContainer(Context context) {
        this.context = context;
    }

    public ChatHeadManager getManager() {
        return manager;
    }

    @Override
    public void onInitialized(ChatHeadManager manager) {
        this.manager = manager;
        frameLayout = new HostFrameLayout(context, this, manager);
        frameLayout.setFocusable(true);
        frameLayout.setFocusableInTouchMode(true);
        scrollView = new ChatHeadsContainer(manager, manager.getSpringSystem(), context);
        frameLayout.addView(scrollView, scrollView.getLayoutParams());

        //   scrollView.setBackgroundColor(Color.parseColor("red"));
        addContainer(frameLayout, false);
    }

    public Context getContext() {
        return context;
    }

    HostFrameLayout getFrameLayout() {
        return frameLayout;
    }

    public ChatHeadsContainer getChatHeadsContainer() {
        return scrollView;
    }

    @Override
    public void addView(View view, ViewGroup.LayoutParams layoutParams) {
        if (view instanceof ChatHead) {
            scrollView.addChatHead(view, layoutParams);
        }
        else if (frameLayout != null) {
            frameLayout.addView(view, layoutParams);
        }
    }

    @Override
    public void requestLayout() {
        if (frameLayout != null) {
            frameLayout.requestLayout();
        }
    }

    @Override
    public void removeView(View view) {
        if (view instanceof ChatHead) {
            scrollView.removeChatHead(view);
        }
        else if (frameLayout != null) {
            frameLayout.removeView(view);
        }
    }

    @Override
    public ViewGroup.LayoutParams createLayoutParams(int height, int width, int gravity, int bottomMargin) {
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(width, height);
        layoutParams.gravity = gravity;
        layoutParams.bottomMargin = bottomMargin;
        return layoutParams;
    }

    @Override
    public void setViewX(View view, int xPosition) {
        view.setTranslationX(xPosition);
    }

    @Override
    public void setViewY(View view, int yPosition) {
        view.setTranslationY(yPosition);
    }

    @Override
    public DisplayMetrics getDisplayMetrics() {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics;
    }


    @Override
    public int getOrientation() {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display getOrient = windowManager.getDefaultDisplay();
        Point size = new Point();

        getOrient.getSize(size);

        int orientation;
        if (size.x < size.y) {
            orientation = Configuration.ORIENTATION_PORTRAIT;
        }
        else {
            orientation = Configuration.ORIENTATION_LANDSCAPE;
        }
        return orientation;
    }

    @Override
    public int getViewX(View view) {
        return (int) view.getTranslationX();
    }

    @Override
    public int getViewY(View view) {
        return (int) view.getTranslationY();
    }

    @Override
    public void bringToFront(View view) {
        view.bringToFront();
    }

    public abstract void addContainer(View container, boolean focusable);
}
