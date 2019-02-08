package com.dennyy.oldschoolcompanion.enums;

public enum ReloadTimerSource {
    FRAGMENT(1), FLOATINTG_VIEW(2), ANY(3);
    private int value;

    ReloadTimerSource(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static ReloadTimerSource fromValue(int id) {
        for (ReloadTimerSource type : ReloadTimerSource.values()) {
            if (type.getValue() == id) {
                return type;
            }
        }
        return ANY;
    }
}
