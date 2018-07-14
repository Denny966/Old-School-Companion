package com.dennyy.osrscompanion.enums;


public enum HiscoreType {
    NORMAL(1), IRONMAN(2), HCIM(3), UIM(4), DMM(5), SDMM(6);
    private int value;

    HiscoreType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static HiscoreType fromValue(int id) {
        for (HiscoreType type : HiscoreType.values()) {
            if (type.getValue() == id) {
                return type;
            }
        }
        return HiscoreType.NORMAL;
    }
}


