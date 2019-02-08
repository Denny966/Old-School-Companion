package com.dennyy.oldschoolcompanion.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.dennyy.oldschoolcompanion.enums.HiscoreType;
import com.dennyy.oldschoolcompanion.enums.TrackDurationType;
import com.dennyy.oldschoolcompanion.helpers.Logger;
import com.dennyy.oldschoolcompanion.models.GrandExchange.*;
import com.dennyy.oldschoolcompanion.models.Hiscores.UserStats;
import com.dennyy.oldschoolcompanion.models.OSBuddy.OSBuddySummary;
import com.dennyy.oldschoolcompanion.models.OSRSNews.OSRSNewsDTO;
import com.dennyy.oldschoolcompanion.models.Timers.Timer;
import com.dennyy.oldschoolcompanion.models.TodoList.TodoList;
import com.dennyy.oldschoolcompanion.models.TodoList.TodoListEntry;
import com.dennyy.oldschoolcompanion.models.Tracker.TrackData;

import java.util.ArrayList;
import java.util.HashSet;

public class AppDb extends SQLiteOpenHelper {
    private static AppDb instance;

    public static synchronized AppDb getInstance(Context context) {
        if (instance == null) {
            instance = new AppDb(context.getApplicationContext());
        }
        return instance;
    }

    private AppDb(Context context) {
        super(context, DB.name, null, DB.version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // run when the database file did not exist and was just created
        String createUserStatsTable = "CREATE TABLE " + DB.UserStats.tableName + " (" +
                DB.UserStats.id + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                DB.UserStats.rsn + " TEXT NOT NULL COLLATE NOCASE, " +
                DB.UserStats.stats + " TEXT, " +
                DB.UserStats.hiscoreType + " INTEGER NOT NULL, " +
                DB.UserStats.dateModified + " INTEGER NOT NULL);";

        String createTrackTable = "CREATE TABLE " + DB.Track.tableName + " (" +
                DB.Track.id + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                DB.Track.rsn + " TEXT NOT NULL COLLATE NOCASE, " +
                DB.Track.data + " TEXT, " +
                DB.Track.durationType + " INTEGER NOT NULL, " +
                DB.Track.dateModified + " INTEGER NOT NULL);";

        String createGrandExchangeTable = "CREATE TABLE " + DB.GrandExchange.tableName + " (" +
                DB.GrandExchange.itemId + " INTEGER PRIMARY KEY, " +
                DB.GrandExchange.data + " TEXT, " +
                DB.GrandExchange.dateModified + " INTEGER NOT NULL);";

        String createGrandExchangeUpdateTable = "CREATE TABLE " + DB.GrandExchangeUpdate.tableName + " (" +
                DB.GrandExchangeUpdate.id + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                DB.GrandExchangeUpdate.data + " TEXT NOT NULL, " +
                DB.GrandExchangeUpdate.dateModified + " INTEGER NOT NULL);";

        String createGrandExchangeGraphTable = "CREATE TABLE " + DB.GrandExchangeGraph.tableName + " (" +
                DB.GrandExchangeGraph.itemId + " INTEGER PRIMARY KEY, " +
                DB.GrandExchangeGraph.data + " TEXT NOT NULL, " +
                DB.GrandExchangeGraph.dateModified + " INTEGER NOT NULL);";

        String createOSBuddyExchangeSummaryTable = "CREATE TABLE " + DB.OSBuddyExchangeSummary.tableName + " (" +
                DB.OSBuddyExchangeSummary.id + " INTEGER PRIMARY KEY, " +
                DB.OSBuddyExchangeSummary.data + " TEXT, " +
                DB.OSBuddyExchangeSummary.dateModified + " INTEGER NOT NULL);";

        String createOSRSNewsTable = "CREATE TABLE " + DB.OSRSNews.tableName + " (" +
                DB.OSRSNews.id + " INTEGER PRIMARY KEY, " +
                DB.OSRSNews.data + " TEXT, " +
                DB.OSRSNews.dateModified + " INTEGER NOT NULL);";

        String createTimersTable = "CREATE TABLE " + DB.Timers.tableName + " (" +
                DB.Timers.id + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                DB.Timers.title + " TEXT, " +
                DB.Timers.description + " TEXT, " +
                DB.Timers.repeat + " INTEGER DEFAULT 0, " +
                DB.Timers.interval + " INTEGER NOT NULL, " +
                DB.Timers.dateModified + " INTEGER NOT NULL);";

        String createTodoListTable = "CREATE TABLE " + DB.Todo.tableName + " (" +
                DB.Todo.id + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                DB.Todo.sortOrder + " INTEGER NOT NULL, " +
                DB.Todo.content + " TEXT, " +
                DB.Todo.done + " INTEGER DEFAULT 0)";

        String createNpcTable = "CREATE TABLE " + DB.Npc.tableName + " (" +
                DB.Npc.name + " TEXT UNIQUE, " +
                DB.Npc.data + " TEXT)";

        String createGeHistoryTable = "CREATE TABLE " + DB.GeHistory.tableName + " (" +
                DB.GeHistory.itemId + " INTEGER PRIMARY KEY, " +
                DB.GeHistory.name + " TEXT, " +
                DB.GeHistory.isFavorite + " INTEGER DEFAULT 0, " +
                DB.GeHistory.dateModified + " INTEGER NOT NULL)";

        String createQuestCompletionTable = "CREATE TABLE " + DB.QuestCompletion.tableName + " (" +
                DB.QuestCompletion.name + " TEXT UNIQUE)";


        String createBestiaryHistoryTable = "CREATE TABLE " + DB.BestiaryHistory.tableName + " (" +
                DB.BestiaryHistory.name + " TEXT UNIQUE, " +
                DB.BestiaryHistory.dateModified + " INTEGER NOT NULL);";

        db.execSQL(createUserStatsTable);
        db.execSQL(createTrackTable);
        db.execSQL(createGrandExchangeTable);
        db.execSQL(createGrandExchangeUpdateTable);
        db.execSQL(createGrandExchangeGraphTable);
        db.execSQL(createOSBuddyExchangeSummaryTable);
        db.execSQL(createOSRSNewsTable);
        db.execSQL(createTimersTable);
        db.execSQL(createTodoListTable);
        db.execSQL(createNpcTable);
        db.execSQL(createGeHistoryTable);
        db.execSQL(createQuestCompletionTable);
        db.execSQL(createBestiaryHistoryTable);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        setWriteAheadLoggingEnabled(true);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 7) {
            db.execSQL("DROP TABLE IF EXISTS " + DB.UserStats.tableName);
            db.execSQL("DROP TABLE IF EXISTS " + DB.Track.tableName);
            db.execSQL("DROP TABLE IF EXISTS " + DB.GrandExchange.tableName);
            db.execSQL("DROP TABLE IF EXISTS " + DB.GrandExchangeUpdate.tableName);
            db.execSQL("DROP TABLE IF EXISTS " + DB.GrandExchangeGraph.tableName);
            db.execSQL("DROP TABLE IF EXISTS " + DB.OSBuddyExchangeSummary.tableName);
            db.execSQL("DROP TABLE IF EXISTS " + DB.OSBuddyExchange.tableName);
            db.execSQL("DROP TABLE IF EXISTS " + DB.OSRSNews.tableName);
            onCreate(db);
        }
        if (oldVersion < 8) {
            String createOSRSNewsTable = "CREATE TABLE IF NOT EXISTS " + DB.OSRSNews.tableName + " (" +
                    DB.OSRSNews.id + " INTEGER PRIMARY KEY, " +
                    DB.OSRSNews.data + " TEXT, " +
                    DB.OSRSNews.dateModified + " INTEGER NOT NULL);";
            db.execSQL(createOSRSNewsTable);
        }
        if (oldVersion < 9) {
            db.execSQL("DROP TABLE IF EXISTS " + DB.OSBuddyExchange.tableName);
        }
        if (oldVersion < 10) {
            db.execSQL("DROP TABLE IF EXISTS " + DB.OSBuddyExchangeSummary.tableName);
            String createOSBuddyExchangeSummaryTable = "CREATE TABLE IF NOT EXISTS " + DB.OSBuddyExchangeSummary.tableName + " (" +
                    DB.OSBuddyExchangeSummary.id + " INTEGER PRIMARY KEY, " +
                    DB.OSBuddyExchangeSummary.data + " TEXT, " +
                    DB.OSBuddyExchangeSummary.dateModified + " INTEGER NOT NULL);";
            db.execSQL(createOSBuddyExchangeSummaryTable);
        }
        if (oldVersion < 11) {
            String createTimersTable = "CREATE TABLE IF NOT EXISTS " + DB.Timers.tableName + " (" +
                    DB.Timers.id + " INTEGER PRIMARY KEY, " +
                    DB.Timers.title + " TEXT, " +
                    DB.Timers.description + " TEXT, " +
                    DB.Timers.repeat + " INTEGER DEFAULT 0, " +
                    DB.Timers.interval + " INTEGER NOT NULL, " +
                    DB.Timers.dateModified + " INTEGER NOT NULL);";
            db.execSQL(createTimersTable);
        }
        if (oldVersion < 12) {
            String createTodoListTable = "CREATE TABLE IF NOT EXISTS " + DB.Todo.tableName + " (" +
                    DB.Todo.id + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    DB.Todo.sortOrder + " INTEGER NOT NULL, " +
                    DB.Todo.content + " TEXT, " +
                    DB.Todo.done + " INTEGER DEFAULT 0);";
            db.execSQL(createTodoListTable);
        }
        if (oldVersion < 13) {
            String createNpcTable = "CREATE TABLE IF NOT EXISTS " + DB.Npc.tableName + " (" +
                    DB.Npc.name + " TEXT UNIQUE, " +
                    DB.Npc.data + " TEXT)";
            db.execSQL(createNpcTable);
        }
        if (oldVersion < 14) {
            String createGeHistoryTable = "CREATE TABLE IF NOT EXISTS " + DB.GeHistory.tableName + " (" +
                    DB.GeHistory.itemId + " INTEGER PRIMARY KEY, " +
                    DB.GeHistory.name + " TEXT, " +
                    DB.GeHistory.isFavorite + " INTEGER DEFAULT 0, " +
                    DB.GeHistory.dateModified + " INTEGER NOT NULL)";

            db.execSQL(createGeHistoryTable);
        }
        if (oldVersion < 15) {
            String createQuestCompletionTable = "CREATE TABLE IF NOT EXISTS " + DB.QuestCompletion.tableName + " (" +
                    DB.QuestCompletion.name + " TEXT UNIQUE)";

            db.execSQL(createQuestCompletionTable);
        }
        if (oldVersion < 16) {
            String createBestiaryHistoryTable = "CREATE TABLE IF NOT EXISTS " + DB.BestiaryHistory.tableName + " (" +
                    DB.BestiaryHistory.name + " TEXT UNIQUE, " +
                    DB.BestiaryHistory.dateModified + " INTEGER NOT NULL);";
            db.execSQL(createBestiaryHistoryTable);
        }
    }

    public UserStats getUserStats(String rsn, HiscoreType mode) {
        String query = "SELECT * FROM " + DB.UserStats.tableName + " WHERE " + DB.UserStats.rsn + " = ? AND " + DB.UserStats.hiscoreType + " = ?";
        Cursor cursor = getReadableDatabase().rawQuery(query, new String[]{ rsn, String.valueOf(mode.getValue()) });
        UserStats userStats = null;
        if (cursor.moveToFirst()) {
            int hiscoreType = cursor.getInt(cursor.getColumnIndex(DB.UserStats.hiscoreType));
            String stats = cursor.getString(cursor.getColumnIndex(DB.UserStats.stats));
            userStats = new UserStats(rsn, stats, HiscoreType.fromValue(hiscoreType));
            userStats.dateModified = cursor.getLong(cursor.getColumnIndex(DB.UserStats.dateModified));
        }
        cursor.close();
        return userStats;
    }

    public void insertOrUpdateUserStats(UserStats userStats) {
        String query = "SELECT * FROM " + DB.UserStats.tableName + " WHERE " + DB.UserStats.rsn + " = ? AND " + DB.UserStats.hiscoreType + " = ?";
        Cursor cursor = getReadableDatabase().rawQuery(query, new String[]{ userStats.rsn, String.valueOf(userStats.hiscoreType) });
        if (cursor.moveToFirst()) {
            ContentValues cv = new ContentValues();
            cv.put(DB.UserStats.rsn, userStats.rsn);
            cv.put(DB.UserStats.stats, userStats.stats);
            cv.put(DB.UserStats.hiscoreType, userStats.hiscoreType);
            cv.put(DB.UserStats.dateModified, System.currentTimeMillis());
            getWritableDatabase().update(DB.UserStats.tableName, cv, DB.UserStats.rsn + " = ? AND " + DB.UserStats.hiscoreType + " = ?", new String[]{ userStats.rsn, String.valueOf(userStats.hiscoreType) });
            cursor.close();
        }
        else {
            ContentValues cv = new ContentValues();
            cv.put(DB.UserStats.rsn, userStats.rsn);
            cv.put(DB.UserStats.stats, userStats.stats);
            cv.put(DB.UserStats.hiscoreType, userStats.hiscoreType);
            cv.put(DB.UserStats.dateModified, System.currentTimeMillis());
            getWritableDatabase().insert(DB.UserStats.tableName, null, cv);
            cursor.close();
        }
    }

    public TrackData getTrackData(String rsn, TrackDurationType mode) {
        String query = "SELECT * FROM " + DB.Track.tableName + " WHERE " + DB.Track.rsn + " = ? AND " + DB.Track.durationType + " = ?";
        Cursor cursor = getReadableDatabase().rawQuery(query, new String[]{ rsn, String.valueOf(mode.getValue()) });
        TrackData trackData = null;
        if (cursor.moveToFirst()) {
            trackData = new TrackData();
            trackData.rsn = rsn;
            trackData.data = cursor.getString(cursor.getColumnIndex(DB.Track.data));
            trackData.durationType = TrackDurationType.fromValue(cursor.getInt(cursor.getColumnIndex(DB.Track.durationType)));
            trackData.dateModified = cursor.getLong(cursor.getColumnIndex(DB.Track.dateModified));

        }
        cursor.close();
        return trackData;
    }

    public void insertOrUpdateTrackData(TrackData trackData) {
        insertOrUpdateTrackData(trackData.rsn, trackData.durationType, trackData.data);
    }

    public void insertOrUpdateTrackData(String rsn, TrackDurationType type, String data) {
        String query = "SELECT * FROM " + DB.Track.tableName + " WHERE " + DB.Track.rsn + " = ? AND " + DB.Track.durationType + " = ?";
        Cursor cursor = getReadableDatabase().rawQuery(query, new String[]{ rsn, String.valueOf(type.getValue()) });
        if (cursor.moveToFirst()) {
            ContentValues cv = new ContentValues();
            cv.put(DB.Track.rsn, rsn);
            cv.put(DB.Track.data, data);
            cv.put(DB.Track.durationType, type.getValue());
            cv.put(DB.Track.dateModified, System.currentTimeMillis());
            getWritableDatabase().update(DB.Track.tableName, cv, DB.Track.rsn + " = ? AND " + DB.Track.durationType + " = ?", new String[]{ rsn, String.valueOf(type.getValue()) });
            cursor.close();
        }
        else {
            ContentValues cv = new ContentValues();
            cv.put(DB.Track.rsn, rsn);
            cv.put(DB.Track.data, data);
            cv.put(DB.Track.durationType, type.getValue());
            cv.put(DB.Track.dateModified, System.currentTimeMillis());
            getWritableDatabase().insert(DB.Track.tableName, null, cv);
            cursor.close();
        }
    }

    public GrandExchangeData getGrandExchangeData(String itemId) {
        String query = "SELECT * FROM " + DB.GrandExchange.tableName + " WHERE " + DB.GrandExchange.itemId + " = ?";
        Cursor cursor = getReadableDatabase().rawQuery(query, new String[]{ itemId });
        GrandExchangeData grandExchangeData = null;
        if (cursor.moveToFirst()) {
            grandExchangeData = new GrandExchangeData();
            grandExchangeData.itemId = itemId;
            grandExchangeData.data = cursor.getString(cursor.getColumnIndex(DB.GrandExchange.data));
            grandExchangeData.dateModified = cursor.getLong(cursor.getColumnIndex(DB.GrandExchange.dateModified));

        }
        cursor.close();
        return grandExchangeData;
    }

    public void insertOrUpdateGrandExchangeData(String itemId, String newData) {
        String query = "SELECT * FROM " + DB.GrandExchange.tableName + " WHERE " + DB.GrandExchange.itemId + " = ?";
        Cursor cursor = getReadableDatabase().rawQuery(query, new String[]{ itemId });
        if (cursor.moveToFirst()) {
            ContentValues cv = new ContentValues();
            cv.put(DB.GrandExchange.data, newData);
            cv.put(DB.GrandExchange.dateModified, System.currentTimeMillis());
            getWritableDatabase().update(DB.GrandExchange.tableName, cv, DB.GrandExchange.itemId + " = ?", new String[]{ itemId });
            cursor.close();
        }
        else {
            ContentValues cv = new ContentValues();
            cv.put(DB.GrandExchange.itemId, itemId);
            cv.put(DB.GrandExchange.data, newData);
            cv.put(DB.GrandExchange.dateModified, System.currentTimeMillis());
            getWritableDatabase().insert(DB.GrandExchange.tableName, null, cv);
            cursor.close();
        }
    }

    public OSBuddySummary getOSBuddyExchangeSummary() {
        String query = "SELECT * FROM " + DB.OSBuddyExchangeSummary.tableName + " WHERE " + DB.OSBuddyExchangeSummary.id + " = 1";
        Cursor cursor = getReadableDatabase().rawQuery(query, null);
        OSBuddySummary osBuddyExchangeData = null;
        if (cursor.moveToFirst()) {
            osBuddyExchangeData = new OSBuddySummary();
            osBuddyExchangeData.id = 1;
            osBuddyExchangeData.data = cursor.getString(cursor.getColumnIndex(DB.OSBuddyExchangeSummary.data));
            osBuddyExchangeData.dateModified = cursor.getLong(cursor.getColumnIndex(DB.OSBuddyExchangeSummary.dateModified));
        }
        cursor.close();
        return osBuddyExchangeData;
    }

    public void insertOrUpdateOSBuddyExchangeSummaryData(String newData) {
        String query = "SELECT * FROM " + DB.OSBuddyExchangeSummary.tableName + " WHERE " + DB.OSBuddyExchangeSummary.id + " = 1";
        Cursor cursor = getReadableDatabase().rawQuery(query, null);
        if (cursor.moveToFirst()) {
            ContentValues cv = new ContentValues();
            cv.put(DB.OSBuddyExchangeSummary.data, newData);
            cv.put(DB.OSBuddyExchangeSummary.dateModified, System.currentTimeMillis());
            getWritableDatabase().update(DB.OSBuddyExchangeSummary.tableName, cv, DB.OSBuddyExchangeSummary.id + " = 1", null);
            cursor.close();
        }
        else {
            ContentValues cv = new ContentValues();
            cv.put(DB.OSBuddyExchangeSummary.data, newData);
            cv.put(DB.OSBuddyExchangeSummary.dateModified, System.currentTimeMillis());
            getWritableDatabase().insert(DB.OSBuddyExchangeSummary.tableName, null, cv);
            cursor.close();
        }
    }

    public GrandExchangeUpdateData getGrandExchangeUpdateData() {
        String query = "SELECT * FROM " + DB.GrandExchangeUpdate.tableName + " WHERE " + DB.GrandExchangeUpdate.id + " = 1";
        Cursor cursor = getReadableDatabase().rawQuery(query, null);
        GrandExchangeUpdateData result = null;
        if (cursor.moveToFirst()) {
            result = new GrandExchangeUpdateData();
            result.data = cursor.getString(cursor.getColumnIndex(DB.GrandExchangeUpdate.data));
            result.dateModified = cursor.getLong(cursor.getColumnIndex(DB.GrandExchangeUpdate.dateModified));
        }
        cursor.close();
        return result;
    }

    public void updateGrandExchangeUpdateData(String newData) {
        String query = "SELECT * FROM " + DB.GrandExchangeUpdate.tableName + " WHERE " + DB.GrandExchangeUpdate.id + " = 1";
        Cursor cursor = getReadableDatabase().rawQuery(query, null);
        if (cursor.moveToFirst()) {
            ContentValues cv = new ContentValues();
            cv.put(DB.GrandExchangeUpdate.data, newData);
            cv.put(DB.GrandExchangeUpdate.dateModified, System.currentTimeMillis());
            getWritableDatabase().update(DB.GrandExchangeUpdate.tableName, cv, DB.GrandExchangeUpdate.id + " = 1", null);
            cursor.close();
        }
        else {
            ContentValues cv = new ContentValues();
            cv.put(DB.GrandExchange.data, newData);
            cv.put(DB.GrandExchange.dateModified, System.currentTimeMillis());
            getWritableDatabase().insert(DB.GrandExchangeUpdate.tableName, null, cv);
            cursor.close();
        }
    }

    public GrandExchangeGraphData getGrandExchangeGraphData(String itemId) {
        String query = "SELECT * FROM " + DB.GrandExchangeGraph.tableName + " WHERE " + DB.GrandExchangeGraph.itemId + " = ?";
        Cursor cursor = getReadableDatabase().rawQuery(query, new String[]{ itemId });
        GrandExchangeGraphData result = null;
        if (cursor.moveToFirst()) {
            result = new GrandExchangeGraphData();
            result.data = cursor.getString(cursor.getColumnIndex(DB.GrandExchangeGraph.data));
            result.dateModified = cursor.getLong(cursor.getColumnIndex(DB.GrandExchangeGraph.dateModified));
        }
        cursor.close();
        return result;
    }

    public void insertOrUpdateGrandExchangeGraphData(String itemId, String newData) {
        String query = "SELECT * FROM " + DB.GrandExchangeGraph.tableName + " WHERE " + DB.GrandExchangeGraph.itemId + " = ?";
        Cursor cursor = getReadableDatabase().rawQuery(query, new String[]{ itemId });
        if (cursor.moveToFirst()) {
            ContentValues cv = new ContentValues();
            cv.put(DB.GrandExchangeGraph.data, newData);
            cv.put(DB.GrandExchangeGraph.dateModified, System.currentTimeMillis());
            getWritableDatabase().update(DB.GrandExchangeGraph.tableName, cv, DB.GrandExchangeGraph.itemId + " = ?", new String[]{ itemId });
            cursor.close();
        }
        else {
            ContentValues cv = new ContentValues();
            cv.put(DB.GrandExchangeGraph.itemId, itemId);
            cv.put(DB.GrandExchangeGraph.data, newData);
            cv.put(DB.GrandExchangeGraph.dateModified, System.currentTimeMillis());
            getWritableDatabase().insert(DB.GrandExchangeGraph.tableName, null, cv);
            cursor.close();
        }
    }

    public OSRSNewsDTO getOSRSNews() {
        String query = "SELECT * FROM " + DB.OSRSNews.tableName + " WHERE " + DB.OSRSNews.id + " = 1";
        Cursor cursor = getReadableDatabase().rawQuery(query, null);
        OSRSNewsDTO result = null;
        if (cursor.moveToFirst()) {
            result = new OSRSNewsDTO();
            result.id = 1;
            result.data = cursor.getString(cursor.getColumnIndex(DB.OSRSNews.data));
            result.dateModified = cursor.getLong(cursor.getColumnIndex(DB.OSRSNews.dateModified));
        }
        cursor.close();
        return result;
    }

    public void insertOrUpdateOSRSNewsData(String newData) {
        String query = "SELECT * FROM " + DB.OSRSNews.tableName + " WHERE " + DB.OSRSNews.id + " = 1";
        Cursor cursor = getReadableDatabase().rawQuery(query, null);
        if (cursor.moveToFirst()) {
            ContentValues cv = new ContentValues();
            cv.put(DB.OSRSNews.data, newData);
            cv.put(DB.OSRSNews.dateModified, System.currentTimeMillis());
            getWritableDatabase().update(DB.OSRSNews.tableName, cv, DB.OSRSNews.id + " = 1", null);
            cursor.close();
        }
        else {
            ContentValues cv = new ContentValues();
            cv.put(DB.OSRSNews.data, newData);
            cv.put(DB.OSRSNews.dateModified, System.currentTimeMillis());
            getWritableDatabase().insert(DB.OSRSNews.tableName, null, cv);
            cursor.close();
        }
    }

    public void insertOrUpdateTimer(Timer timer) {
        String id = String.valueOf(timer.id);
        String query = "SELECT * FROM " + DB.Timers.tableName + " WHERE " + DB.Timers.id + " = ?";
        Cursor cursor = getReadableDatabase().rawQuery(query, new String[]{ id });
        ContentValues cv = new ContentValues();
        cv.put(DB.Timers.title, timer.title);
        cv.put(DB.Timers.description, timer.description);
        cv.put(DB.Timers.interval, timer.interval);
        cv.put(DB.Timers.repeat, timer.isRepeating);
        cv.put(DB.Timers.dateModified, System.currentTimeMillis());
        if (cursor.moveToFirst()) {
            getWritableDatabase().update(DB.Timers.tableName, cv, DB.Timers.id + " = ?", new String[]{ id });
            cursor.close();
        }
        else {
            getWritableDatabase().insert(DB.Timers.tableName, null, cv);
            cursor.close();
        }
    }

    public ArrayList<Timer> getTimers() {
        String query = "SELECT * FROM " + DB.Timers.tableName + " ORDER BY " + DB.Timers.dateModified + " DESC";
        Cursor cursor = getReadableDatabase().rawQuery(query, null);
        ArrayList<Timer> timers = new ArrayList<>();

        while (cursor.moveToNext()) {
            Timer timer = new Timer();
            timer.id = cursor.getInt(cursor.getColumnIndex(DB.Timers.id));
            timer.title = cursor.getString(cursor.getColumnIndex(DB.Timers.title));
            timer.description = cursor.getString(cursor.getColumnIndex(DB.Timers.description));
            timer.interval = cursor.getInt(cursor.getColumnIndex(DB.Timers.interval));
            timer.isRepeating = cursor.getInt(cursor.getColumnIndex(DB.Timers.repeat)) == 1;
            timers.add(timer);
        }
        cursor.close();
        return timers;
    }

    public void deleteTimer(int timerId) {
        getReadableDatabase().delete(DB.Timers.tableName, DB.Timers.id + " = ?", new String[]{ String.valueOf(timerId) });
    }

    public void insertOrUpdateTodo(TodoListEntry entry) {
        String id = String.valueOf(entry.id);
        String query = "SELECT * FROM " + DB.Todo.tableName + " WHERE " + DB.Todo.id + " = ?";
        Cursor cursor = getReadableDatabase().rawQuery(query, new String[]{ id });
        ContentValues cv = new ContentValues();
        cv.put(DB.Todo.content, entry.content);
        cv.put(DB.Todo.sortOrder, entry.sortOrder);
        cv.put(DB.Todo.done, entry.done);
        if (cursor.moveToFirst()) {
            getWritableDatabase().update(DB.Todo.tableName, cv, DB.Todo.id + " = ?", new String[]{ id });
            cursor.close();
        }
        else {
            getWritableDatabase().insert(DB.Todo.tableName, null, cv);
            cursor.close();
        }
    }

    public TodoList getTodoList() {
        String query = "SELECT * FROM " + DB.Todo.tableName + " ORDER BY " + DB.Todo.sortOrder + " DESC";
        Cursor cursor = getReadableDatabase().rawQuery(query, null);
        TodoList todoList = new TodoList();

        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndex(DB.Todo.id));
            String content = cursor.getString(cursor.getColumnIndex(DB.Todo.content));
            int sortOrder = cursor.getInt(cursor.getColumnIndex(DB.Todo.sortOrder));
            boolean done = cursor.getInt(cursor.getColumnIndex(DB.Todo.done)) == 1;
            TodoListEntry todoListEntry = new TodoListEntry(id, sortOrder, content, done);
            todoList.add(todoListEntry);
        }
        cursor.close();
        return todoList;
    }

    public void deleteTodo(int todoId) {
        getReadableDatabase().delete(DB.Todo.tableName, DB.Todo.id + " = ?", new String[]{ String.valueOf(todoId) });
    }

    public void updateTodoListOrder(TodoList todoList) {
        String where = DB.Todo.id + " = ?";
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            int index = todoList.size();
            for (TodoListEntry entry : todoList) {
                ContentValues cv = new ContentValues();
                cv.put(DB.Todo.sortOrder, index);
                db.update(DB.Todo.tableName, cv, where, new String[]{ String.valueOf(entry.id) });
                index--;
            }
            db.setTransactionSuccessful();
        }
        catch (Exception e) {
            Logger.log("error updating sortorder", e);
        }
        finally {
            db.endTransaction();
        }
    }

    public String getNpcData(String npcName) {
        String query = "SELECT * FROM " + DB.Npc.tableName + " WHERE " + DB.Npc.name + " = ?";
        Cursor cursor = getReadableDatabase().rawQuery(query, new String[]{ npcName });
        String result = null;
        if (cursor.moveToFirst()) {
            result = cursor.getString(cursor.getColumnIndex(DB.Npc.data));
        }
        cursor.close();
        return result;
    }

    public void insertOrUpdateNpc(String npcName, String npcData) {
        String query = "SELECT * FROM " + DB.Npc.tableName + " WHERE " + DB.Npc.name + " = ?";
        Cursor cursor = getReadableDatabase().rawQuery(query, new String[]{ npcName });
        ContentValues cv = new ContentValues();
        cv.put(DB.Npc.data, npcData);
        if (cursor.moveToFirst()) {
            getWritableDatabase().update(DB.Npc.tableName, cv, DB.Npc.name + " = ?", new String[]{ npcName });
            cursor.close();
        }
        else {
            cv.put(DB.Npc.name, npcName);
            getWritableDatabase().insert(DB.Npc.tableName, null, cv);
            cursor.close();
        }
    }

    public void deleteMonsterData(String monsterName) {
        getReadableDatabase().delete(DB.Npc.tableName, DB.Npc.name + " = ?", new String[]{ monsterName });
    }

    public void insertOrUpdateGeHistory(String itemId, String itemName, boolean isFavorite) {
        String id = String.valueOf(itemId);
        String query = "SELECT * FROM " + DB.GeHistory.tableName + " WHERE " + DB.GeHistory.itemId + " = ?";
        Cursor cursor = getReadableDatabase().rawQuery(query, new String[]{ id });
        ContentValues cv = new ContentValues();
        cv.put(DB.GeHistory.isFavorite, isFavorite);
        cv.put(DB.GeHistory.name, itemName);
        cv.put(DB.GeHistory.dateModified, System.currentTimeMillis());
        if (cursor.moveToFirst()) {
            getReadableDatabase().update(DB.GeHistory.tableName, cv, DB.GeHistory.itemId + " = ?", new String[]{ id });
        }
        else {
            cv.put(DB.GeHistory.itemId, id);
            getWritableDatabase().insert(DB.GeHistory.tableName, null, cv);
        }
        cursor.close();
    }

    public GeHistory getGeHistory() {
        return getGeHistory(false);
    }

    public GeHistory getGeHistory(boolean clearHistory) {
        if (clearHistory) {
            getReadableDatabase().delete(DB.GeHistory.tableName, DB.GeHistory.isFavorite + " = ?", new String[]{ String.valueOf(0) });
        }
        String query = "SELECT * FROM " + DB.GeHistory.tableName + " ORDER BY " + DB.GeHistory.isFavorite + " DESC, " + DB.GeHistory.dateModified + " DESC";
        Cursor cursor = getReadableDatabase().rawQuery(query, null);
        GeHistory geHistory = new GeHistory();

        while (cursor.moveToNext()) {
            String itemId = cursor.getString(cursor.getColumnIndex(DB.GeHistory.itemId));
            String itemName = cursor.getString(cursor.getColumnIndex(DB.GeHistory.name));
            boolean isFavorite = cursor.getInt(cursor.getColumnIndex(DB.GeHistory.isFavorite)) == 1;
            geHistory.add(new GeHistoryEntry(itemId, itemName, isFavorite));
        }
        cursor.close();
        return geHistory;
    }

    public void insertOrUpdateQuestCompletion(String questName, boolean completed) {
        String query = "SELECT * FROM " + DB.QuestCompletion.tableName + " WHERE " + DB.QuestCompletion.name + " = ?";
        Cursor cursor = getReadableDatabase().rawQuery(query, new String[]{ questName });
        ContentValues cv = new ContentValues();
        cv.put(DB.QuestCompletion.name, questName);
        if (completed && !cursor.moveToFirst()) {
            getWritableDatabase().insert(DB.QuestCompletion.tableName, null, cv);
        }
        else {
            getReadableDatabase().delete(DB.QuestCompletion.tableName, DB.QuestCompletion.name + " = ?", new String[]{ questName });
        }
        cursor.close();
    }

    public HashSet<String> getQuestCompletions() {
        String query = "SELECT * FROM " + DB.QuestCompletion.tableName;
        Cursor cursor = getReadableDatabase().rawQuery(query, null);
        HashSet<String> hashSet = new HashSet<>();

        while (cursor.moveToNext()) {
            String questName = cursor.getString(cursor.getColumnIndex(DB.QuestCompletion.name));
            hashSet.add(questName);
        }
        cursor.close();
        return hashSet;
    }

    public ArrayList<String> getBestiaryHistory() {
        String query = "SELECT * FROM " + DB.BestiaryHistory.tableName + " ORDER BY " + DB.BestiaryHistory.dateModified + " DESC";
        Cursor cursor = getReadableDatabase().rawQuery(query, null);
        ArrayList<String> history = new ArrayList<>();

        while (cursor.moveToNext()) {
            String questName = cursor.getString(cursor.getColumnIndex(DB.BestiaryHistory.name));
            history.add(questName);
        }
        cursor.close();
        return history;
    }

    public void clearBestiaryHistory() {
        getReadableDatabase().delete(DB.BestiaryHistory.tableName, null, null);
    }

    public void insertBestiaryHistory(String monsterName) {
        String query = "SELECT * FROM " + DB.BestiaryHistory.tableName + " WHERE " + DB.BestiaryHistory.name + " = ?";
        Cursor cursor = getReadableDatabase().rawQuery(query, new String[]{ monsterName });
        ContentValues cv = new ContentValues();
        cv.put(DB.BestiaryHistory.dateModified, System.currentTimeMillis());
        if (cursor.moveToFirst()) {
            getWritableDatabase().update(DB.BestiaryHistory.tableName, cv, DB.BestiaryHistory.name + " = ?", new String[]{ monsterName });
        }
        else {
            cv.put(DB.BestiaryHistory.name, monsterName);
            getWritableDatabase().insert(DB.BestiaryHistory.tableName, null, cv);
        }
        cursor.close();
    }
}