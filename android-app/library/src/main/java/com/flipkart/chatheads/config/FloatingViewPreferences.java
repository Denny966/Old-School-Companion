package com.flipkart.chatheads.config;

public class FloatingViewPreferences {
    public final boolean startRightSide;
    public final boolean alignFloatingViewsLeft;
    public final int alignmentMargin;
    public final float inactiveAlpha;
    public final int floatingViewCount;
    public final int sizeDp;
    public final boolean isHardwareAccelerated;
    public final boolean showCloseButton;
    public final float overlayAlpha;

    public FloatingViewPreferences(boolean startRightSide, boolean alignFloatingViewsLeft, int alignmentMargin,
                                   float inactiveAlpha, int floatingViewCount, int sizeDp, boolean isHardwareAccelerated,
                                   boolean showCloseButton, float overlayAlpha) {
        this.startRightSide = startRightSide;
        this.alignFloatingViewsLeft = alignFloatingViewsLeft;
        this.alignmentMargin = alignmentMargin;
        this.inactiveAlpha = inactiveAlpha;
        this.floatingViewCount = floatingViewCount;
        this.sizeDp = sizeDp;
        this.isHardwareAccelerated = isHardwareAccelerated;
        this.showCloseButton = showCloseButton;
        this.overlayAlpha = overlayAlpha;
    }
}