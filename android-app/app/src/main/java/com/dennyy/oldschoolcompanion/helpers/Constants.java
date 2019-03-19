package com.dennyy.oldschoolcompanion.helpers;

import com.dennyy.oldschoolcompanion.BuildConfig;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class Constants {
    public static final String APP_NAME = "Old School Companion";
    public static final String PREMIUM_UPGRADE_SKU = BuildConfig.DEBUG ? "android.test.purchased" : BuildConfig.APPLICATION_ID + ".premium_upgrade_sku";
    public static final int MAX_FLOATING_VIEWS = 3;
    public static final String GE_IMG_SMALL_URL = "https://services.runescape.com/m=itemdb_oldschool/obj_sprite.gif?id=";
    public static final String GE_ITEM_URL = "https://services.runescape.com/m=itemdb_oldschool/api/catalogue/detail.json?item=";
    public static final String GE_IMG_LARGE_URL = "https://services.runescape.com/m=itemdb_oldschool/obj_big.gif?id=";

    public static String GE_GRAPH_URL(String id) {return "https://services.runescape.com/m=itemdb_oldschool/api/graph/" + id + ".json"; }
    public static String GE_UPDATE_URL = "https://dennyy.com/osrs/geupdate/json/latest";
    public static String CLUE_LOC_URL(String coords) {return "https://dennyy.com/images/cluescroll/coords/" + coords + "_map.png"; }
    public static String CLUE_MAP_URL(String coords) {return "https://dennyy.com/images/cluescroll/coords/Coordinate_clue_" + coords + ".png";}
    public static String TT_MAPS_URL(String id) {return "https://www.dennyy.com/images/cluescroll/maps/" + id + ".png";}
    public static String FAIRY_RING_MAP_URL(String code) {return "https://www.dennyy.com/images/fairyrings/" + code + ".png";}

    public static String ITEMIDLIST_URL = "https://www.dennyy.com/osrs/ge/json";
    public static String OSBUDDY_EXCHANGE_SUMMARY_URL = "https://storage.googleapis.com/osbuddy-exchange/summary.json";
    public static final int OSBUDDY_SUMMARY_CACHE_DURATION = 5 * 60 * 1000; // 5 minutes
    public static final int GE_CACHE_DURATION = 3 * 60 * 1000; // 3 minutes
    public static final int GE_UPDATE_CACHE_DURATION = 3 * 60 * 1000; // 3 minutes

    public static final String RS_HISCORES_URL = "https://services.runescape.com/m=hiscore_oldschool/index_lite.ws?player=";
    public static final String RS_HISCORES_IRONMAN_URL = "https://services.runescape.com/m=hiscore_oldschool_ironman/index_lite.ws?player=";
    public static final String RS_HISCORES_UIM_URL = "https://services.runescape.com/m=hiscore_oldschool_ultimate/index_lite.ws?player=";
    public static final String RS_HISCORES_HCIM_URL = "https://services.runescape.com/m=hiscore_oldschool_hardcore_ironman/index_lite.ws?player=";
    public static final String RS_HISCORES_DMM_URL = "https://services.runescape.com/m=hiscore_oldschool_deadman/index_lite.ws?player=";
    public static final String RS_HISCORES_SDMM_URL = "https://services.runescape.com/m=hiscore_oldschool_seasonal/index_lite.ws?player=";

    public static final String OSRS_NEWS_URL = "http://services.runescape.com/m=news/latest_news.rss?oldschool=true";

    public static final int REFRESH_COOLDOWN_MS = 5000;
    public static final int REFRESH_COOLDOWN_LONG_MS = 10000;
    public static final int REFRESH_COOLDOWN_TRACK = 3000;
    public static final int REFRESH_COOLDOWN_CACHE = 1000;
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
        catch (UnsupportedEncodingException ex) {
            Logger.log(ex, rsn, String.valueOf(period));
        }
        return url;
    }

    public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/71.0.3578.98 Safari/537.36";

    public final static String PREF_RSN = "pref_rsn";
    public final static String PREF_FLOATING_VIEWS = "pref_floating_views";
    public final static String PREF_FLOATING_VIEWS_SORT_ORDER = "pref_floating_views_sort_order";
    public final static String PREF_RIGHT_SIDE = "pref_right_side";
    public final static String PREF_FEEDBACK = "pref_feedback";
    public final static String PREF_VIEW_OTHER_APPS = "pref_view_other_apps";
    public final static String PREF_VIEW_IN_STORE = "pref_view_in_store";
    public final static String PREF_SHOW_LIBRARIES = "pref_libraries";
    public static final String PREF_DOWNLOAD_ITEMIDLIST = "pref_download_itemidlist";
    public static final String PREF_LANDSCAPE_ONLY = "pref_only_in_landscape";
    public static final String PREF_FULLSCREEN_ONLY = "pref_only_in_fullscreen_apps";
    public static final String PREF_PADDING_SIDE = "pref_padding_side";
    public static final String PREF_PADDING = "pref_padding";
    public static final String PREF_OPACITY = "pref_opacity";
    public static final String PREF_QUEST_SOURCE = "pref_quest_source";
    public static final String PREF_SIZE = "pref_size";
    public static final String PREF_GE_SOURCE = "pref_ge_items_source";
    public static final String PREF_VERSION = "pref_version";
    public static final String PREF_START_EXTERNAL = "pref_start_external";
    public static final String PREF_QUEST_SORT_TYPE = "pref_quest_sort_type";
    public static final String PREF_QUEST_SORT_DIRECTION = "pref_quest_sort_direction";
    public static final String PREF_HW_ACCELERATION = "pref_hw_acceleration";
    public static final String PREF_SHOW_CLOSE_BUTTON = "pref_show_close_button";

    public static final int CODE_DRAW_OVER_OTHER_APP_PERMISSION = 2084;
    public static final int CODE_STORAGE_PERMISSION = 2085;
    public static final String PERMISSION_ACTIVITY = "android.settings.action.MANAGE_OVERLAY_PERMISSION";
    public static final int FUZZY_RATIO = 80;

    public static final String NOTES_FILE_NAME = "notes.txt";
    public static final String ITEMIDLIST_FILE_NAME = "itemidlist.json";
    public static final String HOSTS_FILE_NAME = "hosts.txt";

    public static final int MAX_EXP = 200_000_000;
    public static final double DEFAULT_COMBAT = 3.4;
    public static final int REQUIRED_STATS_LENGTH = 24;
    public static final int MIN_TOTAL_LEVEL = 32;
    public static final int MAX_SKILLS_LENGTH = 33;
    public static final float HIGH_ALCHEMY_CONSTANT = 0.6f;
    public static final float LOW_ALCHEMY_CONSTANT = 0.4f;

    public static DecimalFormatSymbols LOCALE = DecimalFormatSymbols.getInstance(Locale.US);
    public static final String WORLDMAP_DOWNLOAD_KEY = BuildConfig.APPLICATION_ID + ".worldmap_download";
    public static final String WORLDMAP_DIRECTORY_PATH = String.format("/%1$s", APP_NAME);
    public static final String WORLDMAP_FILE_PATH = String.format("/%1$s/worldmap.png", APP_NAME);
    public static final int WORLDMAP_NOTIFICATION_ID = 966;

    public static final String FIRST_STARTUP = BuildConfig.APPLICATION_ID + ".pref_first_startup";
    public static final String FIRST_VERSION_STARTUP = BuildConfig.APPLICATION_ID + ".pref_first_version_startup";
    public static final String PREVIOUS_APP_VERSION = BuildConfig.APPLICATION_ID + ".previous_app_version";

    public static final int CLICK_DURATION_THRESHOLD = 300;
    public static final int CLICK_DISTANCE_THRESHOLD = 15;

    public static final String SORT_DELIMITER = ":";
}
