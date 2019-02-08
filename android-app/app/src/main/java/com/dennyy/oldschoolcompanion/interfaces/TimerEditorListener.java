package com.dennyy.oldschoolcompanion.interfaces;

public interface TimerEditorListener {
    void onTimerEditorSave(String title, String description, int hours, int minutes, int seconds, boolean repeated);

    void onTimerEditorCancel();
}
