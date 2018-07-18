package com.flipkart.chatheads.config;

import android.content.Context;
import android.graphics.Point;

import com.flipkart.chatheads.utils.ChatHeadUtils;

/**
 * Created by kiran.kumar on 06/05/15.
 */
public class ChatHeadDefaultConfig extends ChatHeadConfig {
    public ChatHeadDefaultConfig(Context context) {
        int diameter = 50;
        setHeadHeight(ChatHeadUtils.dpToPx(context, diameter));
        setHeadWidth(ChatHeadUtils.dpToPx(context, diameter));
        setHeadHorizontalSpacing(ChatHeadUtils.dpToPx(context, 10));
        setHeadVerticalSpacing(ChatHeadUtils.dpToPx(context, 5));
        setInitialPosition(new Point(0, 100));
        setCloseButtonHidden(true);
        setCloseButtonWidth(ChatHeadUtils.dpToPx(context, diameter + 10));
        setCloseButtonHeight(ChatHeadUtils.dpToPx(context, diameter + 10));
        setCloseButtonBottomMargin(ChatHeadUtils.dpToPx(context, 50));
        setCircularRingWidth(ChatHeadUtils.dpToPx(context, diameter + 5));
        setCircularRingHeight(ChatHeadUtils.dpToPx(context, diameter + 5));
        setMaxChatHeads(5);
    }
}
