package com.dennyy.osrscompanion.enums;

public enum DiaryType {
    EASY("EASY"), MEDIUM("MEDIUM"), HARD("HARD"), ELITE("ELITE");
    private String value;

    DiaryType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static DiaryType fromString(String inputType) {
        for (DiaryType type : DiaryType.values()) {
            if (type.getValue().toLowerCase().equals(inputType.toLowerCase())) {
                return type;
            }
        }
        return null;
    }
}
