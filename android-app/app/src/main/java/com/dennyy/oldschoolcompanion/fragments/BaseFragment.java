package com.dennyy.oldschoolcompanion.fragments;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.dennyy.oldschoolcompanion.R;
import com.dennyy.oldschoolcompanion.helpers.Constants;
import com.dennyy.oldschoolcompanion.helpers.Utils;
import com.dennyy.oldschoolcompanion.interfaces.IBackButtonHandler;
import com.dennyy.oldschoolcompanion.interfaces.OptionsMenuListener;
import com.github.mikephil.charting.jobs.MoveViewJob;

public class BaseFragment extends Fragment implements IBackButtonHandler.OnBackClickListener, OptionsMenuListener {

    protected View view;
    protected TextView toolbarTitle;
    protected String defaultRsn;
    protected SharedPreferences preferences;

    private Toast toast;
    private IBackButtonHandler.BackButtonHandlerInterface backButtonHandler;
    private boolean isTransactionSafe;
    private OptionsMenuListener optionsMenuListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        optionsMenuListener = this;
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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        optionsMenuListener.onOptionsMenuCreated();
    }

    @Override
    public void onOptionsMenuCreated() {

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        toolbarTitle = getActivity().findViewById(R.id.toolbar_title);
        toolbarTitle.setTextColor(getResources().getColor(R.color.text));
        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        defaultRsn = preferences.getString(Constants.PREF_RSN, "");

        backButtonHandler = (IBackButtonHandler.BackButtonHandlerInterface) getActivity();
        backButtonHandler.addBackClickListener(this);
    }

    protected void showToast(String message, int duration) {
        if (toast != null) {
            toast.cancel();
        }
        if (getActivity() == null || getActivity().isFinishing() || !isAdded()) {
            return;
        }
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

    protected void showInfoDialog(String title, String message, String positiveButtonText, boolean cancelable, DialogInterface.OnClickListener listener, DialogInterface.OnClickListener cancelListener) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity())
                .setTitle(title)
                .setMessage(message)
                .setCancelable(cancelable)
                .setPositiveButton(positiveButtonText, listener);
        if (cancelListener != null) {
            alertDialogBuilder.setNegativeButton(getResources().getString(R.string.cancel), cancelListener);
        }
        else {
            alertDialogBuilder.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                }
            });
        }
        alertDialogBuilder.create().show();
    }

    @Override
    public void onPause() {
        super.onPause();
        isTransactionSafe = false;
    }

    @Override
    public void onResume() {
        super.onResume();
        isTransactionSafe = true;
    }

    protected boolean isTransactionSafe() {
        return isTransactionSafe;
    }
}