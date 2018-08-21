package com.flipkart.chatheads.config;

public class FloatingViewPreferences {
    private boolean startRightSide;
    private boolean alignFloatingViewsLeft;
    private int alignmentMargin;
    private float inactiveAlpha;
    private int floatingViewCount;

    public FloatingViewPreferences(boolean startRightSide, boolean alignFloatingViewsLeft, int alignmentMargin, float inactiveAlpha, int floatingViewCount) {
        this.startRightSide = startRightSide;
        this.alignFloatingViewsLeft = alignFloatingViewsLeft;
        this.alignmentMargin = alignmentMargin;
        this.inactiveAlpha = inactiveAlpha;
        this.floatingViewCount = floatingViewCount;
    }

    public boolean startRightSide() {
        return startRightSide;
    }

    public boolean alignFloatingViewsLeft() {
        return alignFloatingViewsLeft;
    }

    public int getAlignmentMargin() {
        return alignmentMargin;
    }

    public float getInactiveAlpha() {
        return inactiveAlpha;
    }

    public int getFloatingViewCount() {
        return floatingViewCount;
    }
}
