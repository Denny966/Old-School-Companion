package com.flipkart.chatheads.interfaces;

import com.flipkart.chatheads.ChatHead;

public interface ChatHeadSelectedListener {
    /**
     * Will be called whenever a chat head is clicked.
     * If you return false from here, the arrangement will continue whatever its supposed to do.
     * If you return true from here, the arrangement will stop the action it normally does after click.
     *
     * @param key
     * @param chatHead
     * @return true if you want to take control. false if you dont care.
     */
    boolean onChatHeadSelected(String key, ChatHead chatHead);
}