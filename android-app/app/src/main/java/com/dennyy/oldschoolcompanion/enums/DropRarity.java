package com.dennyy.oldschoolcompanion.enums;

public enum DropRarity {
    ALWAYS("Always"), COMMON("Common"), UNCOMMON("Uncommon"), RARE("Rare"), VERY_RARE("Very rare"), UNKNOWN("Unknown");
    private String value;

    DropRarity(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static DropRarity fromString(String inputType) {
        for (DropRarity type : DropRarity.values()) {
            if (type.getValue().toLowerCase().equals(inputType.toLowerCase())) {
                return type;
            }
        }
        return DropRarity.UNKNOWN;
    }
}
