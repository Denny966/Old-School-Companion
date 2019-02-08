package com.dennyy.oldschoolcompanion.interfaces;

import com.dennyy.oldschoolcompanion.models.TodoList.TodoListEntry;

public interface AdapterTodoClickListener {
    void onTodoDoneListener(TodoListEntry entry);

    void onConfirmDeleteClick(TodoListEntry entry);

    void onEditClick(TodoListEntry entry);
}
