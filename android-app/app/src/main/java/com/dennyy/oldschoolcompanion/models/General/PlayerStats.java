package com.dennyy.oldschoolcompanion.models.General;

import com.dennyy.oldschoolcompanion.enums.SkillType;
import com.dennyy.oldschoolcompanion.helpers.Constants;
import com.dennyy.oldschoolcompanion.helpers.Logger;
import com.dennyy.oldschoolcompanion.helpers.Utils;

import java.util.LinkedHashMap;
import java.util.Locale;

public class PlayerStats extends LinkedHashMap<SkillType, Skill> {
    private int totalLevel;
    private long totalExp;

    /**
     * Generates skills object from official osrs hiscores api
     *
     * @param stats complete string of the official osrs hiscores api
     */
    public PlayerStats(String stats) {
        super(33);
        stats = stats.trim();
        if (Utils.isNullOrEmpty(stats)) {
            return;
        }
        String[] statsArray = stats.split("\n");
        int length = statsArray.length;
        if (length < Constants.REQUIRED_STATS_LENGTH) {
            Logger.log(stats, new IllegalArgumentException(String.format(Locale.getDefault(), "failed to parse stats with length: %d", length)));
            return;
        }
        for (int i = 0; i < statsArray.length; i++) {
            String[] line = statsArray[i].split(",");
            try {
                SkillType skillType = SkillType.fromId(i);
                if (line.length == 3) {
                    int rank = Integer.parseInt(line[0]);
                    int level = Integer.parseInt(line[1]);
                    long exp = Long.parseLong(line[2]);
                    if (skillType != SkillType.OVERALL) {
                        totalLevel += level;
                        totalExp += Math.max(0, exp);
                    }
                    Skill skill = exp > -1 ? new Skill(SkillType.fromId(i), rank, level, exp) : Skill.getDefault(SkillType.fromId(i));
                    put(skillType, skill);
                }
                // minigames
                else if (line.length == 2) {
                    int rank = Integer.parseInt(line[0]);
                    int score = Integer.parseInt(line[1]);
                    Skill skill = score > -1 ? new Skill(SkillType.fromId(i), rank, score) : Skill.getDefault(SkillType.fromId(i));
                    put(skillType, skill);
                }
            } catch (IndexOutOfBoundsException e) {
                Logger.log("Unknown skill encountered");
            }
        }
    }

    public Skill getSkill(SkillType skillType) {
        Skill skill = get(skillType);
        if (skill != null) {
            return skill;
        }
        return Skill.getDefault(skillType);
    }

    public int getLevel(SkillType skillType) {
        return getSkill(skillType).getLevel();
    }

    public long getExp(SkillType skillType) {
        return getSkill(skillType).getExp();
    }

    /**
     * Calculates combat level on the fly based on the stats in the map
     *
     * @return Default combat of 3 if stats are not found, else the combat
     */
    public Combat getCombat() {
        if (isUnranked()) {
            return Combat.getDefault();
        }
        Combat combat = new Combat(
                getLevel(SkillType.ATTACK),
                getLevel(SkillType.DEFENCE),
                getLevel(SkillType.STRENGTH),
                getLevel(SkillType.HITPOINTS),
                getLevel(SkillType.RANGED),
                getLevel(SkillType.PRAYER),
                getLevel(SkillType.MAGIC));
        return combat;
    }

    public long getCombatExp() {
        long exp = getExp(SkillType.ATTACK) +
                getExp(SkillType.DEFENCE) +
                getExp(SkillType.STRENGTH) +
                getExp(SkillType.HITPOINTS) +
                getExp(SkillType.RANGED) +
                getExp(SkillType.PRAYER) +
                getExp(SkillType.MAGIC);
        return exp;
    }

    public int getTotalLevel() {
        return Math.max(totalLevel, Constants.MIN_TOTAL_LEVEL);
    }

    public long getTotalExp() {
        return totalExp;
    }

    public boolean isUnranked() {
        return isEmpty();
    }
}
