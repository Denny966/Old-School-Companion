package com.flipkart.chatheads.config;

import android.graphics.Point;

public class ChatHeadConfig {
    private int headHeight;
    private int headWidth;
    private int headHorizontalSpacing;
    private int headVerticalSpacing;
    private Point initialPosition;
    private int initialHeadWidth;
    public final static float inactiveSize = 0.8f;

    private int closeButtonWidth;
    private int closeButtonHeight;
    private int closeButtonBottomMargin;

    public int getHeadHeight() {
        return headHeight;
    }

    public void setHeadHeight(int headHeight) {
        this.headHeight = headHeight;
    }

    public int getHeadWidth() {
        return headWidth;
    }

    public void setHeadWidth(int headWidth) {
        this.headWidth = headWidth;
        this.initialHeadWidth = headWidth;
    }

    public int getHeadHorizontalSpacing() {
        return headHorizontalSpacing;
    }

    public void setHeadHorizontalSpacing(int headHorizontalSpacing) {
        this.headHorizontalSpacing = headHorizontalSpacing;
    }

    public int getHeadVerticalSpacing() {
        return headVerticalSpacing;
    }

    public void setHeadVerticalSpacing(int headVerticalSpacing) {
        this.headVerticalSpacing = headVerticalSpacing;
    }

    public Point getInitialPosition() {
        return initialPosition;
    }

    public void setInitialPosition(Point initialPosition) {
        this.initialPosition = initialPosition;
    }

    public int getInitialHeadWidth() {
        return initialHeadWidth;
    }

    public int getCloseButtonWidth() {
        return closeButtonWidth;
    }

    public void setCloseButtonWidth(int closeButtonWidth) {
        this.closeButtonWidth = closeButtonWidth;
    }

    public int getCloseButtonHeight() {
        return closeButtonHeight;
    }

    public void setCloseButtonHeight(int closeButtonHeight) {
        this.closeButtonHeight = closeButtonHeight;
    }

    public int getCloseButtonBottomMargin() {
        return closeButtonBottomMargin;
    }

    public void setCloseButtonBottomMargin(int closeButtonBottomMargin) {
        this.closeButtonBottomMargin = closeButtonBottomMargin;
    }
}
