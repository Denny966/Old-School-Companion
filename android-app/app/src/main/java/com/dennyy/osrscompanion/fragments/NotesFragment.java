package com.dennyy.osrscompanion.fragments;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dennyy.osrscompanion.R;
import com.dennyy.osrscompanion.helpers.Constants;
import com.dennyy.osrscompanion.layouthandlers.NotesViewHandler;

public class NotesFragment extends BaseFragment {
    private static final String NOTE_KEY = "NOTE_KEY";

    private NotesViewHandler notesViewHandler;
    private View view;
    private BroadcastReceiver receiver;

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

        notesViewHandler = new NotesViewHandler(getActivity(), view);
        if (savedInstanceState != null) {
            notesViewHandler.note = savedInstanceState.getString(NOTE_KEY);
            notesViewHandler.loadNote();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                notesViewHandler.loadNote();
            }
        };
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver((receiver), new IntentFilter(Constants.UPDATE_NOTE_ACTION));
    }

    @Override
    public void onPause() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(receiver);
        super.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(NOTE_KEY, notesViewHandler.getNote());
    }
}

