package com.dennyy.osrscompanion.layouthandlers;

import android.content.Context;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Toast;

import com.dennyy.osrscompanion.helpers.Constants;

public abstract class BaseViewHandler {
    private Toast toast;
    protected Context context;
    protected View view;
    protected Resources resources;
    protected String defaultRsn;
    protected boolean wasRequesting;

    BaseViewHandler(Context context, View view) {
        this.context = context;
        this.view = view;
        this.resources = context.getResources();
        this.defaultRsn = PreferenceManager.getDefaultSharedPreferences(context).getString(Constants.PREF_RSN, "");

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

    public abstract boolean wasRequesting();

    public abstract void cancelVolleyRequests();
}