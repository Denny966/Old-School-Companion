package com.dennyy.oldschoolcompanion.fragments;

import android.os.Bundle;
import android.view.*;

import com.dennyy.oldschoolcompanion.R;
import com.dennyy.oldschoolcompanion.helpers.Utils;
import com.dennyy.oldschoolcompanion.viewhandlers.BestiaryViewHandler;

public class BestiaryFragment extends BaseFragment {
    private static final String NPC_NAME_KEY = "npc_name_key";

    private BestiaryViewHandler bestiaryViewHandler;

    public BestiaryFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        view = inflater.inflate(R.layout.bestiary_layout, container, false);
        return view;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_bestiary, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_bestiary_history:
                if (bestiaryViewHandler != null) {
                    bestiaryViewHandler.showHistory();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        toolbarTitle.setText(getResources().getString(R.string.bestiary));

        bestiaryViewHandler = new BestiaryViewHandler(getActivity(), view, false);
        if (savedInstanceState != null) {
            String npcName = savedInstanceState.getString(NPC_NAME_KEY);
            if (!Utils.isNullOrEmpty(npcName)) {
                bestiaryViewHandler.loadNpc(npcName);
            }
        }
    }

    @Override
    public boolean onBackClick() {
        if (bestiaryViewHandler != null) {
            return bestiaryViewHandler.handleBackClick();
        }
        return super.onBackClick();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (bestiaryViewHandler != null) {
            outState.putString(NPC_NAME_KEY, bestiaryViewHandler.getNpcName());
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        bestiaryViewHandler.cancelRunningTasks();
    }
}