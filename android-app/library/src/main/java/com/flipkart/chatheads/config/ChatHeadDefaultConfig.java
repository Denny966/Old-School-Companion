package com.flipkart.chatheads.config;

import android.content.Context;
import android.graphics.Point;

import com.flipkart.chatheads.utils.ChatHeadUtils;

public class ChatHeadDefaultConfig extends ChatHeadConfig {
    public ChatHeadDefaultConfig(Context context) {
        int diameter = 50;
        setHeadHeight(ChatHeadUtils.dpToPx(context, diameter));
        setHeadWidth(ChatHeadUtils.dpToPx(context, diameter));
        setHeadHorizontalSpacing(ChatHeadUtils.dpToPx(context, 10));
        setHeadVerticalSpacing(ChatHeadUtils.dpToPx(context, 5));
        setInitialPosition(new Point(0, 100));
    }
}
