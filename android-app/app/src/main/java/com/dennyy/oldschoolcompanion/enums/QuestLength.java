package com.dennyy.oldschoolcompanion.enums;

public enum QuestLength {
    SHORT(0, "Short"), MEDIUM(1, "Medium"), LONG(2, "Long"), VERY_LONG(3, "Very long");
    public final int value;
    public final String name;

    QuestLength(int value, String name) {
        this.value = value;
        this.name = name;
    }

    public static QuestLength fromValue(int value) {
        for (QuestLength type : QuestLength.values()) {
            if (type.value == value) {
                return type;
            }
        }
        return QuestLength.SHORT;
    }
}
