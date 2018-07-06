package com.dennyy.osrscompanion.fragments;


import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import com.dennyy.osrscompanion.FloatingViewService;
import com.dennyy.osrscompanion.R;
import com.dennyy.osrscompanion.customviews.CheckboxDialogPreference;
import com.dennyy.osrscompanion.helpers.Constants;
import com.dennyy.osrscompanion.helpers.Utils;

public class HomeFragment extends BaseFragment implements View.OnClickListener {

    private long lastSwitchTimeMs;
    private View view;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        view = inflater.inflate(R.layout.home_layout, container, false);
        int[] buttons = { R.id.go_to_ge_button, R.id.go_to_hiscores_button, R.id.go_to_tracker_button, R.id.go_to_calculator_button, R.id.go_to_clues_button, R.id.go_to_notes_button, R.id.go_to_settings_button };
        for (int i : buttons) {
            view.findViewById(i).setOnClickListener(this);
        }

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        toolbarTitle.setText(getResources().getString(R.string.app_name));
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);

        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        final String selected = preferences.getString("pref_floating_views", "");

        final Switch mainSwitch = ((Switch) menu.findItem(R.id.myswitch).getActionView().findViewById(R.id.switchForActionBar));

        mainSwitch.setChecked(Utils.isMyServiceRunning(getActivity(), FloatingViewService.class));
        mainSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {


                boolean drawPermissionDisabled = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(getActivity());
                if (drawPermissionDisabled) {
                    final Intent drawIntent = new Intent(Constants.PERMISSION_ACTIVITY, Uri.parse("package:" + getActivity().getPackageName()));
                    String drawPermissionTitle = getResources().getString(R.string.draw_on_screen_permission_required);
                    showInfoDialog(drawPermissionTitle, getResources().getString(R.string.draw_dialog_info), getResources().getString(R.string.turn_on), false, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            startActivityForResult(drawIntent, Constants.CODE_DRAW_OVER_OTHER_APP_PERMISSION);
                        }
                    });
                    mainSwitch.setChecked(false);
                }
                else if (System.currentTimeMillis() - lastSwitchTimeMs < 1000) {
                    showToast(getResources().getString(R.string.wait_for_service), Toast.LENGTH_LONG);
                    mainSwitch.setChecked(!isChecked);
                }
                else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(getActivity())) {
                    showToast(getResources().getString(R.string.draw_on_screen_permission_required), Toast.LENGTH_SHORT);
                    mainSwitch.setChecked(false);
                }
                else if (isChecked && selected.length() < 1) {
                    showToast(getResources().getString(R.string.enable_floating_views_first), Toast.LENGTH_LONG);
                    mainSwitch.setChecked(false);
                }
                else {
                    Intent intent = new Intent(getActivity(), FloatingViewService.class);
                    if (isChecked && !Utils.isMyServiceRunning(getActivity(), FloatingViewService.class)) {
                        String selected = preferences.getString("pref_floating_views", "");
                        int length = selected.split(CheckboxDialogPreference.DEFAULT_SEPARATOR).length;
                        if (length < 1) {
                            showToast(getResources().getString(R.string.no_floating_views_selected), Toast.LENGTH_SHORT);
                            mainSwitch.setChecked(false);
                            return;
                        }
                        showToast(getResources().getString(R.string.service_started), Toast.LENGTH_SHORT);
                        getActivity().startService(intent);
                    }
                    else {
                        showToast(getResources().getString(R.string.service_stopped), Toast.LENGTH_SHORT);
                        getActivity().stopService(intent);
                    }
                    lastSwitchTimeMs = System.currentTimeMillis();
                }
            }
        });
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.CODE_DRAW_OVER_OTHER_APP_PERMISSION) {
            boolean drawPermissionDisabled = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(getActivity());
            if (drawPermissionDisabled) {
                showToast(getResources().getString(R.string.draw_permission_not_granted), Toast.LENGTH_SHORT);
            }
            else {
                showToast(getResources().getString(R.string.draw_permission_granted), Toast.LENGTH_SHORT);
            }
        }
        else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            updateGridPosition(view.findViewById(R.id.go_to_settings_button), 3, 0);
        }
        else {
            updateGridPosition(view.findViewById(R.id.go_to_settings_button), 2, 0);
        }
    }

    private void updateGridPosition(View view, int row, int column) {
        GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams(view.getLayoutParams());
        layoutParams.columnSpec = GridLayout.spec(column, 1, GridLayout.FILL);
        layoutParams.rowSpec = GridLayout.spec(row, 1, GridLayout.FILL);
        view.setLayoutParams(layoutParams);
        view.invalidate();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        Fragment fragment = null;
        String tag = "";
        switch (id) {
            case R.id.go_to_ge_button:
                fragment = new GrandExchangeFragment();
                break;
            case R.id.go_to_tracker_button:
                fragment = new TrackerFragment();
                break;
            case R.id.go_to_hiscores_button:
                fragment = new HiscoresFragmentViewPager();
                break;
            case R.id.go_to_calculator_button:
                fragment = new CalculatorFragment();
                break;
            case R.id.go_to_clues_button:
                fragment = new TreasureTrailFragment();
                break;
            case R.id.go_to_notes_button:
                fragment = new NotesFragment();
                break;
            case R.id.go_to_settings_button:
                fragment = new UserPreferenceFragment();
                break;
        }
        transaction.replace(R.id.fragment_container, fragment, tag);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void showInfoDialog(String title, String message, String positiveButtonText, boolean cancelable, DialogInterface.OnClickListener listener) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());

        alertDialogBuilder.setTitle(title);

        alertDialogBuilder.setMessage(message).setCancelable(cancelable).setPositiveButton(positiveButtonText, listener).setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        alertDialogBuilder.create().show();
    }
}
