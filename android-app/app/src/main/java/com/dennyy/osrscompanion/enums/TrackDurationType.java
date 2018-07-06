package com.dennyy.osrscompanion.enums;


public enum TrackDurationType {
    DAY(86400), WEEK(604800), MONTH(2592000), YEAR(31556926);
    private int value;

    TrackDurationType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static TrackDurationType fromValue(int id) {
        for (TrackDurationType type : TrackDurationType.values()) {
            if (type.getValue() == id) {
                return type;
            }
        }
        return TrackDurationType.WEEK;
    }
}
