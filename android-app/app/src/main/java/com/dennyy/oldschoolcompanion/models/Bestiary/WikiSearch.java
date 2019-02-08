package com.dennyy.oldschoolcompanion.models.Bestiary;

public class WikiSearch {
    public final int id;
    public final String name;

    public WikiSearch(int id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
