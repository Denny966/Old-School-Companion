package com.dennyy.oldschoolcompanion.interfaces;

import com.dennyy.oldschoolcompanion.models.TodoList.TodoList;

public interface TodoListLoadedListener {
    void onTodoListLoaded(TodoList todoList);

    void onTodoListLoadFailed();
}
