package com.dennyy.oldschoolcompanion.enums;

public enum CombatClass {
    MELEE(1), RANGE(2), MAGE(3);
    private int value;

    CombatClass(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public String getString() {
        switch (fromValue(value)) {
            case RANGE:
                return "Range";
            case MAGE:
                return "Mage";
            default:
                return "Melee";
        }
    }

    public static CombatClass fromValue(int id) {
        for (CombatClass type : CombatClass.values()) {
            if (type.getValue() == id) {
                return type;
            }
        }
        return MELEE;
    }
}
