package com.dennyy.oldschoolcompanion.interfaces;

import com.dennyy.oldschoolcompanion.customviews.SeekBarPreference;

public interface SeekBarPreferenceListener {
    void onSeekBarValueSet(SeekBarPreference preference, String key, int value);

    void onSeekBarCancel(SeekBarPreference preference, String key);
}
