package com.dennyy.oldschoolcompanion.fragments;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dennyy.oldschoolcompanion.R;
import com.dennyy.oldschoolcompanion.viewhandlers.NotesViewHandler;
import com.dennyy.oldschoolcompanion.models.Notes.NoteChangeEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

public class NotesFragment extends BaseFragment {

    private static final String NOTE_KEY = "NOTE_KEY";

    private NotesViewHandler notesViewHandler;

    public NotesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        view = inflater.inflate(R.layout.notes_layout, container, false);

        return view;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        toolbarTitle.setText(getResources().getString(R.string.notes));

        notesViewHandler = new NotesViewHandler(getActivity(), view, false);
        if (savedInstanceState != null) {
            notesViewHandler.setNote(savedInstanceState.getString(NOTE_KEY));
        }
    }

    @Subscribe
    public void onNoteChangeEvent(NoteChangeEvent event) {
        if (notesViewHandler != null && event.isFloatingView) {
            notesViewHandler.setNote(event.note);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (notesViewHandler != null) {
            outState.putString(NOTE_KEY, notesViewHandler.getNote());
        }
    }
}

