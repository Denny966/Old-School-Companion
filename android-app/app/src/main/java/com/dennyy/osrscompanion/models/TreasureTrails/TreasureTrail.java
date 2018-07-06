package com.dennyy.osrscompanion.models.TreasureTrails;

import com.dennyy.osrscompanion.enums.TreasureTrailType;

import java.io.Serializable;

public class TreasureTrail implements Serializable {
    public String text;
    public TreasureTrailType type;

    // optional
    public String answer;
    public String npc;
    public String location;

    @Override
    public String toString() {
        return text;
    }
}
