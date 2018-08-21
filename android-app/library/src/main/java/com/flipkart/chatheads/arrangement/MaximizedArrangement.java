package com.flipkart.chatheads.arrangement;

import android.annotation.TargetApi;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.util.ArrayMap;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.rebound.SimpleSpringListener;
import com.facebook.rebound.Spring;
import com.flipkart.chatheads.ChatHead;
import com.flipkart.chatheads.ChatHeads;
import com.flipkart.chatheads.ChatHeadsContainer;
import com.flipkart.chatheads.config.ChatHeadConfig;
import com.flipkart.chatheads.custom.ContentView;
import com.flipkart.chatheads.interfaces.ChatHeadManager;
import com.flipkart.chatheads.utils.ChatHeadUtils;
import com.flipkart.chatheads.utils.SpringConfigsHolder;

import java.util.Map;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class MaximizedArrangement extends ChatHeadArrangement {
    public static final String BUNDLE_HERO_INDEX_KEY = "hero_index";
    private static double MAX_DISTANCE_FROM_ORIGINAL;
    private static int MIN_VELOCITY_TO_POSITION_BACK;
    private final Map<ChatHead, Point> positions = new ArrayMap<>();
    private Point chatHeadsContainerPosition;
    private ChatHeadManager manager;
    private ChatHeads chatHeads;
    private int maxWidth;
    private int maxHeight;
    private ChatHead currentChatHead;
    private ContentView arrowLayout;
    private int maxDistanceFromOriginal;
    private int topPadding;
    private boolean isTransitioning = false;
    private Bundle extras;
    private ChatHeadsContainer chatHeadsContainer;

    public MaximizedArrangement(ChatHeadManager manager) {
        this.manager = manager;
    }


    @Override
    public void setContainer(ChatHeadManager container) {
        this.manager = container;
    }

    @Override
    public void onActivate(ChatHeadManager chatHeadManager, Bundle extras, int maxWidth, int maxHeight, boolean animated) {
        isTransitioning = true;
        this.manager = chatHeadManager;
        this.chatHeadsContainer = chatHeadManager.getChatHeadsContainer();
        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;
        this.chatHeads = chatHeadManager.getChatHeads();

        MIN_VELOCITY_TO_POSITION_BACK = ChatHeadUtils.dpToPx(chatHeadManager.getDisplayMetrics(), 50);
        MAX_DISTANCE_FROM_ORIGINAL = ChatHeadUtils.dpToPx(chatHeadManager.getContext(), 10);
        int heroIndex = 0;
        this.extras = extras;
        if (extras != null) {
            heroIndex = extras.getInt(BUNDLE_HERO_INDEX_KEY, -1);
        }
        currentChatHead = chatHeads.getByHeroIndex(heroIndex);

        maxDistanceFromOriginal = (int) MAX_DISTANCE_FROM_ORIGINAL;

        int spacing = chatHeadManager.getConfig().getHeadHorizontalSpacing();
        int widthPerHead = chatHeadManager.getConfig().getHeadWidth();
        topPadding = ChatHeadUtils.dpToPx(chatHeadManager.getContext(), 5);

        chatHeads.updateChatHeadSizes(currentChatHead);
        for (int i = 0; i < chatHeads.size(); i++) {
            ChatHead chatHead = chatHeads.get(i);
            int xPos = (i * (widthPerHead + spacing));
            positions.put(chatHead, new Point(xPos, topPadding));
        }
        chatHeadsContainerPosition = new Point(0, topPadding);
        Spring horizontalSpring = chatHeadsContainer.getHorizontalSpring();
        horizontalSpring.setAtRest();
        horizontalSpring.setSpringConfig(SpringConfigsHolder.NOT_DRAGGING);
        horizontalSpring.setEndValue(0);
        if (!animated) {
            horizontalSpring.setCurrentValue(0);
        }
        Spring verticalSpring = chatHeadsContainer.getVerticalSpring();
        verticalSpring.setAtRest();
        verticalSpring.setSpringConfig(SpringConfigsHolder.NOT_DRAGGING);
        verticalSpring.setEndValue(topPadding);
        if (!animated) {
            verticalSpring.setCurrentValue(topPadding);
        }

        chatHeadManager.showOverlayView(animated);
        selectChatHead(currentChatHead);
        manager.getChatHeadsContainer().restoreWidth();

        chatHeadsContainer.getVerticalSpring().addListener(new SimpleSpringListener() {
            @Override
            public void onSpringAtRest(Spring spring) {
                super.onSpringAtRest(spring);
                if (isTransitioning) {
                    isTransitioning = false;
                }
                chatHeadsContainer.getVerticalSpring().removeListener(this);
            }
        });
        chatHeadsContainer.getHorizontalSpring().addListener(new SimpleSpringListener() {
            @Override
            public void onSpringAtRest(Spring spring) {
                super.onSpringAtRest(spring);
                if (isTransitioning) {
                    isTransitioning = false;
                }
                chatHeadsContainer.getHorizontalSpring().removeListener(this);
            }
        });
    }


    @Override
    public void onDeactivate(int maxWidth, int maxHeight) {
        if (currentChatHead != null) {
            manager.detachView(currentChatHead, getArrowLayout());
        }
        hideView();
        manager.hideOverlayView(true);
        positions.clear();
    }


    @Override
    public boolean handleTouchUp(ChatHeadsContainer activeChatHead, int xVelocity, int yVelocity, Spring activeHorizontalSpring, Spring activeVerticalSpring, boolean wasDragging) {
        return false;
    }

    public boolean handleChatHeadTouchUp(ChatHead activeChathead, int xVelocity, int yVelocity, Spring activeHorizontalSpring, Spring activeVerticalSpring, boolean wasDragging) {
        if (xVelocity == 0 && yVelocity == 0) {
            // this is a hack. If both velocities are 0, onSprintUpdate is not called and the chat head remains whereever it is
            // so we give a a negligible velocity to artificially fire onSpringUpdate
            xVelocity = 1;
            yVelocity = 1;
        }

        activeHorizontalSpring.setVelocity(xVelocity);
        activeVerticalSpring.setVelocity(yVelocity);


        if (wasDragging) {
            return true;
        }
        else {
            if (activeChathead != currentChatHead) {
                boolean handled = manager.onChatHeadSelected(activeChathead);
                if (!handled) {
                    selectTab(activeChathead);
                    return true;
                }
            }
            boolean handled = manager.onChatHeadSelected(activeChathead);
            if (!handled) {
                deactivate();
            }
            return handled;
        }
    }

    private void selectTab(final ChatHead activeChatHead) {
        if (currentChatHead != activeChatHead) {
            detach(currentChatHead);
            currentChatHead = activeChatHead;
            chatHeads.updateChatHeadSizes(currentChatHead);
        }
        pointTo(activeChatHead);
        showOrHideView();
    }

    private void detach(ChatHead chatHead) {
        manager.detachView(chatHead, getArrowLayout());
    }

    private void positionToOriginal(ChatHeadsContainer chatHeadsContainer, Spring activeHorizontalSpring, Spring activeVerticalSpring) {
        if (chatHeadsContainer.getState() == ChatHeadsContainer.State.FREE) {
            activeHorizontalSpring.setSpringConfig(SpringConfigsHolder.NOT_DRAGGING);
            activeHorizontalSpring.setVelocity(0);
            activeHorizontalSpring.setEndValue(0);
            activeVerticalSpring.setSpringConfig(SpringConfigsHolder.NOT_DRAGGING);
            activeVerticalSpring.setVelocity(0);
            activeVerticalSpring.setEndValue(0);
        }
    }

    @Override
    public void onSpringUpdate(ChatHeadsContainer chatHeadsContainer, boolean isDragging, int maxWidth, int maxHeight, Spring spring, Spring activeHorizontalSpring, Spring activeVerticalSpring, int totalVelocity) {
        /** Bounds Check **/
        if (spring == activeHorizontalSpring && !isDragging) {
            double xPosition = activeHorizontalSpring.getCurrentValue();
            if (xPosition + manager.getConfig().getHeadWidth() > maxWidth && activeHorizontalSpring.getSpringConfig() != SpringConfigsHolder.NOT_DRAGGING && !activeHorizontalSpring.isOvershooting()) {
                positionToOriginal(chatHeadsContainer, activeHorizontalSpring, activeVerticalSpring);
            }
            if (xPosition < 0 && activeHorizontalSpring.getSpringConfig() != SpringConfigsHolder.NOT_DRAGGING && !activeHorizontalSpring.isOvershooting()) {
                positionToOriginal(chatHeadsContainer, activeHorizontalSpring, activeVerticalSpring);
            }
        }
        else if (spring == activeVerticalSpring && !isDragging) {
            double yPosition = activeVerticalSpring.getCurrentValue();

            if (yPosition + manager.getConfig().getHeadHeight() > maxHeight && activeHorizontalSpring.getSpringConfig() != SpringConfigsHolder.NOT_DRAGGING && !activeHorizontalSpring.isOvershooting()) {
                positionToOriginal(chatHeadsContainer, activeHorizontalSpring, activeVerticalSpring);
            }
            if (yPosition < 0 && activeHorizontalSpring.getSpringConfig() != SpringConfigsHolder.NOT_DRAGGING && !activeHorizontalSpring.isOvershooting()) {
                positionToOriginal(chatHeadsContainer, activeHorizontalSpring, activeVerticalSpring);
            }
        }

        /** position it back **/
        if (!isDragging && totalVelocity < MIN_VELOCITY_TO_POSITION_BACK && activeHorizontalSpring.getSpringConfig() == SpringConfigsHolder.DRAGGING) {
            positionToOriginal(chatHeadsContainer, activeHorizontalSpring, activeVerticalSpring);

        }

        showOrHideView();
    }

    private void showOrHideView() {
        if (chatHeadsContainerPosition != null) {
            double dx = chatHeadsContainer.getHorizontalSpring().getCurrentValue() - chatHeadsContainerPosition.x;
            double dy = chatHeadsContainer.getVerticalSpring().getCurrentValue() - chatHeadsContainerPosition.y;
            double distanceFromOriginal = Math.hypot(dx, dy);
            if (distanceFromOriginal < maxDistanceFromOriginal) {
                showView(dx, dy, distanceFromOriginal);
            }
            else {
                hideView();
            }
        }

    }

    private ContentView getArrowLayout() {
        if (arrowLayout == null) {
            arrowLayout = manager.getContentView();
        }
        return arrowLayout;
    }

    private boolean isViewHidden() {
        ContentView arrowLayout = getArrowLayout();
        if (arrowLayout != null) {
            return arrowLayout.getVisibility() == View.GONE;
        }
        return true;
    }

    private void hideView() {
        ContentView arrowLayout = getArrowLayout();
        arrowLayout.setVisibility(View.GONE);
    }

    private void showView(double dx, double dy, double distanceFromOriginal) {
        ContentView contentView = getArrowLayout();
        contentView.setVisibility(View.VISIBLE);
        contentView.setTranslationX((float) dx);
        contentView.setTranslationY((float) dy);
        contentView.setAlpha(1f - ((float) distanceFromOriginal / (float) maxDistanceFromOriginal));
    }

    public static void sendViewToBack(final View child) {
        final ViewGroup parent = (ViewGroup) child.getParent();
        if (null != parent && parent.indexOfChild(child) != 0) {
            parent.removeView(child);
            parent.addView(child, 0);
        }
    }

    private void pointTo(ChatHead activeChatHead) {
        ContentView contentView = getArrowLayout();
        contentView.removeAllViews();
        manager.attachView(activeChatHead, contentView);
        sendViewToBack(manager.getOverlayView());
        Point point = positions.get(activeChatHead);
        if (point != null) {
            int padding = manager.getConfig().getHeadVerticalSpacing();
            contentView.pointTo(point.x + manager.getConfig().getHeadWidth() / 2, point.y + manager.getConfig().getHeadHeight() + padding);
        }
    }


    @Override
    public void onChatHeadAdded(final ChatHead chatHead, final boolean animated) {
        //we post so that chat head measurement is done
        Spring spring = chatHead.getHorizontalSpring();
        spring.setCurrentValue(maxWidth).setAtRest();
        spring = chatHead.getVerticalSpring();
        spring.setCurrentValue(topPadding).setAtRest();
        onActivate(manager, getBundleWithHero(), maxWidth, maxHeight, animated);
    }

    @Override
    public void onChatHeadRemoved(ChatHead removed) {
        manager.detachView(removed, getArrowLayout());
        manager.removeView(removed, getArrowLayout());
        positions.remove(removed);
        boolean isEmpty = false;
        if (currentChatHead == removed) {
            ChatHead nextBestChatHead = chatHeads.getNextBestChatHead();
            if (nextBestChatHead != null) {
                isEmpty = false;
                selectTab(nextBestChatHead);
            }
            else {
                isEmpty = true;
            }
        }
        if (!isEmpty) {
            onActivate(manager, getBundleWithHero(), maxWidth, maxHeight, true);
        }
        else {
            deactivate();
        }
    }

    @Override
    public void selectChatHead(final ChatHead chatHead) {
        selectTab(chatHead);
    }

    private Bundle getBundleWithHero() {
        Bundle bundle = extras;
        if (bundle == null) {
            bundle = new Bundle();
        }
        bundle.putInt(MinimizedArrangement.BUNDLE_HERO_INDEX_KEY, chatHeads.getHeroIndex(currentChatHead));
        return bundle;
    }

    private void deactivate() {
        manager.setArrangement(MinimizedArrangement.class, getBundleWithHero());
        hideView();
    }

    @Override
    public void onConfigChanged(ChatHeadConfig newConfig) {

    }

    @Override
    public Bundle getRetainBundle() {
        return getBundleWithHero();
    }

    @Override
    public boolean canDrag(ChatHeadsContainer chatHead) {
        return false;
    }

    @Override
    public void bringToFront(final ChatHead chatHead) {
        //nothing to do, everything is in front.
        selectChatHead(chatHead);
    }

    @Override
    public void onReloadFragment(ChatHead chatHead) {
        if (currentChatHead != null && chatHead == currentChatHead) {
            manager.attachView(chatHead, getArrowLayout());
        }
    }
}