package com.dennyy.oldschoolcompanion.fragments.calculators;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.dennyy.oldschoolcompanion.R;
import com.dennyy.oldschoolcompanion.enums.HiscoreType;
import com.dennyy.oldschoolcompanion.fragments.BaseFragment;
import com.dennyy.oldschoolcompanion.helpers.Utils;
import com.dennyy.oldschoolcompanion.interfaces.DiariesLoadedListener;
import com.dennyy.oldschoolcompanion.viewhandlers.DiaryCalculatorViewHandler;
import com.dennyy.oldschoolcompanion.models.AchievementDiary.DiariesMap;

public class DiaryCalculatorFragment extends BaseFragment {

    private static final String WAS_REQUESTING_KEY = "was_requesting_key";
    private static final String HISCORE_TYPE_KEY = "hiscore_type_key";
    private static final String HISCORE_DATA_KEY = "hiscore_data_key";
    private static final String LAST_EXPANDED_INDEX_KEY = "last_expanded_index_key";

    private DiaryCalculatorViewHandler diaryCalculatorViewHandler;

    public DiaryCalculatorFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        view = inflater.inflate(R.layout.diary_calculator_layout, container, false);
        return view;
    }

    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        toolbarTitle.setText(getResources().getString(R.string.diary_calculator));

        diaryCalculatorViewHandler = new DiaryCalculatorViewHandler(getActivity(), view, new DiariesLoadedListener() {
            @Override
            public void onDiariesLoaded(DiariesMap ignored) {
                loadFragment(savedInstanceState);
            }

            @Override
            public void onDiariesLoadError() {
                // handled in viewhandler
            }
        });

    }

    private void loadFragment(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            diaryCalculatorViewHandler.selectedHiscoreType = HiscoreType.fromValue(savedInstanceState.getInt(HISCORE_TYPE_KEY));
            diaryCalculatorViewHandler.hiscoresData = savedInstanceState.getString(HISCORE_DATA_KEY);
            diaryCalculatorViewHandler.lastExpandedPosition = savedInstanceState.getInt(LAST_EXPANDED_INDEX_KEY);

            diaryCalculatorViewHandler.updateIndicators();
            if (savedInstanceState.getBoolean(WAS_REQUESTING_KEY)) {
                diaryCalculatorViewHandler.updateUser();
            }
            else if (!Utils.isNullOrEmpty(diaryCalculatorViewHandler.hiscoresData)) {
                diaryCalculatorViewHandler.handleHiscoresData(diaryCalculatorViewHandler.hiscoresData);
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(HISCORE_TYPE_KEY, diaryCalculatorViewHandler.selectedHiscoreType.getValue());
        outState.putString(HISCORE_DATA_KEY, diaryCalculatorViewHandler.hiscoresData);
        outState.putInt(LAST_EXPANDED_INDEX_KEY, diaryCalculatorViewHandler.lastExpandedPosition);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_diary, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                if (diaryCalculatorViewHandler.allowUpdateUser())
                    diaryCalculatorViewHandler.updateUser();
                return true;
            case R.id.action_diary_info:
                Utils.showDialog(getActivity(), getActivity().getString(R.string.diary_calculator), getString(R.string.diary_dialog_info));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        diaryCalculatorViewHandler.cancelRunningTasks();
    }
}
