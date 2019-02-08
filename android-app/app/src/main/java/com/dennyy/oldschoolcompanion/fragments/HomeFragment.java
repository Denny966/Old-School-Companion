package com.dennyy.oldschoolcompanion.fragments;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.view.*;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;
import com.dennyy.oldschoolcompanion.FloatingViewService;
import com.dennyy.oldschoolcompanion.R;
import com.dennyy.oldschoolcompanion.customviews.CheckboxDialogPreference;
import com.dennyy.oldschoolcompanion.enums.AppStart;
import com.dennyy.oldschoolcompanion.fragments.calculators.CalculatorsFragment;
import com.dennyy.oldschoolcompanion.fragments.hiscores.HiscoresFragment;
import com.dennyy.oldschoolcompanion.fragments.preferences.UserPreferenceFragment;
import com.dennyy.oldschoolcompanion.helpers.Constants;
import com.dennyy.oldschoolcompanion.helpers.Logger;
import com.dennyy.oldschoolcompanion.helpers.Utils;
import com.dennyy.oldschoolcompanion.interfaces.AdapterTileClickListener;
import com.dennyy.oldschoolcompanion.models.General.TileData;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;

public class HomeFragment extends BaseTileFragment implements AdapterTileClickListener {
    private Switch mainSwitch;
    private long lastSwitchTimeMs;

    public HomeFragment() {
        super(2, 4);
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

        initializeChangelog();
    }

    private void initializeChangelog() {
        AppStart appStart = checkAppStart();
        String floatingViews = preferences.getString(Constants.PREF_FLOATING_VIEWS, null);
        boolean firstTimeAppUpgrade = appStart == AppStart.FIRST_TIME && !Utils.isNullOrEmpty(floatingViews);
        if (appStart == AppStart.FIRST_TIME_VERSION || firstTimeAppUpgrade) {
            try {
                Utils.showChangelogs(getActivity());
            }
            catch (Exception e) {
                Logger.log("showing changelogs from homefragment", e);
            }
        }
    }

    @Override
    public void onOptionsMenuCreated() {
        String floatingViews = preferences.getString(Constants.PREF_FLOATING_VIEWS, null);
        if (Utils.isNullOrEmpty(floatingViews)) {
            new MaterialShowcaseView.Builder(getActivity())
                    .setTarget(mainSwitch)
                    .setDismissOnTargetTouch(true)
                    .setMaskColour(Color.parseColor("#E6335075"))
                    .setDismissText(getResources().getString(R.string.got_it))
                    .setContentText(getResources().getString(R.string.first_start_info))
                    .setDelay(500)
                    .singleUse(Constants.FIRST_STARTUP)
                    .show();
        }
    }

    @Override
    public void initializeTiles() {
        if (tiles.isEmpty()) {
            tiles.add(new TileData(0, getString(R.string.grandexchange), getDrawable(R.drawable.coins)));
            tiles.add(new TileData(1, getString(R.string.tracker), getDrawable(R.drawable.tracker)));
            tiles.add(new TileData(2, getString(R.string.hiscores), getDrawable(R.drawable.hiscores)));
            tiles.add(new TileData(3, getString(R.string.calculators), getDrawable(R.drawable.calculators)));
            tiles.add(new TileData(4, getString(R.string.treasure_trails), getDrawable(R.drawable.clue_scroll_clear)));
            tiles.add(new TileData(5, getString(R.string.notes), getDrawable(R.drawable.notes)));
            tiles.add(new TileData(6, getString(R.string.quest_guide), getDrawable(R.drawable.quest_icon)));
            tiles.add(new TileData(7, getString(R.string.fairy_rings), getDrawable(R.drawable.fairy_rings)));
            tiles.add(new TileData(8, getString(R.string.osrs_wiki), getDrawable(R.drawable.rswiki_logo)));
            tiles.add(new TileData(9, getString(R.string.rsnews), getDrawable(R.drawable.newspaper)));
            tiles.add(new TileData(10, getString(R.string.timers), getDrawable(R.drawable.stopwatch)));
            tiles.add(new TileData(11, getString(R.string.worldmap), getDrawable(R.drawable.worldmap)));
            tiles.add(new TileData(12, getString(R.string.todo_list), getDrawable(R.drawable.todo)));
            tiles.add(new TileData(13, getString(R.string.bestiary), getDrawable(R.drawable.npc_examine)));
            tiles.add(new TileData(16, getString(R.string.alch_overview), getDrawable(R.drawable.alch)));
            tiles.add(new TileData(14, getString(R.string.settings), getDrawable(R.drawable.settings)));
        }
    }

    @Override
    protected void initializeGridView() {
        gridView = view.findViewById(R.id.home_grid_layout);
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);

        final String selected = preferences.getString(Constants.PREF_FLOATING_VIEWS, "");
        mainSwitch = menu.findItem(R.id.myswitch).getActionView().findViewById(R.id.switchForActionBar);

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
                            try {
                                startActivityForResult(drawIntent, Constants.CODE_DRAW_OVER_OTHER_APP_PERMISSION);
                            }
                            catch (ActivityNotFoundException ignored) {
                                startActivity(new Intent(Settings.ACTION_SETTINGS));
                            }
                        }
                    }, null);
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
    public void onTileClick(TileData tileData) {
        if (!isTransactionSafe()) {
            return;
        }
        Fragment fragment = null;
        String tag = "";
        if (tileData.text.equals(getString(R.string.grandexchange))) {
            fragment = new GrandExchangeFragment();
        }
        else if (tileData.text.equals(getString(R.string.tracker))) {
            fragment = new TrackerFragment();
        }
        else if (tileData.text.equals(getString(R.string.hiscores))) {
            fragment = new HiscoresFragment();
        }
        else if (tileData.text.equals(getString(R.string.calculators))) {
            fragment = new CalculatorsFragment();
        }
        else if (tileData.text.equals(getString(R.string.treasure_trails))) {
            fragment = new TreasureTrailFragment();
        }
        else if (tileData.text.equals(getString(R.string.notes))) {
            fragment = new NotesFragment();
        }
        else if (tileData.text.equals(getString(R.string.settings))) {
            fragment = new UserPreferenceFragment();
        }
        else if (tileData.text.equals(getString(R.string.quest_guide))) {
            fragment = new QuestFragment();
        }
        else if (tileData.text.equals(getString(R.string.fairy_rings))) {
            fragment = new FairyRingFragment();
        }
        else if (tileData.text.equals(getString(R.string.osrs_wiki))) {
            fragment = new RSWikiFragment();
        }
        else if (tileData.text.equals(getString(R.string.rsnews))) {
            fragment = new OSRSNewsFragment();
        }
        else if (tileData.text.equals(getString(R.string.timers))) {
            fragment = new TimersFragment();
        }
        else if (tileData.text.equals(getString(R.string.worldmap))) {
            fragment = new WorldmapFragment();
        }
        else if (tileData.text.equals(getString(R.string.todo_list))) {
            fragment = new TodoFragment();
        }
        else if (tileData.text.equals(getString(R.string.bestiary))) {
            fragment = new BestiaryFragment();
        }
        else if (tileData.text.equals(getString(R.string.alch_overview))) {
            fragment = new AlchOverviewFragment();
        }
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment, tag);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private AppStart checkAppStart() {
        AppStart appStart = AppStart.NORMAL;
        try {
            String packageName = getActivity().getPackageName();
            PackageInfo pInfo = getActivity().getPackageManager().getPackageInfo(packageName, 0);
            int previousVersionCode = preferences.getInt(Constants.PREVIOUS_APP_VERSION, -1);
            int currentVersionCode = pInfo.versionCode;
            appStart = checkAppStart(currentVersionCode, previousVersionCode);
            preferences.edit().putInt(Constants.PREVIOUS_APP_VERSION, currentVersionCode).apply();
        }
        catch (PackageManager.NameNotFoundException e) {
            Logger.log(e);
        }
        return appStart;
    }

    private AppStart checkAppStart(int currentVersionCode, int previousVersionCode) {
        if (previousVersionCode == -1) {
            return AppStart.FIRST_TIME;
        }
        else if (previousVersionCode < currentVersionCode) {
            return AppStart.FIRST_TIME_VERSION;
        }
        else {
            return AppStart.NORMAL;
        }
    }
}
