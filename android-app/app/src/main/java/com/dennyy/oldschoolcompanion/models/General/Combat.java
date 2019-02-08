package com.dennyy.oldschoolcompanion.models.General;

import com.dennyy.oldschoolcompanion.enums.CombatClass;
import com.dennyy.oldschoolcompanion.helpers.Constants;

import java.math.RoundingMode;
import java.text.DecimalFormat;

public class Combat {
    private int attack;
    private int defence;
    private int strength;
    private int hitpoints;
    private int range;
    private int prayer;
    private int magic;

    private double level;
    private CombatClass combatClass;
    private NextLevel nextLevel;

    public Combat(int attack, int defence, int strength, int hitpoints, int range, int prayer, int magic) {
        this.attack = attack;
        this.defence = defence;
        this.strength = strength;
        this.hitpoints = hitpoints;
        this.range = range;
        this.prayer = prayer;
        this.magic = magic;

        double base = (defence + hitpoints + Math.floor(prayer / 2)) * 0.25;
        double melee = (attack + strength) * 0.325;
        double mage = 0.325 * (Math.floor(magic / 2) + magic);
        double ranged = 0.325 * (Math.floor(range / 2) + range);


        double max = Math.max(melee, Math.max(ranged, mage));

        if (melee >= max) {
            combatClass = CombatClass.MELEE;
        }
        else if (range >= max) {
            combatClass = CombatClass.RANGE;
        }
        else if (mage >= max) {
            combatClass = CombatClass.MAGE;
        }
        DecimalFormat df = new DecimalFormat("#.##", Constants.LOCALE);
        df.setRoundingMode(RoundingMode.CEILING);
        level = Double.parseDouble(df.format(base + max));
        nextLevel = calculateNextLevel();
    }

    public double getLevel() {
        return level;
    }

    public CombatClass getCombatClass() {
        return combatClass;
    }

    public NextLevel getNextLevel() {
        return nextLevel;
    }

    public static Combat getDefault() {
        return new Combat(1, 1, 1, 10, 1, 1, 1);
    }

    private NextLevel calculateNextLevel() {
        NextLevel nextLevel = new NextLevel();
        double currentCombatLevel = getCombatLevel(attack, defence, strength, hitpoints, range, prayer, magic);
        int levelsNeeded = 1;
        while (Math.floor(currentCombatLevel) >= Math.floor(getCombatLevel(attack + levelsNeeded, defence, strength, hitpoints, range, prayer, magic))) {
            levelsNeeded++;
        }
        nextLevel.AttackOrStrength = getLevelsNeeded(levelsNeeded, attack, strength);

        levelsNeeded = 1;

        while (Math.floor(currentCombatLevel) >= Math.floor(getCombatLevel(attack, defence + levelsNeeded, strength, hitpoints, range, prayer, magic))) {
            levelsNeeded++;
        }
        nextLevel.DefenceOrHitpoints = getLevelsNeeded(levelsNeeded, defence, hitpoints);
        levelsNeeded = 1;

        while (Math.floor(currentCombatLevel) >= Math.floor(getCombatLevel(attack, defence, strength, hitpoints, range, prayer + levelsNeeded, magic))) {
            levelsNeeded++;
        }
        nextLevel.Prayer = getLevelsNeeded(levelsNeeded, prayer, prayer);
        levelsNeeded = 1;

        while (Math.floor(currentCombatLevel) >= Math.floor(getCombatLevel(attack, defence, strength, hitpoints, range + levelsNeeded, prayer, magic))) {
            levelsNeeded++;
        }
        nextLevel.Range = getLevelsNeeded(levelsNeeded, range, range);
        levelsNeeded = 1;

        while (Math.floor(currentCombatLevel) >= Math.floor(getCombatLevel(attack, defence, strength, hitpoints, range, prayer, magic + levelsNeeded))) {
            levelsNeeded++;
        }
        nextLevel.Mage = getLevelsNeeded(levelsNeeded, magic, magic);

        return nextLevel;
    }

    private double getCombatLevel(double attack, double defence, double strength, double hitpoints, double range, double prayer, double magic) {
        double base = (defence + hitpoints + Math.floor(prayer / 2)) * 0.25;
        double melee = (attack + strength) * 0.325;
        double mage = 0.325 * (Math.floor(magic / 2) + magic);
        double ranged = 0.325 * (Math.floor(range / 2) + range);
        double max = Math.max(melee, Math.max(ranged, mage));

        DecimalFormat df = new DecimalFormat("#.##", Constants.LOCALE);
        df.setRoundingMode(RoundingMode.CEILING);
        double level = Double.parseDouble(df.format(base + max));
        return level;
    }


    private String getLevelsNeeded(int levelsNeeded, double skillOneLevel, double skillTwoLevel) {
        int result = skillOneLevel < 99 && skillOneLevel + levelsNeeded <= 99 ? levelsNeeded : -1;
        if (result == -1) {
            result = skillTwoLevel < 99 && skillTwoLevel + levelsNeeded <= 99 ? levelsNeeded : -1;
        }
        if (result == -1) {
            return "N/A";
        }
        return String.valueOf(result);
    }
}