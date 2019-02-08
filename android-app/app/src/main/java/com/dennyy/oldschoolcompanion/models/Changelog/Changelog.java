package com.dennyy.oldschoolcompanion.models.Changelog;

public class Changelog {
    public final String versionName;
    public final ChangelogEntries entries;
    public final int entriesSize;

    public Changelog(String versionName, ChangelogEntries entries) {
        this.versionName = versionName;
        this.entries = entries;
        entriesSize = entries.size();
    }
}