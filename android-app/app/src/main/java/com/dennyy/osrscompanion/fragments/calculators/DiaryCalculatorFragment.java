package com.dennyy.osrscompanion.fragments.calculators;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.dennyy.osrscompanion.R;
import com.dennyy.osrscompanion.enums.HiscoreType;
import com.dennyy.osrscompanion.fragments.BaseFragment;
import com.dennyy.osrscompanion.helpers.Utils;
import com.dennyy.osrscompanion.layouthandlers.DiaryCalculatorViewHandler;

public class DiaryCalculatorFragment extends BaseFragment {

    private static final String WAS_REQUESTING_KEY = "was_requesting_key";
    private static final String HISCORE_TYPE_KEY = "hiscore_type_key";
    private static final String HISCORE_DATA_KEY = "hiscore_data_key";
    private static final String LAST_EXPANDED_INDEX_KEY = "last_expanded_index_key";

    private DiaryCalculatorViewHandler diaryCalculatorViewHandler;
    private View view;


    public DiaryCalculatorFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        view = inflater.inflate(R.layout.diary_calculator_layout, container, false);
        return view;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        toolbarTitle.setText(getResources().getString(R.string.diary_calculator));

        diaryCalculatorViewHandler = new DiaryCalculatorViewHandler(getActivity(), view);
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
        diaryCalculatorViewHandler.cancelVolleyRequests();
    }
}
