package com.dennyy.oldschoolcompanion.enums;


public enum TreasureTrailType {
    ANAGRAM("ANAGRAM"), CIPHER("CIPHER"), CHALLENGE("CHALLENGE"), CRYPTIC("CRYPTIC"), COORDINATES("COORDS");
    private String value;

    TreasureTrailType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static TreasureTrailType fromString(String inputType) {
        for (TreasureTrailType type : TreasureTrailType.values()) {
            if (type.getValue().toLowerCase().equals(inputType.toLowerCase())) {
                return type;
            }
        }
        return TreasureTrailType.ANAGRAM;
    }
}
