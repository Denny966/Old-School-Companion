package com.dennyy.osrscompanion.helpers;

import android.util.Log;

import com.dennyy.osrscompanion.BuildConfig;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class Constants {
    public static String GE_IMG_SMALL_URL = "https://services.runescape.com/m=itemdb_oldschool/obj_sprite.gif?id=";
    public static String GE_ITEM_URL = "https://services.runescape.com/m=itemdb_oldschool/api/catalogue/detail.json?item=";
    public static String GE_IMG_LARGE_URL = "https://services.runescape.com/m=itemdb_oldschool/obj_big.gif?id=";

    public static String GE_GRAPH_URL(String id) {return "https://services.runescape.com/m=itemdb_oldschool/api/graph/" + id + ".json"; }
    public static String GE_UPDATE_URL = "https://dennyy.com/osrs/geupdate/json/latest";
    public static String CLUE_LOC_URL(String coords) {return "https://dennyy.com/images/cluescroll/coords/" + coords + "_map.png"; }
    public static String CLUE_MAP_URL(String coords) {return "https://dennyy.com/images/cluescroll/coords/Coordinate_clue_" + coords + ".png";}
    public static String TT_MAPS_URL(String id) {return "https://www.dennyy.com/images/cluescroll/maps/" + id + ".png";}
    public static String ITEMIDLIST_URL = "https://www.dennyy.com/osrs/ge/json";
    public static String OSBUDDY_EXCHANGE_URL = "https://api.rsbuddy.com/grandExchange?a=guidePrice&i=";

    public static String RS_HISCORES_URL = "http://services.runescape.com/m=hiscore_oldschool/index_lite.ws?player=";
    public static String RS_HISCORES_IRONMAN_URL = "http://services.runescape.com/m=hiscore_oldschool_ironman/index_lite.ws?player=";
    public static String RS_HISCORES_UIM_URL = "http://services.runescape.com/m=hiscore_oldschool_ultimate/index_lite.ws?player=";
    public static String RS_HISCORES_HCIM_URL = "http://services.runescape.com/m=hiscore_oldschool_hardcore_ironman/index_lite.ws?player=";
    public static String RS_HISCORES_DMM_URL = "http://services.runescape.com/m=hiscore_oldschool_deadman/index_lite.ws?player=";
    public static String RS_HISCORES_SDMM_URL = "http://services.runescape.com/m=hiscore_oldschool_seasonal/index_lite.ws?player=";

    public static int REFRESH_COOLDOWN_MS = 5000;
    public static int REFRESH_COOLDOWN_LONG_MS = 10000;
    public static int REFRESH_COOLDOWN_TRACK = 3000;
    public static int REFRESH_COOLDOWN_CACHE = 1000;
    public static final int MAX_REFRESH_COUNT = 5;

    public static String TRACKER_URL(String rsn, long period) {

        //        Error values:
        //        -1 = User not in database
        //        -2 = Invalid username
        //        -3 = Database error
        //        -4 = Server under heavy load; api temporarily disabled

        // UPDATE CODES
        //        Return codes:
        //        1 = Success!
        //        2 = Player not on RuneScape hiscores.
        //        3 = Negative XP gain detected.
        //        4 = Unknown error.
        //        5 = This player has been updated within the last 30 seconds.
        //        6 = The player name was invalid.
        String url = "https://crystalmathlabs.com/tracker/api.php?multiquery=";
        try {
            url += URLEncoder.encode("[{\"type\": \"update\", \"player\": \"" + rsn + "\"}, {\"type\": \"track\", \"time\": \"" + period + "\"}]", "utf-8");
        }
        catch (UnsupportedEncodingException e) {
            Log.e(Constants.class.getSimpleName(), "Error creating tracker url");
        }
        return url;
    }

    public final static String PREF_RSN = "pref_rsn";
    public final static String PREF_FLOATING_VIEWS = "pref_floating_views";
    public final static String PREF_RIGHT_SIDE = "pref_right_side";
    public final static String PREF_FEEDBACK = "pref_feedback";
    public final static String PREF_VIEW_OTHER_APPS = "pref_view_other_apps";
    public final static String PREF_VIEW_IN_STORE = "pref_view_in_store";
    public final static String PREF_SHOW_LIBRARIES = "pref_libraries";
    public static final String PREF_DOWNLOAD_ITEMIDLIST = "pref_download_itemidlist";
    public static final String PREF_LANDSCAPE_ONLY = "pref_only_in_landscape";
    public static final String PREF_FULLSCREEN_ONLY = "pref_only_in_fullscreen_apps";
    public static final String PREF_ALIGN_LEFT = "pref_align_floating_views_left";
    public static final String PREF_ALIGN_MARGIN = "pref_alignment_margin";
    public static final String PREF_OPACITY = "pref_opacity";

    public static final int CODE_DRAW_OVER_OTHER_APP_PERMISSION = 2084;
    public static final String PERMISSION_ACTIVITY = "android.settings.action.MANAGE_OVERLAY_PERMISSION";
    public static final int FUZZY_RATIO = 80;

    public static final String NOTES_FILE_NAME = "notes.txt";
    public static final String ITEMIDLIST_FILE_NAME = "itemidlist.json";
    public static final String UPDATE_NOTE_ACTION = BuildConfig.APPLICATION_ID.concat(".UPDATE_NOTE_ACTION");

    public static final int MAX_EXP = 200_000_000;
    public static final double DEFAULT_COMBAT = 3.4;

    public static DecimalFormatSymbols LOCALE = DecimalFormatSymbols.getInstance(Locale.US);
}

