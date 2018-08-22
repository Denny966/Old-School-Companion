package com.flipkart.chatheads.interfaces;

import com.flipkart.chatheads.ChatHeadsContainer;
import com.flipkart.chatheads.arrangement.ChatHeadArrangement;

import java.io.Serializable;

public interface ChatHeadListener {
    void onChatHeadAdded(String key);

    void onChatHeadRemoved(String key, boolean userTriggered);

    void onChatHeadArrangementChanged(ChatHeadArrangement oldArrangement, ChatHeadArrangement newArrangement);

    void onChatHeadAnimateEnd(ChatHeadsContainer chatHead);

    void onChatHeadAnimateStart(ChatHeadsContainer chatHead);
}
