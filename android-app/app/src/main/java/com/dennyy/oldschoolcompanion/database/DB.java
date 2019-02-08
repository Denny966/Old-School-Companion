package com.dennyy.oldschoolcompanion.database;

public abstract class DB {

    public static final String name = "osrscompanion.db";
    public static final int version = 15;

    public static class UserStats {
        public static final String tableName = "UserStats";

        public static final String id = "id";
        public static final String rsn = "rsn";
        public static final String stats = "stats";
        public static final String hiscoreType = "hiscoreType";
        public static final String dateModified = "dateModified";
    }

    public static class Track {
        public static final String tableName = "Tracker";

        public static final String id = "id";
        public static final String rsn = "rsn";
        public static final String durationType = "durationType";
        public static final String data = "data";
        public static final String dateModified = "dateModified";
    }

    public static class GrandExchange {
        public static final String tableName = "GrandExchange";

        public static final String itemId = "itemId";
        public static final String data = "data";
        public static final String dateModified = "dateModified";
    }

    public static class GrandExchangeUpdate {
        public static final String tableName = "GrandExchangeUpdate";

        public static final String id = "id";
        public static final String data = "data";
        public static final String dateModified = "dateModified";
    }

    public static class GrandExchangeGraph {
        public static final String tableName = "GrandExchangeGraph";

        public static final String itemId = "itemId";
        public static final String data = "data";
        public static final String dateModified = "dateModified";
    }

    public static class OSBuddyExchange {
        public static final String tableName = "OSBuddyExchange";
    }

    public static class OSBuddyExchangeSummary {
        public static final String tableName = "OSBuddyExchangeSummary";

        public static final String id = "id";
        public static final String data = "data";
        public static final String dateModified = "dateModified";
    }

    public static class OSRSNews {
        public static final String tableName = "OSRSNews";

        public static final String id = "id";
        public static final String data = "data";
        public static final String dateModified = "dateModified";
    }

    public static class Timers {
        public static final String tableName = "Timers";

        public static final String id = "id";
        public static final String title = "title";
        public static final String description = "description";
        public static final String repeat = "repeat";
        public static final String interval = "interval";
        public static final String dateModified = "dateModified";
    }

    public static class Todo {
        public static final String tableName = "TodoList";

        public static final String id = "id";
        public static final String sortOrder = "sortOrder";
        public static final String content = "content";
        public static final String done = "done";
    }

    public static class Npc {
        public static final String tableName = "Npc";

        public static final String name = "name";
        public static final String data = "data";
    }

    public static class GeHistory {
        public static final String tableName = "GeHistory";

        public static final String itemId = "itemId";
        public static final String name = "name";
        public static final String isFavorite = "isFavorite";
        public static final String dateModified = "dateModified";
    }

    public static class QuestCompletion {
        public static final String tableName = "QuestCompletion";

        public static final String name = "name";
    }

    public static class BestiaryHistory {
        public static final String tableName = "BestiaryHistory";

        public static final String name = "name";
        public static final String dateModified = "dateModified";
    }
}