package com.dennyy.oldschoolcompanion.models.Notes;

public class NoteChangeEvent {
    public final String note;
    public final boolean isFloatingView;

    public NoteChangeEvent(String note, boolean isFloatingView) {
        this.note = note;
        this.isFloatingView = isFloatingView;
    }
}