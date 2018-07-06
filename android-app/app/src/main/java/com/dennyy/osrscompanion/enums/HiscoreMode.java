package com.dennyy.osrscompanion.enums;


public enum HiscoreMode {
    NORMAL(1), IRONMAN(2), HCIM(3), UIM(4), DMM(5), SDMM(6);
    private int value;

    HiscoreMode(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static HiscoreMode fromValue(int id) {
        for (HiscoreMode type : HiscoreMode.values()) {
            if (type.getValue() == id) {
                return type;
            }
        }
        return HiscoreMode.NORMAL;
    }
}


