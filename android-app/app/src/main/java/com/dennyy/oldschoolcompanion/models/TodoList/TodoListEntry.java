package com.dennyy.oldschoolcompanion.models.TodoList;

import java.io.Serializable;

public class TodoListEntry implements Serializable {
    public final int id;
    public int sortOrder;
    public String content;
    public boolean done;

    public TodoListEntry(int id, int sortOrder, String content, boolean done) {
        this.id = id;
        this.sortOrder = sortOrder;
        this.content = content;
        this.done = done;
    }
}
