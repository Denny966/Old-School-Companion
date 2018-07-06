package com.dennyy.osrscompanion.layouthandlers;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

import com.dennyy.osrscompanion.R;
import com.dennyy.osrscompanion.helpers.Constants;
import com.dennyy.osrscompanion.helpers.Utils;

import java.lang.ref.WeakReference;

public class NotesViewHandler extends BaseViewHandler implements TextWatcher {
    public String note;

    private EditText notesEditText;
    private CountDownTimer autoSaveTimer;
    private LocalBroadcastManager broadcaster;

    public NotesViewHandler(Context context, View view) {
        super(context, view);
        notesEditText = (EditText) view.findViewById(R.id.notes_edittext);
        loadNote();
        notesEditText.addTextChangedListener(this);
        broadcaster = LocalBroadcastManager.getInstance(context);

    }

    public void loadNote() {
        notesEditText.setTag("system loaded");
        new LoadNote(context, notesEditText).execute();
    }

    public String getNote() {
        return notesEditText.getText().toString();
    }

    private void broadcastNoteUpdate() {
        Intent intent = new Intent(Constants.UPDATE_NOTE_ACTION);
        broadcaster.sendBroadcast(intent);
    }

    @Override
    public void afterTextChanged(final Editable s) {
        if (autoSaveTimer != null) {
            autoSaveTimer.cancel();
        }
        autoSaveTimer = new CountDownTimer(200, 200) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                note = s.toString();
                if (notesEditText.getTag() == null) {
                    new WriteNote(context).execute(note);
                    broadcastNoteUpdate();
                }
                notesEditText.setTag(null);
            }
        }.start();
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
            EditText editText = notesEditText.get();
            if (editText != null) {
                int cursorPosition = editText.getSelectionStart();
                editText.setText(note);
                editText.setSelection(Math.min(editText.getText().toString().length(), cursorPosition));
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
