package com.dennyy.oldschoolcompanion.models.TreasureTrails;

import com.dennyy.oldschoolcompanion.enums.TreasureTrailType;

import java.io.Serializable;

public class TreasureTrail implements Serializable {
    public String text;
    public TreasureTrailType type;

    // optional
    public String answer;
    public String npc;
    public String location;

    public String getCoordinatesFormatted() {
        if (type == TreasureTrailType.COORDINATES && text.length() == 10) {
            String firstDirection = text.substring(4, 5).equalsIgnoreCase("n") ? "north" : "south";
            String secondDirection = text.substring(9, 10).equalsIgnoreCase("e") ? "east" : "west";
            String formattedCoords = String.format("%s degrees %s minutes %s, %s degrees %s minutes %s", text.substring(0, 2), text.substring(2, 4), firstDirection, text.substring(5, 7), text.substring(7, 9), secondDirection);
            return formattedCoords;
        }
        return "";
    }

    private String getCoordinatesFormattedShort() {
        if (type == TreasureTrailType.COORDINATES && text.length() == 10) {
            String formattedCoords = String.format("%s.%s, %s.%s", text.substring(0, 2), text.substring(2, 5), text.substring(5, 7), text.substring(7, 10));
            return formattedCoords.toUpperCase();
        }
        return "";
    }

    public String getCoordinatesFormattedForUrl() {
        return getCoordinatesFormattedShort().replace(", ","_");
    }

    public boolean containsCoordinates(String search) {
        return type == TreasureTrailType.COORDINATES && (getCoordinatesFormatted().toLowerCase().contains(search.toLowerCase()) || getCoordinatesFormattedShort().toLowerCase().contains(search.toLowerCase()));
    }

    @Override
    public String toString() {
        return text;
    }
}
