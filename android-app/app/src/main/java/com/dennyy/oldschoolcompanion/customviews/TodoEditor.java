package com.dennyy.oldschoolcompanion.customviews;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import com.dennyy.oldschoolcompanion.R;
import com.dennyy.oldschoolcompanion.interfaces.TodoEditorListener;
import com.dennyy.oldschoolcompanion.models.TodoList.TodoListEntry;

public class TodoEditor extends ScrollView implements View.OnClickListener {
    private EditText todoInput;
    private TextView todoTitle;
    private TodoEditorListener listener;
    private int id;
    private int sortOrder;
    private boolean done;

    public TodoEditor(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.todo_editor, this);
        id = -1;
        sortOrder = -1;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        todoInput = findViewById(R.id.todo_input);
        todoTitle = findViewById(R.id.add_todo_item);
        findViewById(R.id.todo_save).setOnClickListener(this);
        findViewById(R.id.todo_cancel).setOnClickListener(this);
    }

    public void setListener(TodoEditorListener listener) {
        this.listener = listener;
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (listener == null)
            return;
        if (id == R.id.todo_save) {
            listener.onTodoEditorSave(todoInput.getText().toString(), sortOrder, done);
        }
        else if (id == R.id.todo_cancel) {
            clear();
            listener.onTodoEditorCancel();
        }
    }

    public void clear() {
        this.id = -1;
        this.sortOrder = -1;
        this.done = false;
        todoInput.setText(null);
        todoTitle.setText(getResources().getString(R.string.add_todo_item));
    }

    public void setContent(TodoListEntry entry) {
        this.id = entry.id;
        this.sortOrder = entry.sortOrder;
        this.done = entry.done;
        todoInput.setText(entry.content);
        todoTitle.setText(getResources().getString(R.string.edit_todo_item));
    }

    public int getEntryId() {
        return id;
    }

    public boolean isNewEntry() {
        return id == -1;
    }

    public void hide() {
        setVisibility(GONE);
    }

    public void show() {
        setVisibility(VISIBLE);
    }

    public boolean isHidden() {
        return getVisibility() != VISIBLE;
    }

    public EditText getInputView() {
        return todoInput;
    }
}
