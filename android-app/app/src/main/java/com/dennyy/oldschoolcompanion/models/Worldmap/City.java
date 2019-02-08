package com.dennyy.oldschoolcompanion.models.Worldmap;

import android.graphics.Point;

public class City {
    public String name;
    public Point location;

    public City(String name, Point location) {
        this.name = name;
        this.location = location;
    }
}
