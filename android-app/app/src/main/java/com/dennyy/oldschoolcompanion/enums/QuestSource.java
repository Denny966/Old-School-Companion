package com.dennyy.oldschoolcompanion.enums;

public enum QuestSource {
    RSWIKI(0, "RSWiki"), RUNEHQ(1, "RuneHQ");
    private final int value;
    private final String name;

    QuestSource(int value, String name) {
        this.value = value;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public int getValue() {
        return value;
    }

    public static QuestSource fromValue(int value) {
        for (QuestSource type : QuestSource.values()) {
            if (type.getValue() == value) {
                return type;
            }
        }
        return QuestSource.RSWIKI;
    }

    public static QuestSource fromName(String name) {
        for (QuestSource type : QuestSource.values()) {
            if (type.getName().equalsIgnoreCase(name)) {
                return type;
            }
        }
        return QuestSource.RSWIKI;
    }
}
