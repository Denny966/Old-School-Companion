package com.dennyy.osrscompanion.fragments;

import android.app.Fragment;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.dennyy.osrscompanion.R;
import com.dennyy.osrscompanion.helpers.Constants;
import com.dennyy.osrscompanion.helpers.Utils;
import com.dennyy.osrscompanion.interfaces.IBackButtonHandler;
import com.github.mikephil.charting.jobs.MoveViewJob;

public class BaseFragment extends Fragment implements IBackButtonHandler.OnBackClickListener {

    protected TextView toolbarTitle;
    protected String defaultRsn;
    private Toast toast;
    private IBackButtonHandler.BackButtonHandlerInterface backButtonHandler;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null && activity.getSupportActionBar() != null) {
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        toolbarTitle = (TextView) getActivity().findViewById(R.id.toolbar_title);
        toolbarTitle.setTextColor(getResources().getColor(R.color.text));
        defaultRsn = PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(Constants.PREF_RSN, "");

        backButtonHandler = (IBackButtonHandler.BackButtonHandlerInterface) getActivity();
        backButtonHandler.addBackClickListener(this);
    }

    protected void showToast(String message, int duration) {
        if (toast != null)
            toast.cancel();
        toast = Toast.makeText(getActivity(), message, duration);
        toast.show();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        MoveViewJob.getInstance(null, 0, 0, null, null);
        if (backButtonHandler != null) {
            backButtonHandler.removeBackClickListener(this);
            backButtonHandler = null;
        }
    }

    @Override
    public boolean onBackClick() {
        Utils.hideKeyboard(getActivity());
        return false;
    }

    public Drawable getDrawable(int resourceId) {
        return getResources().getDrawable(resourceId);
    }

}
