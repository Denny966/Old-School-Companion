package com.dennyy.oldschoolcompanion.viewhandlers;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.dennyy.oldschoolcompanion.helpers.Constants;
import com.dennyy.oldschoolcompanion.helpers.Utils;

public abstract class BaseViewHandler {
    private Toast toast;
    private final String defaultRsn;
    protected Context context;
    protected View view;
    protected Resources resources;
    protected boolean wasRequesting;
    protected boolean isFloatingView;
    protected SharedPreferences preferences;

    private final Handler keyBoardHandler = new Handler();
    private Runnable keyBoardRunnable;

    BaseViewHandler(Context context, View view) {
        this(context, view, false);
    }

    BaseViewHandler(Context context, View view, boolean isFloatingView) {
        this.context = context;
        this.view = view;
        this.resources = context.getResources();
        this.preferences = PreferenceManager.getDefaultSharedPreferences(context);
        this.defaultRsn = preferences.getString(Constants.PREF_RSN, "");
        this.isFloatingView = isFloatingView;
    }

    protected void showToast(String message, int duration) {
        if (toast != null)
            toast.cancel();
        toast = Toast.makeText(context, message, duration);
        toast.show();
    }

    protected String getString(int resourceId) {
        return context.getResources().getString(resourceId);
    }

    protected String getString(int resourceId, Object... formatArgs) {
        return context.getResources().getString(resourceId, formatArgs);
    }

    protected int getColor(int resourceId) {
        return context.getResources().getColor(resourceId);
    }

    protected Drawable getDrawable(int resourceId) {
        return context.getDrawable(resourceId);
    }

    protected void hideKeyboard() {
        cancelKeyboardRunnable();
        keyBoardRunnable = new Runnable() {
            @Override
            public void run() {
                Utils.hideKeyboard(context, view);
            }
        };
        keyBoardHandler.postDelayed(keyBoardRunnable, 500);
    }

    protected void cancelKeyboardRunnable() {
        keyBoardHandler.removeCallbacks(keyBoardRunnable);
    }

    protected void showKeyboard(final View view) {
        cancelKeyboardRunnable();
        view.requestFocus();
        keyBoardRunnable = new Runnable() {
            @Override
            public void run() {
                Utils.showKeyboard(context, view);
            }
        };
        keyBoardHandler.postDelayed(keyBoardRunnable, 500);
    }

    /**
     * Try to get text from edittext as rsn else it uses the defaultRsn that the user set in the settings
     * Check the result of this method also for null or empty and handle accordingly
     *
     * @param editText The edittext to get the text from
     * @return String with the rsn
     */
    protected String getRsn(EditText editText) {
        String result = editText.getText().toString();
        if (Utils.isNullOrEmpty(result)) {
            result = defaultRsn;
        }
        if (!Utils.isNullOrEmpty(result)) {
            editText.setText(result);
        }
        return result;
    }

    public abstract boolean wasRequesting();

    public abstract void cancelRunningTasks();
}