package com.dennyy.osrscompanion.enums;


public enum GeGraphDays {
    WEEK(7), MONTH(30), QUARTER(90), ALL(180);
    private int value;

    GeGraphDays(int value) {
        this.value = value;
    }

    public int getDays() {
        return value;
    }

    public static GeGraphDays fromDays(int days) {
        for (GeGraphDays type : GeGraphDays.values()) {
            if (type.getDays() == days) {
                return type;
            }
        }
        return GeGraphDays.ALL;
    }
}
