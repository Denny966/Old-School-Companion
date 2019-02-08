package com.dennyy.oldschoolcompanion.models.General;

import com.dennyy.oldschoolcompanion.enums.QuestDifficulty;
import com.dennyy.oldschoolcompanion.enums.QuestLength;
import com.dennyy.oldschoolcompanion.helpers.Utils;

public class Quest {
    public final String name;
    public final String url;
    public final String runeHqUrl;
    public final QuestDifficulty questDifficulty;
    public final QuestLength questLength;
    public final boolean isMembers;
    public final int questPoints;
    private boolean isCompleted;

    public Quest(String name, String url, String runeHqUrl, int questDifficulty, int questLength, boolean isMembers, int questPoints) {
        this.name = name;
        this.url = url;
        this.runeHqUrl = runeHqUrl;
        this.questDifficulty = QuestDifficulty.fromValue(questDifficulty);
        this.questLength = QuestLength.fromValue(questLength);
        this.isMembers = isMembers;
        this.questPoints = questPoints;
    }

    public boolean hasRuneHqUrl() {
        return !Utils.isNullOrEmpty(runeHqUrl);
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }
}