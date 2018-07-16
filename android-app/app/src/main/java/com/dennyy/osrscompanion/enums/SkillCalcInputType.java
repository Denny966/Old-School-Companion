package com.dennyy.osrscompanion.enums;

public enum SkillCalcInputType {
    CURRENT_LVL(1), TARGET_LVL(2), CURRENT_EXP(3), TARGET_EXP(4);
    private int value;

    SkillCalcInputType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
