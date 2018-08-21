package com.dennyy.osrscompanion.fragments;


import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.GridView;
import android.widget.Switch;
import android.widget.Toast;

import com.dennyy.osrscompanion.FloatingViewService;
import com.dennyy.osrscompanion.R;
import com.dennyy.osrscompanion.adapters.TileAdapter;
import com.dennyy.osrscompanion.customviews.CheckboxDialogPreference;
import com.dennyy.osrscompanion.fragments.calculators.CalculatorsFragment;
import com.dennyy.osrscompanion.helpers.Constants;
import com.dennyy.osrscompanion.helpers.Utils;
import com.dennyy.osrscompanion.models.General.TileData;

import java.util.ArrayList;

public class HomeFragment extends BaseTileFragment implements AdapterView.OnItemClickListener{

    private long lastSwitchTimeMs;
    private View view;
    private ArrayList<TileData> homeTiles;

    public HomeFragment() {
        super(2, 4);
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        view = inflater.inflate(R.layout.home_layout, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        toolbarTitle.setText(getResources().getString(R.string.app_name));
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        initializeTiles();
    }

    @Override
    public void initializeTiles() {
        homeTiles = new ArrayList<>();
        homeTiles.add(new TileData(getString(R.string.grandexchange), getDrawable(R.drawable.coins)));
        homeTiles.add(new TileData(getString(R.string.tracker), getDrawable(R.drawable.tracker)));
        homeTiles.add(new TileData(getString(R.string.hiscores), getDrawable(R.drawable.hiscores)));
        homeTiles.add(new TileData(getString(R.string.calculators), getDrawable(R.drawable.calculators)));
        homeTiles.add(new TileData(getString(R.string.clue_scrolls), getDrawable(R.drawable.clue_scroll_clear)));
        homeTiles.add(new TileData(getString(R.string.notes), getDrawable(R.drawable.notes)));
        homeTiles.add(new TileData(getString(R.string.quest_guide), getDrawable(R.drawable.quest_icon)));
        homeTiles.add(new TileData(getString(R.string.fairy_rings), getDrawable(R.drawable.fairy_rings)));
        homeTiles.add(new TileData(getString(R.string.osrs_wiki), getDrawable(R.drawable.rswiki_logo)));
        homeTiles.add(new TileData(getString(R.string.settings), getDrawable(R.drawable.settings)));

        TileAdapter tileAdapter = new TileAdapter(getActivity(), homeTiles);
        GridView gridView = view.findViewById(R.id.home_grid_layout);
        gridView.setNumColumns(currentColumns);
        gridView.setAdapter(tileAdapter);
        gridView.setOnItemClickListener(this);
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
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        TileData tileData = this.homeTiles.get(i);
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        Fragment fragment = null;
        String tag = "";
        if (tileData.text.equals(getString(R.string.grandexchange))) {
            fragment = new GrandExchangeFragment();
        }
        if (tileData.text.equals(getString(R.string.tracker))) {
            fragment = new TrackerFragment();
        }
        if (tileData.text.equals(getString(R.string.hiscores))) {
            fragment = new HiscoresFragmentViewPager();
        }
        if (tileData.text.equals(getString(R.string.calculators))) {
            fragment = new CalculatorsFragment();
        }
        if (tileData.text.equals(getString(R.string.clue_scrolls))) {
            fragment = new TreasureTrailFragment();
        }
        if (tileData.text.equals(getString(R.string.notes))) {
            fragment = new NotesFragment();
        }
        if (tileData.text.equals(getString(R.string.settings))) {
            fragment = new UserPreferenceFragment();
        }
        if (tileData.text.equals(getString(R.string.quest_guide))) {
            fragment = new QuestFragment();
        }
        if (tileData.text.equals(getString(R.string.fairy_rings))) {
            fragment = new FairyRingFragment();
        }
        if (tileData.text.equals(getString(R.string.osrs_wiki))) {
            fragment = new RSWikiFragment();
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
