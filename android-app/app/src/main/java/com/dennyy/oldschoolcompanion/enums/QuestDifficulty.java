package com.dennyy.oldschoolcompanion.enums;

public enum QuestDifficulty {
    NOVICE(0, "Novice"), INTERMEDIATE(1, "Intermediate"), EXPERIENCED(2, "Experienced"), MASTER(3, "Master"), GRANDMASTER(4, "Grandmaster"), SPECIAL(5, "Special");
    public final int value;
    public final String name;

    QuestDifficulty(int value, String name) {
        this.value = value;
        this.name = name;
    }

    public static QuestDifficulty fromValue(int value) {
        for (QuestDifficulty type : QuestDifficulty.values()) {
            if (type.value == value) {
                return type;
            }
        }
        return QuestDifficulty.NOVICE;
    }
}
