package com.flipkart.chatheads;

import java.util.ArrayList;

public class ChatHeads extends ArrayList<ChatHead> {
    public ChatHeads() {
    }

    public ChatHeads(int initialCapacity) {
        super(initialCapacity);
    }

    public ChatHead getByKey(String key) {
        for (ChatHead chatHead : this) {
            if (chatHead.getKey().equals(key))
                return chatHead;
        }

        return null;
    }

    public ChatHead getByHeroIndex(int index) {
        if (index < 0 || index > size() - 1) {
            index = 0;
        }
        return get(index);
    }

    public int getHeroIndex(ChatHead chatHead) {
        return getHeroIndex(chatHead.getKey());
    }

    public int getHeroIndex(String key) {
        int heroIndex = 0;
        for (ChatHead chatHead : this) {
            if (key.equals(chatHead.getKey())) {
                return heroIndex;
            }
            heroIndex++;
        }
        return heroIndex;
    }

    public void updateChatHeadSizes(ChatHead activeChatHead) {
        for (ChatHead chatHead : this) {
            chatHead.setAlpha(1.0f);
            chatHead.setHero(false);
            chatHead.setInactive(true);
        }
        activeChatHead.setHero(true);
        activeChatHead.setInactive(false);
    }

    public ChatHead getNextBestChatHead() {
        ChatHead nextBestChatHead = null;
        for (ChatHead head : this) {
            if (nextBestChatHead == null) {
                nextBestChatHead = head;
            }
        }
        return nextBestChatHead;
    }

    public ArrayList<String> getKeys() {
        ArrayList<String> keys = new ArrayList<>();
        for (ChatHead chatHead : this) {
            keys.add(chatHead.getKey());
        }
        return keys;
    }
}
