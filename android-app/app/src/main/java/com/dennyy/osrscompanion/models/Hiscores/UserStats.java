package com.dennyy.osrscompanion.models.Hiscores;


import com.dennyy.osrscompanion.enums.HiscoreMode;

import java.io.Serializable;

public class UserStats implements Serializable {

    public String rsn;
    public String stats;
    public int hiscoreType = HiscoreMode.NORMAL.getValue();
    public long dateModified;

    public UserStats(String rsn, String stats, HiscoreMode hiscoreMode) {
        this.rsn = rsn;
        this.stats = stats;
        this.hiscoreType = hiscoreMode.getValue();
    }
}
