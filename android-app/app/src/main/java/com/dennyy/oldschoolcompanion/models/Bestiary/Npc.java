package com.dennyy.oldschoolcompanion.models.Bestiary;

import android.content.Context;
import android.text.Html;
import com.dennyy.oldschoolcompanion.helpers.Logger;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Npc {
    public final boolean successfulBuild;
    public final ArrayList<String> versions;
    public final String name;
    public final ArrayList<String> img;
    public final String releaseDate;
    public final ArrayList<String> combat;
    public final ArrayList<String> hitpoints;
    public final ArrayList<String> slaylvl;
    public final ArrayList<String> slayExp;
    public final String members;
    public final String aggressive;
    public final String poisonous;
    public final ArrayList<String> attackStyles;
    public final ArrayList<String> maxHit;
    public final ArrayList<String> weakness;
    public final ArrayList<String> examine;
    public final String immunePoison;
    public final String immuneVenom;
    public final ArrayList<String> att;
    public final ArrayList<String> str;
    public final ArrayList<String> def;
    public final ArrayList<String> mage;
    public final ArrayList<String> range;
    public final ArrayList<String> astab;
    public final ArrayList<String> aslash;
    public final ArrayList<String> acrush;
    public final ArrayList<String> amagic;
    public final ArrayList<String> arange;
    public final ArrayList<String> dstab;
    public final ArrayList<String> dslash;
    public final ArrayList<String> dcrush;
    public final ArrayList<String> dmagic;
    public final ArrayList<String> drange;
    public final ArrayList<String> strBonus;
    public final ArrayList<String> rangeBonus;
    public final ArrayList<String> attBonus;
    public final ArrayList<String> mageBonus;
    public final ArrayList<NpcDrop> drops;

    private Npc(Builder builder) {
        this.successfulBuild = builder.successfulBuild;
        this.versions = builder.versions;
        this.name = builder.name;
        this.img = builder.img;
        this.releaseDate = builder.releaseDate;
        this.combat = builder.combat;
        this.hitpoints = builder.hitpoints;
        this.slaylvl = builder.slaylvl;
        this.slayExp = builder.slayExp;
        this.members = builder.members;
        this.aggressive = builder.aggressive;
        this.poisonous = builder.poisonous;
        this.attackStyles = builder.attackStyles;
        this.maxHit = builder.maxHit;
        this.weakness = builder.weakness;
        this.examine = builder.examine;
        this.immunePoison = builder.immunePoison;
        this.immuneVenom = builder.immuneVenom;
        this.att = builder.att;
        this.str = builder.str;
        this.def = builder.def;
        this.mage = builder.mage;
        this.range = builder.range;
        this.astab = builder.astab;
        this.aslash = builder.aslash;
        this.acrush = builder.acrush;
        this.amagic = builder.amagic;
        this.arange = builder.arange;
        this.dstab = builder.dstab;
        this.dslash = builder.dslash;
        this.dcrush = builder.dcrush;
        this.dmagic = builder.dmagic;
        this.drange = builder.drange;
        this.strBonus = builder.strBonus;
        this.rangeBonus = builder.rangeBonus;
        this.attBonus = builder.attBonus;
        this.mageBonus = builder.mageBonus;
        this.drops = builder.drops;
    }

    public static class Builder {
        private Context context;
        private boolean successfulBuild;
        private ArrayList<String> versions = new ArrayList<>();
        private String name;
        private ArrayList<String> img = new ArrayList<>();
        private String releaseDate;
        private ArrayList<String> combat = new ArrayList<>();
        private ArrayList<String> hitpoints = new ArrayList<>();
        private ArrayList<String> slaylvl = new ArrayList<>();
        private ArrayList<String> slayExp = new ArrayList<>();
        private String members;
        private String aggressive;
        private String poisonous;
        private ArrayList<String> attackStyles = new ArrayList<>();
        private ArrayList<String> maxHit = new ArrayList<>();
        private ArrayList<String> weakness = new ArrayList<>();
        private ArrayList<String> examine = new ArrayList<>();
        private String immunePoison;
        private String immuneVenom;
        private ArrayList<String> att = new ArrayList<>();
        private ArrayList<String> str = new ArrayList<>();
        private ArrayList<String> def = new ArrayList<>();
        private ArrayList<String> mage = new ArrayList<>();
        private ArrayList<String> range = new ArrayList<>();
        private ArrayList<String> astab = new ArrayList<>();
        private ArrayList<String> aslash = new ArrayList<>();
        private ArrayList<String> acrush = new ArrayList<>();
        private ArrayList<String> amagic = new ArrayList<>();
        private ArrayList<String> arange = new ArrayList<>();
        private ArrayList<String> dstab = new ArrayList<>();
        private ArrayList<String> dslash = new ArrayList<>();
        private ArrayList<String> dcrush = new ArrayList<>();
        private ArrayList<String> dmagic = new ArrayList<>();
        private ArrayList<String> drange = new ArrayList<>();
        private ArrayList<String> strBonus = new ArrayList<>();
        private ArrayList<String> rangeBonus = new ArrayList<>();
        private ArrayList<String> attBonus = new ArrayList<>();
        private ArrayList<String> mageBonus = new ArrayList<>();
        private ArrayList<NpcDrop> drops = new ArrayList<>();

        Builder(Context context) {
            this.context = context;
        }

        public Builder setSuccessfulBuild(boolean successfulBuild) {
            this.successfulBuild = successfulBuild;
            return this;
        }

        public Builder addVersion(String version) {
            this.versions.add(version);
            return this;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder addImg(String img) {
            this.img.add(img);
            return this;
        }

        public Builder setReleaseDate(String releaseDate) {
            this.releaseDate = releaseDate;
            return this;
        }

        public Builder setMembers(String members) {
            this.members = members;
            return this;
        }

        public Builder setAggressive(String aggressive) {
            this.aggressive = aggressive;
            return this;
        }

        public Builder addCombat(String combat) {
            this.combat.add(combat);
            return this;
        }

        public Builder addHitpoints(String hitpoints) {
            this.hitpoints.add(hitpoints);
            return this;
        }

        public Builder addSlaylvl(String slaylvl) {
            this.slaylvl.add(slaylvl);
            return this;
        }

        public Builder addSlayExp(String slayExp) {
            this.slayExp.add(slayExp);
            return this;
        }

        public Builder setPoisonous(String poisonous) {
            this.poisonous = poisonous;
            return this;
        }

        public Builder addAttackStyles(String attackStyles) {
            this.attackStyles.add(attackStyles);
            return this;
        }

        public Builder addMaxHit(String maxHit) {
            this.maxHit.add(maxHit);
            return this;
        }

        public Builder addWeakness(String weakness) {
            this.weakness.add(weakness);
            return this;
        }

        public Builder addExamine(String examine) {
            this.examine.add(examine.replace("{{*}}", "-"));
            return this;
        }

        public Builder setImmunePoison(String immunePoison) {
            this.immunePoison = immunePoison;
            return this;
        }

        public Builder setImmuneVenom(String immuneVenom) {
            this.immuneVenom = immuneVenom;
            return this;
        }

        public Builder addAtt(String att) {
            this.att.add(att);
            return this;
        }

        public Builder addStr(String str) {
            this.str.add(str);
            return this;
        }

        public Builder addDef(String def) {
            this.def.add(def);
            return this;
        }

        public Builder addMage(String mage) {
            this.mage.add(mage);
            return this;
        }

        public Builder addRange(String range) {
            this.range.add(range);
            return this;
        }

        public Builder addAstab(String astab) {
            this.astab.add(astab);
            return this;
        }

        public Builder addAslash(String aslash) {
            this.aslash.add(aslash);
            return this;
        }

        public Builder addAcrush(String acrush) {
            this.acrush.add(acrush);
            return this;
        }

        public Builder addAmagic(String amagic) {
            this.amagic.add(amagic);
            return this;
        }

        public Builder addArange(String arange) {
            this.arange.add(arange);
            return this;
        }

        public Builder addDstab(String dstab) {
            this.dstab.add(dstab);
            return this;
        }

        public Builder addDslash(String dslash) {
            this.dslash.add(dslash);
            return this;
        }

        public Builder addDcrush(String dcrush) {
            this.dcrush.add(dcrush);
            return this;
        }

        public Builder addDmagic(String dmagic) {
            this.dmagic.add(dmagic);
            return this;
        }

        public Builder addDrange(String drange) {
            this.drange.add(drange);
            return this;
        }

        public Builder addStrBonus(String strBonus) {
            this.strBonus.add(strBonus);
            return this;
        }

        public Builder addRangeBonus(String rangeBonus) {
            this.rangeBonus.add(rangeBonus);
            return this;
        }

        public Builder addAttBonus(String attBonus) {
            this.attBonus.add(attBonus);
            return this;
        }

        public Builder addMageBonus(String mageBonus) {
            this.mageBonus.add(mageBonus);
            return this;
        }

        public Builder setDrops(ArrayList<NpcDrop> drops) {
            this.drops = drops;
            return this;
        }

        public Npc build() {
            return new Npc(this);
        }
    }

    public static Npc fromJson(Context context, String npcName, String result) {
        Npc.Builder builder = new Npc.Builder(context);
        try {
            JSONObject root = new JSONObject(result);
            JSONObject parse = root.getJSONObject("parse");
            builder.setName(parse.getString("title"));
            int pageId = parse.getInt("pageid");
            String wikiText = parse.getJSONObject("wikitext").getString("*");
            String infoBox;
            int startPos = wikiText.indexOf("{{Infobox Monster");
            if (startPos == -1) {
                builder.setSuccessfulBuild(false);
                return builder.build();
            }
            int endPos = findClosingParen(wikiText, startPos);
            if (endPos > -1) {
                infoBox = wikiText.substring(startPos + 2, endPos - 2);
            }
            else {
                Pattern pattern = Pattern.compile("\\{\\{Infobox Monster(.*?)\\}\\}", Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.MULTILINE);
                Matcher m = pattern.matcher(wikiText);
                if (!m.find()) {
                    throw new IllegalArgumentException(wikiText);
                }
                infoBox = m.group(1);
            }
            String[] npcInfo = infoBox.split("\\|");
            for (String info : npcInfo) {
                String[] line = info.split("=");
                if (line.length < 2) continue;
                String key = line[0].trim().toLowerCase();
                String value = Html.fromHtml(line[1].trim().replace("[[", "").replace("]]", "")).toString();
                if (key.startsWith("version"))
                    builder.addVersion(value);
                if (key.startsWith("image")) {
                    String[] values = value.split(":");
                    if (values.length > 1) {
                        builder.addImg(values[1].replace(" ", "_"));
                    }
                }
                if (key.startsWith("release"))
                    builder.setReleaseDate(value);
                if (key.startsWith("cb") || key.startsWith("combat") || key.startsWith("level"))
                    builder.addCombat(value);
                if (key.startsWith("hp") || key.startsWith("hitpoints"))
                    builder.addHitpoints(value);
                if (key.startsWith("slaylvl"))
                    builder.addSlaylvl(value);
                if (key.startsWith("slayxp"))
                    builder.addSlayExp(value);
                if (key.startsWith("members"))
                    builder.setMembers(value);
                if (key.startsWith("aggressive"))
                    builder.setAggressive(value);
                if (key.startsWith("poisonous"))
                    builder.setPoisonous(value);
                if (key.startsWith("attack style"))
                    builder.addAttackStyles(value);
                if (key.startsWith("max hit"))
                    builder.addMaxHit(value);
                if (key.startsWith("weakness"))
                    builder.addWeakness(value);
                if (key.startsWith("examine"))
                    builder.addExamine(value);
                if (key.startsWith("immunepoison"))
                    builder.setImmunePoison(value);
                if (key.startsWith("immunevenom"))
                    builder.setImmuneVenom(value);
                if (key.matches("att(\\d+)?"))
                    builder.addAtt(value);
                if (key.matches("str(\\d+)?"))
                    builder.addStr(value);
                if (key.startsWith("def"))
                    builder.addDef(value);
                if (key.startsWith("mage"))
                    builder.addMage(value);
                if (key.startsWith("range"))
                    builder.addRange(value);
                if (key.startsWith("astab"))
                    builder.addAstab(value);
                if (key.startsWith("aslash"))
                    builder.addAslash(value);
                if (key.startsWith("acrush"))
                    builder.addAcrush(value);
                if (key.startsWith("amagic"))
                    builder.addAmagic(value);
                if (key.startsWith("arange"))
                    builder.addArange(value);
                if (key.startsWith("dstab"))
                    builder.addDstab(value);
                if (key.startsWith("dslash"))
                    builder.addDslash(value);
                if (key.startsWith("dcrush"))
                    builder.addDcrush(value);
                if (key.startsWith("dmagic"))
                    builder.addDmagic(value);
                if (key.startsWith("drange"))
                    builder.addDrange(value);
                if (key.startsWith("strbns"))
                    builder.addStrBonus(value);
                if (key.startsWith("rngbns"))
                    builder.addRangeBonus(value);
                if (key.startsWith("attbns"))
                    builder.addAttBonus(value);
                if (key.startsWith("mbns"))
                    builder.addMageBonus(value);
            }
            String dropsLine = "{{DropsLine";
            ArrayList<NpcDrop> drops = new ArrayList<>();
            startPos = wikiText.indexOf(dropsLine);
            endPos = findClosingParen(wikiText, startPos);
            while (startPos > -1 && endPos > -1) {
                String[] dropInfo = wikiText.substring(startPos, endPos).split("\\|");
                NpcDrop.Builder dropBuilder = new NpcDrop.Builder();
                for (String drop : dropInfo) {
                    String[] line = drop.split("=");
                    if (line.length < 2) continue;
                    String key = line[0].trim().toLowerCase();
                    String value = Html.fromHtml(line[1].trim().replace("}", "").replace("{", "").replace("NamedRef", "").replace("[[", "").replace("]]", "").replace("CiteTwitter", "")).toString();
                    switch (key) {
                        case "name":
                            dropBuilder.setName(value);
                            break;
                        case "namenotes":
                            dropBuilder.setNameNotes(value);
                            break;
                        case "quantity":
                            dropBuilder.setQuantity(value);
                            break;
                        case "rarity":
                            dropBuilder.setRarity(value);
                            break;
                        case "raritynotes":
                            dropBuilder.setRarityNotes(value);
                            break;
                    }
                }

                drops.add(dropBuilder.build());
                startPos = wikiText.indexOf(dropsLine, endPos);
                endPos = findClosingParen(wikiText, startPos);
            }

            builder.setDrops(drops);
            builder.setSuccessfulBuild(true);
        }
        catch (Exception e) {
            Logger.log(e, String.format("failed to parse npc %s", npcName), result);
            builder.setSuccessfulBuild(false);
        }
        return builder.build();
    }

    private static int findClosingParen(String text, int openPos) {
        int closePos = openPos;
        int counter = 1;
        while (counter > 0) {
            if (closePos >= text.length() - 1) {
                return -1;
            }
            char c = text.charAt(++closePos);
            if (c == '{') {
                counter++;
            }
            else if (c == '}') {
                counter--;
            }
        }
        return closePos;
    }
}
