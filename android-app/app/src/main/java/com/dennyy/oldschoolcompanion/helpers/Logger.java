package com.dennyy.oldschoolcompanion.helpers;

import android.util.Log;
import com.dennyy.oldschoolcompanion.AppController;

public class Logger {

    public static void log(String message, Exception exception) {

    }

    public static void log(Exception exception, String... messages) {

    }

    public static void log(Exception exception) {

    }

    public static void log(String message) {
        Log.d(AppController.TAG, message);
    }
}
