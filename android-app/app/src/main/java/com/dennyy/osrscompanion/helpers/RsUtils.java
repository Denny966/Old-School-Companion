package com.dennyy.osrscompanion.helpers;

import com.dennyy.osrscompanion.R;
import com.dennyy.osrscompanion.enums.CombatClass;
import com.dennyy.osrscompanion.models.General.Combat;
import com.dennyy.osrscompanion.models.General.NextLevel;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RsUtils {
    public static int exp(int lvl) {
        return new int[]{ 0, 0, 83, 174, 276, 388, 512, 650, 801, 969, 1154, 1358, 1584, 1833, 2107, 2411, 2746, 3115, 3523, 3973, 4470, 5018, 5624, 6291, 7028, 7842, 8740, 9730, 10824, 12031, 13363, 14833, 16456, 18247, 20224, 22406, 24815, 27473, 30408, 33648, 37224, 41171, 45529, 50339, 55649, 61512, 67983, 75127, 83014, 91721, 101333, 111945, 123660, 136594, 150872, 166636, 184040, 203254, 224466, 247886, 273742, 302288, 333804, 368599, 407015, 449428, 496254, 547953, 605032, 668051, 737627, 814445, 899257, 992895, 1096278, 1210421, 1336443, 1475581, 1629200, 1798808, 1986068, 2192818, 2421087, 2673114, 2951373, 3258594, 3597792, 3972294, 4385776, 4842295, 5346332, 5902831, 6517253, 7195629, 7944614, 8771558, 9684577, 10692629, 11805606, 13034431, 14391160, 15889109, 17542976, 19368992, 21385073, 23611006, 26068632, 28782069, 31777943, 35085654, 38737661, 42769801, 47221641, 52136869, 57563718, 63555443, 70170840, 77474828, 85539082, 94442737, 104273167, 115126838, 127110260, 140341028, 154948977, 171077457, 188884740 }[lvl];
    }

    public static String getSkill(int index) {
        return new String[]{ "Overall", "Attack", "Defence", "Strength", "Hitpoints", "Ranged", "Prayer", "Magic", "Cooking", "Woodcutting", "Fletching", "Fishing", "Firemaking", "Crafting", "Smithing", "Mining", "Herblore", "Agility", "Thieving", "Slayer", "Farming", "Runecraft", "Hunter", "Construction", "Easy clues", "Medium clues", "Total clues", "Bounty Hunter Rogue", "Bounty Hunter", "Hard clues", "LMS", "Elite clues", "Master clues" }[index];
    }

    public static int lvl(int exp, boolean cap) {
        int lvl = 1;
        while (lvl < 126) {
            if (RsUtils.exp(lvl + 1) > exp)
                break;
            if (cap && exp > 14391159) {
                lvl = 99;
                break;
            }
            lvl++;
        }
        return lvl;
    }

    public static double getGEPercentChange(double price, double change) {
        double oldPrice = price - change;
        return (price / oldPrice * 100) - 100;
    }

    public static int getGEPriceChange(double price, double percent) {
        return (int) Math.round(price - (price / (1 + percent / 100)));
    }

    public static String kmbt(double n) {
        return kmbt(n, 1);
    }

    public static String kmbt(double n, int decimals) {
        if (n == 0) {
            return "0";
        }
        String minus = n < 0 ? "-" : "";
        String format = "#,###" + (n > 9999 ? "." + Utils.repeat("0", decimals) : "");
        DecimalFormat df = new DecimalFormat(format);
        df.setRoundingMode(RoundingMode.CEILING);
        n = Math.abs(n);
        if (n < 10000) {
            return minus + df.format(n);
        }
        else if (n < 1000000) {
            return minus + df.format(n / 1000) + "k";
        }
        else if (n < 1000000000) {
            return minus + df.format(n / 1000000) + "m";
        }
        else if (n < 1000000000000L) {
            return minus + df.format(n / 1000000000) + "b";
        }
        else if (n < 100000000000000L) {
            return minus + df.format(n / 1000000000000L) + "t";
        }
        else {
            return "0";
        }
    }

    public static double revkmbt(String input) {
        input = input.replace(" ", "");

        Pattern p = Pattern.compile("([-+]?\\d+(?:[.,]+\\d+)?)([kmbt])");
        Matcher m = p.matcher(input);
        double result;
        if (m.matches()) {
            String digits = m.group(1);
            String suffix = m.group(2);

            result = Utils.eval("(" + digits + " * 1" + Utils.repeat("000", "kmbt".indexOf(suffix) + 1) + ")");
            return result;
        }
        if (!input.matches("([-+]?\\d+(?:[.,]+\\d+)?)")) {
            return 0;
        }
        result = Double.parseDouble(input);
        return result == 0 ? 0 : result;
    }

    public static int getSkillResourceId(int skillId) {
        if (skillId == 0)
            return R.drawable.stats_icon;
        if (skillId == 1)
            return R.drawable.attack_icon;
        if (skillId == 2)
            return R.drawable.defence_icon;
        if (skillId == 3)
            return R.drawable.strength_icon;
        if (skillId == 4)
            return R.drawable.hitpoints_icon;
        if (skillId == 5)
            return R.drawable.ranged_icon;
        if (skillId == 6)
            return R.drawable.prayer_icon;
        if (skillId == 7)
            return R.drawable.magic_icon;
        if (skillId == 8)
            return R.drawable.cooking_icon;
        if (skillId == 9)
            return R.drawable.woodcutting_icon;
        if (skillId == 10)
            return R.drawable.fletching_icon;
        if (skillId == 11)
            return R.drawable.fishing_icon;
        if (skillId == 12)
            return R.drawable.firemaking_icon;
        if (skillId == 13)
            return R.drawable.crafting_icon;
        if (skillId == 14)
            return R.drawable.smithing_icon;
        if (skillId == 15)
            return R.drawable.mining_icon;
        if (skillId == 16)
            return R.drawable.herblore_icon;
        if (skillId == 17)
            return R.drawable.agility_icon;
        if (skillId == 18)
            return R.drawable.thieving_icon;
        if (skillId == 19)
            return R.drawable.slayer_icon;
        if (skillId == 20)
            return R.drawable.farming_icon;
        if (skillId == 21)
            return R.drawable.runecrafting_icon;
        if (skillId == 22)
            return R.drawable.hunter_icon;
        if (skillId == 23)
            return R.drawable.construction_icon;
        if (skillId == 24)
            return R.drawable.clue_scroll_easy;
        if (skillId == 25)
            return R.drawable.clue_scroll_med;
        if (skillId == 26)
            return R.drawable.clue_scroll;
        if (skillId == 27)
            return R.drawable.bounty_hunter_rogue;
        if (skillId == 28)
            return R.drawable.bounty_hunter;
        if (skillId == 29)
            return R.drawable.clue_scroll_hard;
        if (skillId == 30)
            return R.drawable.lms;
        if (skillId == 31)
            return R.drawable.clue_scroll_elite;
        if (skillId == 32)
            return R.drawable.clue_scroll_master;
        if (skillId == -1)
            return R.drawable.multicombat_icon;
        throw new IndexOutOfBoundsException("Invalid skill id");
    }

    public static double getCombatLevel(double attack, double defence, double strength, double hitpoints, double range, double prayer, double magic) {
        double base = (defence + hitpoints + Math.floor(prayer / 2)) * 0.25;
        double melee = (attack + strength) * 0.325;
        double mage = 0.325 * (Math.floor(magic / 2) + magic);
        double ranged = 0.325 * (Math.floor(range / 2) + range);
        double max = Math.max(melee, Math.max(ranged, mage));

        DecimalFormat df = new DecimalFormat("#.##");
        df.setRoundingMode(RoundingMode.CEILING);
        double level = Double.parseDouble(df.format(base + max));
        return level;
    }

    public static Combat combat(double attack, double defence, double strength, double hitpoints, double range, double prayer, double magic) {
        Combat combat = new Combat();
        double base = (defence + hitpoints + Math.floor(prayer / 2)) * 0.25;
        double melee = (attack + strength) * 0.325;
        double mage = 0.325 * (Math.floor(magic / 2) + magic);
        double ranged = 0.325 * (Math.floor(range / 2) + range);


        double max = Math.max(melee, Math.max(ranged, mage));

        if (melee >= max) {
            combat.combatClass = CombatClass.MELEE;
        }
        else if (range >= max) {
            combat.combatClass = CombatClass.RANGE;
        }
        else if (mage >= max) {
            combat.combatClass = CombatClass.MAGE;
        }
        DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(Locale.getDefault());
        DecimalFormat df = new DecimalFormat("#.##", symbols);
        df.setRoundingMode(RoundingMode.CEILING);
        combat.level = Double.parseDouble(df.format(base + max));
        return combat;
    }

    public static NextLevel getNextLevel(double attack, double defence, double strength, double hitpoints, double range, double prayer, double magic) {
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

    private static String getLevelsNeeded(int levelsNeeded, double skillOneLevel, double skillTwoLevel) {
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
