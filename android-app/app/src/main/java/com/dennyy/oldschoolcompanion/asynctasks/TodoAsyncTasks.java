package com.dennyy.oldschoolcompanion.asynctasks;

import android.content.Context;
import android.os.AsyncTask;

import com.dennyy.oldschoolcompanion.database.AppDb;
import com.dennyy.oldschoolcompanion.interfaces.TodoListLoadedListener;
import com.dennyy.oldschoolcompanion.interfaces.TodoUpdateListener;
import com.dennyy.oldschoolcompanion.models.TodoList.TodoList;
import com.dennyy.oldschoolcompanion.models.TodoList.TodoListEntry;

import java.lang.ref.WeakReference;

public final class TodoAsyncTasks {
    private TodoAsyncTasks() {

    }

    public static class UpdateSortOrder extends AsyncTask<Void, Void, Void> {
        private WeakReference<Context> weakContext;
        private TodoUpdateListener callback;
        private TodoList todoList;

        public UpdateSortOrder(final Context context, TodoList todoList, TodoUpdateListener callback) {
            this.weakContext = new WeakReference<>(context);
            this.todoList = todoList;
            this.callback = callback;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            Context context = weakContext.get();
            if (context != null) {
                AppDb.getInstance(context).updateTodoListOrder(todoList);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            callback.onActionFinished();
        }
    }

    public static class DeleteTodo extends AsyncTask<Void, Void, Void> {
        private WeakReference<Context> weakContext;
        private TodoUpdateListener callback;
        private int id;

        public DeleteTodo(final Context context, int id, TodoUpdateListener callback) {
            this.weakContext = new WeakReference<>(context);
            this.id = id;
            this.callback = callback;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            Context context = weakContext.get();
            if (context != null) {
                AppDb.getInstance(context).deleteTodo(id);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            callback.onActionFinished();
        }
    }

    public static class InsertOrUpdateTodo extends AsyncTask<Void, Void, Void> {
        private WeakReference<Context> weakContext;
        private TodoUpdateListener callback;
        private TodoListEntry entry;

        public InsertOrUpdateTodo(final Context context, TodoListEntry entry, TodoUpdateListener callback) {
            this.weakContext = new WeakReference<>(context);
            this.entry = entry;
            this.callback = callback;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            Context context = weakContext.get();
            if (context != null) {
                AppDb.getInstance(context).insertOrUpdateTodo(entry);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            callback.onActionFinished();
        }
    }

    public static class GetTodoList extends AsyncTask<Void, Void, TodoList> {
        private WeakReference<Context> weakContext;
        private TodoListLoadedListener callback;

        public GetTodoList(final Context context, final TodoListLoadedListener callback) {
            this.weakContext = new WeakReference<>(context);
            this.callback = callback;
        }

        @Override
        protected TodoList doInBackground(Void... voids) {
            Context context = weakContext.get();
            if (context == null) {
                return null;
            }
            TodoList todoList = AppDb.getInstance(weakContext.get()).getTodoList();

            return todoList;
        }

        @Override
        protected void onPostExecute(TodoList todoList) {
            if (todoList == null) {
                callback.onTodoListLoadFailed();
            }
            else {
                callback.onTodoListLoaded(todoList);
            }
        }
    }
}