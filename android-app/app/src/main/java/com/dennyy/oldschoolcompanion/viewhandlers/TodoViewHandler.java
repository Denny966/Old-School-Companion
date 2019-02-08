package com.dennyy.oldschoolcompanion.viewhandlers;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.widget.Toast;

import com.dennyy.oldschoolcompanion.R;
import com.dennyy.oldschoolcompanion.adapters.TodoAdapter;
import com.dennyy.oldschoolcompanion.asynctasks.TodoAsyncTasks;
import com.dennyy.oldschoolcompanion.customviews.TodoEditor;
import com.dennyy.oldschoolcompanion.helpers.Utils;
import com.dennyy.oldschoolcompanion.interfaces.AdapterTodoClickListener;
import com.dennyy.oldschoolcompanion.interfaces.TodoEditorListener;
import com.dennyy.oldschoolcompanion.interfaces.TodoListLoadedListener;
import com.dennyy.oldschoolcompanion.interfaces.TodoUpdateListener;
import com.dennyy.oldschoolcompanion.models.TodoList.ReloadTodoListEvent;
import com.dennyy.oldschoolcompanion.models.TodoList.TodoList;
import com.dennyy.oldschoolcompanion.models.TodoList.TodoListEntry;
import com.woxthebox.draglistview.DragListView;

import org.greenrobot.eventbus.EventBus;

public class TodoViewHandler extends BaseViewHandler implements View.OnClickListener, TodoEditorListener, TodoListLoadedListener, AdapterTodoClickListener, DragListView.DragListListener, TodoUpdateListener {

    private DragListView todoListView;
    private TodoAdapter todoAdapter;
    private TodoEditor todoEditor;

    public TodoViewHandler(Context context, View view, boolean isFloatingView) {
        super(context, view, isFloatingView);

        todoListView = view.findViewById(R.id.todo_listview);
        todoListView.setDragListListener(this);
        todoListView.setLayoutManager(new LinearLayoutManager(context));
        todoListView.setCanDragHorizontally(false);
        todoListView.setDragEnabled(false);
        todoEditor = view.findViewById(R.id.todo_editor);
        todoEditor.setListener(this);

        if (isFloatingView) {
            view.findViewById(R.id.navbar).setVisibility(View.VISIBLE);
            view.findViewById(R.id.add_todo).setOnClickListener(this);
            view.findViewById(R.id.todo_edit_mode).setOnClickListener(this);
        }
        reloadTodoList();
    }

    public void reloadTodoList() {
        new TodoAsyncTasks.GetTodoList(context, this).execute();
    }

    @Override
    public void onTodoListLoaded(TodoList todoList) {
        if (todoAdapter == null) {
            todoAdapter = new TodoAdapter(context, todoList, this);
            todoListView.setAdapter(todoAdapter, true);
        }
        else {
            todoAdapter.updateList(todoList);
        }
    }

    @Override
    public void onTodoListLoadFailed() {
        showToast(getString(R.string.unexpected_error_try_reopen), Toast.LENGTH_LONG);
    }

    @Override
    public void onTodoEditorSave(String todoText, int sortOrder, boolean done) {
        if (Utils.isNullOrEmpty(todoText)) {
            showToast(getString(R.string.todo_empty_text), Toast.LENGTH_SHORT);
            return;
        }
        final boolean isNewEntry = todoEditor.isNewEntry();
        int id = todoEditor.getEntryId();
        TodoListEntry entry = new TodoListEntry(id, sortOrder, todoText, done);
        if (isNewEntry) {
            entry.sortOrder = todoAdapter.getNextSortOrder();
        }
        else {
            todoAdapter.updateEntry(entry);
        }
        onTodoEditorCancel();
        if (todoAdapter.isEditModeActivated()) {
            toggleEditMode();
        }
        new TodoAsyncTasks.InsertOrUpdateTodo(context, entry, new TodoUpdateListener() {
            @Override
            public void onActionFinished() {
                EventBus.getDefault().post(new ReloadTodoListEvent(isFloatingView, isNewEntry));
            }
        }).execute();
    }

    @Override
    public void onTodoEditorCancel() {
        hideKeyboard();
        todoEditor.hide();
        clearEditor();
        todoListView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onTodoDoneListener(TodoListEntry entry) {
        if (hasEmptyAdapter()) {
            return;
        }
        entry.done = !entry.done;
        todoAdapter.notifyDataSetChanged();
        new TodoAsyncTasks.InsertOrUpdateTodo(context, entry, this).execute();
    }

    @Override
    public void onConfirmDeleteClick(TodoListEntry entry) {
        if (hasEmptyAdapter()) {
            return;
        }
        todoAdapter.removeEntry(entry.id);
        todoAdapter.notifyDataSetChanged();
        new TodoAsyncTasks.DeleteTodo(context, entry.id, this).execute();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.add_todo) {
            clearEditor();
            openAddTodoView();
        }
        else if (id == R.id.todo_edit_mode) {
            toggleEditMode();
        }
    }

    @Override
    public void onEditClick(TodoListEntry entry) {
        if (hasEmptyAdapter()) {
            return;
        }
        todoEditor.setContent(entry);
        openAddTodoView();
    }

    public void openAddTodoView() {
        todoEditor.show();
        todoListView.setVisibility(View.GONE);
        showKeyboard(todoEditor.getInputView());
    }

    public boolean isEditorOpen() {
        return !todoEditor.isHidden();
    }

    public void toggleEditMode() {
        if (hasEmptyAdapter()) {
            return;
        }
        todoAdapter.toggleEditMode(!todoAdapter.isEditModeActivated());
        todoListView.setDragEnabled(todoAdapter.isEditModeActivated());
    }

    public void clearEditor() {
        if (todoEditor != null) {
            todoEditor.clear();
        }
    }

    private boolean hasEmptyAdapter() {
        if (todoAdapter == null) {
            showToast(getString(R.string.unexpected_error_try_reopen), Toast.LENGTH_SHORT);
            return true;
        }
        return false;
    }

    @Override
    public void onItemDragStarted(int position) {

    }

    @Override
    public void onItemDragging(int itemPosition, float x, float y) {

    }

    @Override
    public void onItemDragEnded(int fromPosition, int toPosition) {
        if (hasEmptyAdapter()) {
            return;
        }
        new TodoAsyncTasks.UpdateSortOrder(context, todoAdapter.getTodoList(), this).execute();
    }

    @Override
    public void onActionFinished() {
        EventBus.getDefault().post(new ReloadTodoListEvent(isFloatingView));
    }

    @Override
    public boolean wasRequesting() {
        return false;
    }

    @Override
    public void cancelRunningTasks() {

    }
}