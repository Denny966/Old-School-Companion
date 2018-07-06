package com.dennyy.osrscompanion.enums;

public enum CompareMode {
    LEVEL(1), RANK(2), EXP(3);
    private int value;

    CompareMode(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static CompareMode fromValue(int id) {
        for (CompareMode type : CompareMode.values()) {
            if (type.getValue() == id) {
                return type;
            }
        }
        return LEVEL;
    }
}
