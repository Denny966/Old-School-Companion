package com.dennyy.oldschoolcompanion.models.TodoList;

import java.util.ArrayList;
import java.util.ListIterator;

public class TodoList extends ArrayList<TodoListEntry> {
    public TodoList() {

    }

    public TodoList(ArrayList<TodoListEntry> entries) {
        super(entries);
    }

    public void updateEntry(TodoListEntry updatedEntry) {
        for (TodoListEntry entry : this) {
            if (entry.id == updatedEntry.id) {
                entry.content = updatedEntry.content;
                entry.done = updatedEntry.done;
                break;
            }
        }
    }

    public void updateList(TodoList newTodoList) {
        clear();
        trimToSize();
        addAll(newTodoList);
    }

    public void deleteEntry(int id) {
        ListIterator<TodoListEntry> iterator = listIterator();
        while (iterator.hasNext()) {
            if (iterator.next().id == id) {
                iterator.remove();
            }
        }
    }

    public boolean contains(TodoListEntry entry) {
        for (TodoListEntry todoListEntry : this) {
            if (entry.id == todoListEntry.id) {
                return true;
            }
        }
        return false;
    }
}
