package com.dennyy.oldschoolcompanion.adapters;

import android.content.Context;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dennyy.oldschoolcompanion.R;
import com.dennyy.oldschoolcompanion.helpers.Utils;
import com.dennyy.oldschoolcompanion.interfaces.AdapterTodoClickListener;
import com.dennyy.oldschoolcompanion.models.TodoList.TodoList;
import com.dennyy.oldschoolcompanion.models.TodoList.TodoListEntry;
import com.woxthebox.draglistview.DragItemAdapter;

import java.util.ArrayList;

public class TodoAdapter extends DragItemAdapter<TodoListEntry, TodoAdapter.ViewHolder> {
    private TodoList todoList;
    private Context context;
    private LayoutInflater inflater;
    private AdapterTodoClickListener listener;
    private boolean editModeActivated;

    public TodoAdapter(Context context, ArrayList<TodoListEntry> entries, AdapterTodoClickListener listener) {
        this.context = context;
        this.todoList = new TodoList(entries);
        this.inflater = LayoutInflater.from(context);
        this.listener = listener;
        setItemList(this.todoList);
    }

    public void updateList(TodoList newTodoList) {
        todoList.updateList(newTodoList);
        notifyDataSetChanged();
    }

    public void updateEntry(TodoListEntry entry) {
        todoList.updateEntry(entry);
        notifyDataSetChanged();
    }

    public void removeEntry(int entryId) {
        todoList.deleteEntry(entryId);
        notifyDataSetChanged();
    }

    public int getNextSortOrder() {
        int sortOrder = getItemCount() + 1;
        for (TodoListEntry todoListEntry : todoList) {
            if (todoListEntry.sortOrder >= sortOrder) {
                sortOrder = todoListEntry.sortOrder + 1;
            }
        }
        return sortOrder;
    }

    public TodoList getTodoList() {
        return todoList;
    }

    public boolean isEditModeActivated() {
        return editModeActivated;
    }

    public void toggleEditMode(boolean activated) {
        editModeActivated = activated;
        notifyDataSetChanged();
    }

    @Override
    public long getUniqueItemId(int position) {
        return todoList.get(position).id;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.todo_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {

        final TodoListEntry entry = todoList.get(position);
        viewHolder.content.setText(entry.content);
        if (editModeActivated) {
            viewHolder.content.setAlpha(1f);
            viewHolder.doneButton.setAlpha(1f);
            viewHolder.content.setPaintFlags(viewHolder.content.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            viewHolder.doneButton.setVisibility(View.GONE);
            viewHolder.todoEditContainer.setVisibility(View.VISIBLE);
            viewHolder.dragHandler.setVisibility(View.VISIBLE);
            setMargin(viewHolder.content, (int) Utils.convertDpToPixel(50, context), (int) Utils.convertDpToPixel(200, context));
        }
        else {
            viewHolder.doneButton.setVisibility(View.VISIBLE);
            viewHolder.todoEditContainer.setVisibility(View.GONE);
            viewHolder.dragHandler.setVisibility(View.GONE);
            setMargin(viewHolder.content, 0, (int) Utils.convertDpToPixel(100, context));
            if (entry.done) {
                viewHolder.content.setPaintFlags(viewHolder.content.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                viewHolder.doneButton.setText(context.getString(R.string.todo_undo));
                viewHolder.content.setAlpha(0.5f);
                viewHolder.doneButton.setAlpha(0.5f);
            }
            else {
                viewHolder.content.setPaintFlags(viewHolder.content.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                viewHolder.doneButton.setText(context.getString(R.string.todo_done));
                viewHolder.content.setAlpha(1f);
                viewHolder.doneButton.setAlpha(1f);
            }
        }
        viewHolder.doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onTodoDoneListener(entry);
            }
        });
        viewHolder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onConfirmDeleteClick(entry);
            }
        });

        viewHolder.editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onEditClick(entry);
            }
        });
        viewHolder.itemView.setTag(mItemList.get(position));
        super.onBindViewHolder(viewHolder, position);
    }

    private void setMargin(View view, int leftMargin, int rightMargin) {
        if (view.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
            p.leftMargin = leftMargin;
            p.rightMargin = rightMargin;
            view.requestLayout();
        }
    }

    class ViewHolder extends DragItemAdapter.ViewHolder {
        public TextView content;
        public Button doneButton;
        public Button deleteButton;
        public Button editButton;
        public ImageView dragHandler;
        public LinearLayout todoEditContainer;

        public ViewHolder(View convertView) {
            super(convertView, R.id.todo_row_drag, false);
            content = convertView.findViewById(R.id.todo_row_content);
            doneButton = convertView.findViewById(R.id.todo_row_done);
            deleteButton = convertView.findViewById(R.id.todo_row_delete);
            editButton = convertView.findViewById(R.id.todo_row_edit);
            todoEditContainer = convertView.findViewById(R.id.todo_row_edit_container);
            dragHandler = convertView.findViewById(R.id.todo_row_drag);
        }
    }
}
