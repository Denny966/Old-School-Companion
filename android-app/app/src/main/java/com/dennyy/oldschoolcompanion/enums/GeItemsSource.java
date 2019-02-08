package com.dennyy.oldschoolcompanion.enums;

public enum GeItemsSource {
    BOTH(0, "ge_items_source_both"), P2P(1, "ge_items_source_members"), F2P(2, "ge_items_source_f2p");
    private final int value;
    private final String name;

    GeItemsSource(int value, String name) {
        this.value = value;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public int getValue() {
        return value;
    }

    public static GeItemsSource fromValue(int value) {
        for (GeItemsSource type : GeItemsSource.values()) {
            if (type.getValue() == value) {
                return type;
            }
        }
        return GeItemsSource.BOTH;
    }

    public static GeItemsSource fromName(String name) {
        for (GeItemsSource type : GeItemsSource.values()) {
            if (type.getName().equalsIgnoreCase(name)) {
                return type;
            }
        }
        return GeItemsSource.BOTH;
    }
}
