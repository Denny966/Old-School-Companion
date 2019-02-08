package com.dennyy.oldschoolcompanion.enums;

public enum QuestSortType {
    NAME(0, "Name"), LENGTH(1, "Length"), DIFFICULTY(2, "Difficulty"), QP(3, "Quest points"), MEMBERS(4, "Members"), COMPLETION(5, "Completion");
    private final int value;
    private final String name;

    QuestSortType(int value, String name) {
        this.value = value;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public int getValue() {
        return value;
    }

    public static QuestSortType fromValue(int value) {
        for (QuestSortType type : QuestSortType.values()) {
            if (type.getValue() == value) {
                return type;
            }
        }
        return QuestSortType.NAME;
    }
}
