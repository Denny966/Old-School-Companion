package com.dennyy.osrscompanion.models.Hiscores;

import com.dennyy.osrscompanion.helpers.RsUtils;

import java.util.ArrayList;
import java.util.List;


public class TotalAndCombatInfo {
    private int totalLevel;
    private long totalExp;
    private long combatExp;
    private double combatLevel;

    /***
     * Object to hold totallevel, combat exp and combat level based on the given string array
     * from the osrs hiscore api
     * @param stats
     */
    public TotalAndCombatInfo(String[] stats) {
        if (stats.length <= 1) {
            this.totalLevel = 0;
            this.totalExp = 0;
            this.combatExp = 0;
            this.combatLevel = 0;
        }
        else {
            List<Integer> cmb = new ArrayList<>();
            int totalLevel = 0;
            long totalExp = 0;
            long combatExp = 0;

            for (int i = 0; i < stats.length; i++) {
                String[] line = stats[i].split(",");
                if (line.length == 3) {
                    int level = Integer.parseInt(line[1]);
                    long exp = Long.parseLong(line[2]);
                    cmb.add(level);
                    if (i > 0) {
                        if (i < 8)
                            combatExp += exp;
                        totalExp += exp;
                    }
                    totalLevel += level;

                }
            }
            int att = cmb.get(1);
            int def = cmb.get(2);
            int str = cmb.get(3);
            int hp = cmb.get(4);
            int range = cmb.get(5);
            int pray = cmb.get(6);
            int mage = cmb.get(7);
            double combat = RsUtils.combat(att, def, str, hp, range, pray, mage);
            this.totalLevel = totalLevel;
            this.totalExp = totalExp;
            this.combatExp = combatExp;
            this.combatLevel = combat;
        }
    }

    public int getTotalLevel() {
        return totalLevel;
    }

    public long getTotalExp() { return totalExp; }

    public long getCombatExp() {
        return combatExp;
    }

    public double getCombatLevel() {
        return combatLevel;
    }
}
