package com.dennyy.osrscompanion.helpers;


import android.content.Context;
import android.database.Cursor;

import com.dennyy.osrscompanion.AppController;
import com.dennyy.osrscompanion.models.General.Action;
import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

import java.util.ArrayList;

public class ActionsDb extends SQLiteAssetHelper {
    private static final String DATABASE_NAME = "actions.db";
    private static final int DATABASE_VERSION = 6;

    private static ActionsDb instance;

    public static synchronized ActionsDb getInstance() {
        if (instance == null) {
            instance = new ActionsDb(AppController.getInstance().getApplicationContext());
        }
        return instance;
    }

    private ActionsDb(Context context) {

        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        setForcedUpgrade();
    }

    public ArrayList<Action> getActions(int skillId) {
        String query = "SELECT * FROM items WHERE skill_id = ? ORDER BY level ASC";
        Cursor cursor = getWritableDatabase().rawQuery(query, new String[]{ String.valueOf(skillId) });
        ArrayList<Action> actions = new ArrayList<>();

        while (cursor.moveToNext()) {
            Action action = new Action();
            action.skillId = cursor.getInt(cursor.getColumnIndex("skill_id"));
            action.name = cursor.getString(cursor.getColumnIndex("name"));
            action.level = cursor.getInt(cursor.getColumnIndex("level"));
            action.exp = cursor.getDouble(cursor.getColumnIndex("xp"));
            actions.add(action);
        }
        cursor.close();
        return actions;
    }
}
