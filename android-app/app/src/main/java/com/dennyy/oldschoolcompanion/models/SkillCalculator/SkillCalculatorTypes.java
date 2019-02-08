package com.dennyy.oldschoolcompanion.models.SkillCalculator;

import com.dennyy.oldschoolcompanion.enums.SkillType;

import java.util.LinkedHashMap;

public class SkillCalculatorTypes extends LinkedHashMap<SkillType, String> {

    private SkillCalculatorTypes(int capacity) {
        super(capacity);
    }

    public static SkillCalculatorTypes get() {
        SkillCalculatorTypes types = new SkillCalculatorTypes(18);
        types.put(SkillType.ATTACK, "skill_combat.json");
        types.put(SkillType.DEFENCE, "skill_combat.json");
        types.put(SkillType.STRENGTH, "skill_combat.json");
        types.put(SkillType.HITPOINTS, "skill_combat.json");
        types.put(SkillType.RANGED, "skill_combat.json");
        types.put(SkillType.PRAYER, "skill_prayer.json");
        types.put(SkillType.MAGIC, "skill_magic.json");
        types.put(SkillType.COOKING, "skill_cooking.json");
        types.put(SkillType.WOODCUTTING, "skill_woodcutting.json");
        types.put(SkillType.FLETCHING, "skill_fletching.json");
        types.put(SkillType.FISHING, "skill_fishing.json");
        types.put(SkillType.FIREMAKING, "skill_firemaking.json");
        types.put(SkillType.CRAFTING, "skill_crafting.json");
        types.put(SkillType.SMITHING, "skill_smithing.json");
        types.put(SkillType.MINING, "skill_mining.json");
        types.put(SkillType.HERBLORE, "skill_herblore.json");
        types.put(SkillType.AGILITY, "skill_agility.json");
        types.put(SkillType.THIEVING, "skill_thieving.json");
        types.put(SkillType.SLAYER, "skill_slayer.json");
        types.put(SkillType.FARMING, "skill_farming.json");
        types.put(SkillType.RUNECRAFTING, "skill_runecraft.json");
        types.put(SkillType.HUNTER, "skill_hunter.json");
        types.put(SkillType.CONSTRUCTION, "skill_construction.json");
        return types;
    }
}