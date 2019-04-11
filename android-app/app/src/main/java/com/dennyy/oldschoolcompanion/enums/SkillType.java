package com.dennyy.oldschoolcompanion.enums;

import com.dennyy.oldschoolcompanion.R;
import com.dennyy.oldschoolcompanion.helpers.Constants;
import com.dennyy.oldschoolcompanion.helpers.Logger;

public enum SkillType {
    COMBAT(-1, R.drawable.multicombat_icon, "Combat"),
    OVERALL(0, R.drawable.stats_icon, "Overall"),
    ATTACK(1, R.drawable.attack_icon, "Attack"),
    DEFENCE(2, R.drawable.defence_icon, "Defence"),
    STRENGTH(3, R.drawable.strength_icon, "Strength"),
    HITPOINTS(4, R.drawable.hitpoints_icon, "Hitpoints"),
    RANGED(5, R.drawable.ranged_icon, "Ranged"),
    PRAYER(6, R.drawable.prayer_icon, "Prayer"),
    MAGIC(7, R.drawable.magic_icon, "Magic"),
    COOKING(8, R.drawable.cooking_icon, "Cooking"),
    WOODCUTTING(9, R.drawable.woodcutting_icon, "Woodcutting"),
    FLETCHING(10, R.drawable.fletching_icon, "Fletching"),
    FISHING(11, R.drawable.fishing_icon, "Fishing"),
    FIREMAKING(12, R.drawable.firemaking_icon, "Firemaking"),
    CRAFTING(13, R.drawable.crafting_icon, "Crafting"),
    SMITHING(14, R.drawable.smithing_icon, "Smithing"),
    MINING(15, R.drawable.mining_icon, "Mining"),
    HERBLORE(16, R.drawable.herblore_icon, "Herblore"),
    AGILITY(17, R.drawable.agility_icon, "Agility"),
    THIEVING(18, R.drawable.thieving_icon, "Thieving"),
    SLAYER(19, R.drawable.slayer_icon, "Slayer"),
    FARMING(20, R.drawable.farming_icon, "Farming"),
    RUNECRAFTING(21, R.drawable.runecrafting_icon, "Runecraft"),
    HUNTER(22, R.drawable.hunter_icon, "Hunter"),
    CONSTRUCTION(23, R.drawable.construction_icon, "Construction"),
    BH(24, R.drawable.bounty_hunter, "Bounty Hunter"),
    BHR(25, R.drawable.bounty_hunter_rogue, "Bounty Hunter Rogue"),
    LMS(26, R.drawable.lms, "LMS"),
    CLUE_TOTAL(27, R.drawable.clue_scroll, "Total clues"),
    CLUE_BEGINNER(28, R.drawable.clue_scroll_beginner, "Beginner clues"),
    CLUE_EASY(29, R.drawable.clue_scroll_easy, "Easy clues"),
    CLUE_MED(30, R.drawable.clue_scroll_med, "Medium clues"),
    CLUE_HARD(31, R.drawable.clue_scroll_hard, "Hard clues"),
    CLUE_ELITE(32, R.drawable.clue_scroll_elite, "Elite clues"),
    CLUE_MASTER(33, R.drawable.clue_scroll_master, "Master clues");

    public final int id;
    public final int drawable;
    public final String name;

    SkillType(int id, int drawable, String name) {
        this.id = id;
        this.drawable = drawable;
        this.name = name;
    }

    public boolean isMinigame() {
        return id >= Constants.REQUIRED_STATS_LENGTH;
    }

    public static SkillType fromId(int id) {
        for (SkillType skillType : SkillType.values()) {
            if (skillType.id == id) {
                return skillType;
            }
        }
        Logger.log(String.valueOf(id), new IndexOutOfBoundsException("unknown skill id"));
        throw new IndexOutOfBoundsException(String.format("Unknown skill id: %d", id));
    }

    public static boolean isCombat(SkillType type, SkillType... exclusions) {
        boolean excluded = false;
        for (SkillType exclusion : exclusions) {
            excluded = type == exclusion;
            if (excluded) break;
        }
        return !excluded && (type == SkillType.ATTACK || type == SkillType.STRENGTH || type == SkillType.DEFENCE || type == SkillType.HITPOINTS ||
                type == SkillType.RANGED || type == SkillType.MAGIC || type == SkillType.PRAYER);
    }
}