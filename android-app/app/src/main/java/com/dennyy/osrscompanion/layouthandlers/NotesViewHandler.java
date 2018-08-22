package com.dennyy.osrscompanion.layouthandlers;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

import com.dennyy.osrscompanion.R;
import com.dennyy.osrscompanion.helpers.Constants;
import com.dennyy.osrscompanion.helpers.Utils;
import com.dennyy.osrscompanion.models.Notes.NoteChangeEvent;

import org.greenrobot.eventbus.EventBus;

import java.lang.ref.WeakReference;

public class NotesViewHandler extends BaseViewHandler implements TextWatcher {
    private String note;
    private EditText notesEditText;
    private final Handler handler = new Handler();
    private Runnable runnable;

    public NotesViewHandler(Context context, View view) {
        super(context, view);
        notesEditText = view.findViewById(R.id.notes_edittext);
        new LoadNote(context, notesEditText).execute();
        notesEditText.addTextChangedListener(this);
    }

    public String getNote() {
        return notesEditText.getText().toString();
    }

    public void setNote(String note) {
        notesEditText.setTag("");
        notesEditText.setText(note);
        notesEditText.setTag(null);
    }

    @Override
    public void afterTextChanged(final Editable s) {
        note = s.toString();
        handler.removeCallbacks(runnable);
        if (notesEditText.getTag() != null) {
            return;
        }
        runnable = new Runnable() {
            @Override
            public void run() {
                saveAndPublishNotes();
            }
        };
        handler.postDelayed(runnable, 500);
    }

    private void saveAndPublishNotes() {
        note = String.format("%s%s", note, Utils.repeat("\n", getNewlinesToAppend()));
        final int cursorPosition = notesEditText.getSelectionStart();
        new WriteNote(context).execute(note);
        notesEditText.post(new Runnable() {
            @Override
            public void run() {
                int selection = Math.min(notesEditText.getText().toString().length(), cursorPosition);
                notesEditText.setSelection(selection);
            }
        });
        EventBus.getDefault().post(new NoteChangeEvent(note));
    }

    private int getNewlinesToAppend() {
        // Append 10 newlines at the end for floating view service for when the keyboard hides the view
        int newLinesAppend = 10;
        String lastCharacters = note.substring(Math.max(0, note.length() - newLinesAppend));

        int existingNewLines = 0;
        for (int i = 0; i < lastCharacters.length(); i++) {
            char c = lastCharacters.charAt(i);
            if (c == '\n') {
                existingNewLines++;
            }
            else {
                existingNewLines = 0;
            }
        }
        int newlinesToAppend = newLinesAppend - existingNewLines;
        return newlinesToAppend;
    }


    private static class LoadNote extends AsyncTask<String, Void, String> {
        private WeakReference<Context> context;
        private WeakReference<EditText> notesEditText;

        private LoadNote(Context context, EditText notesEditText) {
            this.context = new WeakReference<>(context);
            this.notesEditText = new WeakReference<>(notesEditText);
        }

        @Override
        protected String doInBackground(String... params) {
            Context context = this.context.get();
            String note = "";
            if (context != null)
                note = Utils.readFromFile(context, Constants.NOTES_FILE_NAME);
            return note;
        }

        @Override
        protected void onPostExecute(String note) {
            final EditText editText = notesEditText.get();
            if (editText != null) {
                editText.setText(note);
            }
        }
    }

    private static class WriteNote extends AsyncTask<String, Void, Void> {
        private WeakReference<Context> context;

        private WriteNote(Context context) {
            this.context = new WeakReference<>(context);
        }

        @Override
        protected Void doInBackground(String... params) {
            Context context = this.context.get();
            if (context != null) {
                Utils.writeToFile(context, Constants.NOTES_FILE_NAME, params[0]);
            }
            return null;
        }
    }

    @Override
    public boolean wasRequesting() {
        return false;
    }

    @Override
    public void cancelVolleyRequests() {

    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }
}