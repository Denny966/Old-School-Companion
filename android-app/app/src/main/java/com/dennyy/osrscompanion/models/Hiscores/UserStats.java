package com.dennyy.osrscompanion.models.Hiscores;


import com.dennyy.osrscompanion.enums.HiscoreType;

import java.io.Serializable;

public class UserStats implements Serializable {

    public String rsn;
    public String stats;
    public int hiscoreType = HiscoreType.NORMAL.getValue();
    public long dateModified;

    public UserStats(String rsn, String stats, HiscoreType hiscoreType) {
        this.rsn = rsn;
        this.stats = stats;
        this.hiscoreType = hiscoreType.getValue();
    }
}
