package com.flipkart.chatheads.config;

public class FloatingViewPreferences {
    private boolean startRightSide;
    private boolean alignFloatingViewsLeft;
    private int alignmentMargin;
    private float inactiveAlpha;

    public FloatingViewPreferences(boolean startRightSide, boolean alignFloatingViewsLeft, int alignmentMargin, float inactiveAlpha) {
        this.startRightSide = startRightSide;
        this.alignFloatingViewsLeft = alignFloatingViewsLeft;
        this.alignmentMargin = alignmentMargin;
        this.inactiveAlpha = inactiveAlpha;
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

}
