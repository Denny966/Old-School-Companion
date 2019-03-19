package com.flipkart.chatheads.arrangement;

import android.os.Bundle;
import com.facebook.rebound.Spring;
import com.flipkart.chatheads.ChatHead;
import com.flipkart.chatheads.ChatHeadsContainer;
import com.flipkart.chatheads.config.ChatHeadConfig;
import com.flipkart.chatheads.interfaces.ChatHeadManager;

public abstract class ChatHeadArrangement {
    public abstract void setContainer(ChatHeadManager container);

    public abstract void onActivate(ChatHeadManager container, Bundle extras, int maxWidth, int maxHeight, boolean animated);

    public abstract void onDeactivate(int maxWidth, int maxHeight);

    public abstract void onSpringUpdate(ChatHeadsContainer chatHeadsContainer, boolean isDragging, int maxWidth, int maxHeight, Spring spring, Spring activeHorizontalSpring, Spring activeVerticalSpring, int totalVelocity);

    public abstract boolean handleTouchUp(ChatHeadsContainer chatHeadsContainer, int xVelocity, int yVelocity, Spring activeHorizontalSpring, Spring activeVerticalSpring, boolean wasDragging);

    public abstract boolean handleChatHeadTouchUp(ChatHead chatHead, int xVelocity, int yVelocity, Spring activeHorizontalSpring, Spring activeVerticalSpring, boolean wasDragging);

    public abstract void onChatHeadAdded(ChatHead chatHead, boolean animated);

    public abstract void onChatHeadRemoved(ChatHead removed);

    public abstract void selectChatHead(ChatHead chatHead);

    public abstract void onConfigChanged(ChatHeadConfig newConfig);

    public abstract Bundle getRetainBundle();

    public abstract boolean canDrag();

    public abstract boolean shouldShowCloseButton();

    public abstract void onCapture(ChatHeadManager container);
}
