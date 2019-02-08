package com.dennyy.oldschoolcompanion.models.Hiscores;


import com.dennyy.oldschoolcompanion.enums.HiscoreType;

import java.io.Serializable;

public class UserStats implements Serializable {

    public String rsn;
    public String stats;
    public int hiscoreType;
    public long dateModified;

    public UserStats(String rsn, String stats, HiscoreType hiscoreType) {
        this.rsn = rsn;
        this.stats = stats;
        this.hiscoreType = hiscoreType.getValue();
    }
}
