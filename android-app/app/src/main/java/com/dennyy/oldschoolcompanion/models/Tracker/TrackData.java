package com.dennyy.oldschoolcompanion.models.Tracker;

import com.dennyy.oldschoolcompanion.enums.TrackDurationType;

import java.io.Serializable;

public class TrackData implements Serializable {
    public String rsn;
    public TrackDurationType durationType;
    public String data;
    public long dateModified;
}
