package com.dennyy.oldschoolcompanion.fragments;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.dennyy.oldschoolcompanion.R;
import com.dennyy.oldschoolcompanion.models.TodoList.ReloadTodoListEvent;
import com.dennyy.oldschoolcompanion.viewhandlers.TodoViewHandler;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

public class TodoFragment extends BaseFragment {
    private TodoViewHandler todoViewHandler;

    public TodoFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        view = inflater.inflate(R.layout.todo_layout, container, false);
        return view;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        toolbarTitle.setText(getResources().getString(R.string.todo_list));
        todoViewHandler = new TodoViewHandler(getActivity(), view, false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_todo, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (todoViewHandler != null) {
            if (id == R.id.action_add_todo) {
                todoViewHandler.clearEditor();
                todoViewHandler.openAddTodoView();
            }
            else if (id == R.id.action_todo_edit_mode) {
                todoViewHandler.toggleEditMode();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        todoViewHandler.cancelRunningTasks();
    }

    @Subscribe
    public void reloadTodoList(ReloadTodoListEvent event) {
        if (todoViewHandler != null && (event.isFloatingView || event.forceReload)) {
            todoViewHandler.reloadTodoList();
        }
    }

    @Override
    public boolean onBackClick() {
        if (todoViewHandler != null && todoViewHandler.isEditorOpen()) {
            todoViewHandler.onTodoEditorCancel();
            return true;
        }
        return super.onBackClick();
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }
}