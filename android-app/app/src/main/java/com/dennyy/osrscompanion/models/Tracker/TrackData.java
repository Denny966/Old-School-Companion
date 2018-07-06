package com.dennyy.osrscompanion.models.Tracker;

import com.dennyy.osrscompanion.enums.TrackDurationType;

import java.io.Serializable;

public class TrackData implements Serializable {
    public String rsn;
    public TrackDurationType durationType;
    public String data;
    public long dateModified;
}
