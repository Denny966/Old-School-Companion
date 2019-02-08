package com.dennyy.oldschoolcompanion.helpers;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RsUtils {
    public static int exp(int lvl) {
        return new int[]{ 0, 0, 83, 174, 276, 388, 512, 650, 801, 969, 1154, 1358, 1584, 1833, 2107, 2411, 2746, 3115, 3523, 3973, 4470, 5018, 5624, 6291, 7028, 7842, 8740, 9730, 10824, 12031, 13363, 14833, 16456, 18247, 20224, 22406, 24815, 27473, 30408, 33648, 37224, 41171, 45529, 50339, 55649, 61512, 67983, 75127, 83014, 91721, 101333, 111945, 123660, 136594, 150872, 166636, 184040, 203254, 224466, 247886, 273742, 302288, 333804, 368599, 407015, 449428, 496254, 547953, 605032, 668051, 737627, 814445, 899257, 992895, 1096278, 1210421, 1336443, 1475581, 1629200, 1798808, 1986068, 2192818, 2421087, 2673114, 2951373, 3258594, 3597792, 3972294, 4385776, 4842295, 5346332, 5902831, 6517253, 7195629, 7944614, 8771558, 9684577, 10692629, 11805606, 13034431, 14391160, 15889109, 17542976, 19368992, 21385073, 23611006, 26068632, 28782069, 31777943, 35085654, 38737661, 42769801, 47221641, 52136869, 57563718, 63555443, 70170840, 77474828, 85539082, 94442737, 104273167, 115126838, 127110260, 140341028, 154948977, 171077457, 188884740 }[lvl];
    }

    public static int lvl(long exp, boolean cap) {
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
        return kmbt(n, 2);
    }

    public static String kmbt(double n, int decimals) {
        if (n == 0) {
            return "0";
        }
        String minus = n < 0 ? "-" : "";
        String format = "#,###" + (n > 9999 ? "." + Utils.repeat("0", decimals) : "");
        DecimalFormat df = new DecimalFormat(format, Constants.LOCALE);
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
        return result;
    }

    public static Date getDateFromItemIdList(String content) throws JSONException, ParseException {
        JSONObject obj = new JSONObject(content);
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date fileDate = format.parse(obj.getString("datetime"));
        return fileDate;
    }
}