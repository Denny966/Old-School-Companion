package com.dennyy.oldschoolcompanion.enums;

import android.widget.Toast;

public enum ToastLength {
    SHORT(Toast.LENGTH_SHORT), LONG(Toast.LENGTH_LONG);
    private int value;

    ToastLength(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
