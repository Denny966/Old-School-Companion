package com.dennyy.oldschoolcompanion.fragments.preferences;

import android.app.FragmentTransaction;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.*;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.NoConnectionError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.dennyy.oldschoolcompanion.AppController;
import com.dennyy.oldschoolcompanion.BuildConfig;
import com.dennyy.oldschoolcompanion.R;
import com.dennyy.oldschoolcompanion.asynctasks.UpdateItemIdListTask;
import com.dennyy.oldschoolcompanion.customviews.SeekBarPreference;
import com.dennyy.oldschoolcompanion.fragments.BaseFragment;
import com.dennyy.oldschoolcompanion.helpers.Constants;
import com.dennyy.oldschoolcompanion.helpers.Logger;
import com.dennyy.oldschoolcompanion.helpers.Utils;
import com.dennyy.oldschoolcompanion.interfaces.ItemIdListResultListener;
import com.dennyy.oldschoolcompanion.interfaces.SeekBarPreferenceListener;


public class UserPreferenceFragment extends PreferenceFragment implements Preference.OnPreferenceClickListener, SeekBarPreferenceListener {
    private SharedPreferences preferences;
    private Toast toast;
    private static final String ITEMIDLIST_REQUEST_TAG = "item_id_list_request";
    private static final String LAST_UPDATE_TIME_KEY = "last_item_id_list_update_time";

    public UserPreferenceFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.pref_main, container, false);
        ((TextView) view.findViewById(R.id.version_info)).setText(getActivity().getResources().getString(R.string.version_string, BuildConfig.VERSION_NAME));
        setHasOptionsMenu(true);
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_settings, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_system_settings:
                try {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getActivity().getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                }
                catch (ActivityNotFoundException ignored) {
                    startActivity(new Intent(Settings.ACTION_SETTINGS));
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private String[] getPreferenceKeys() {
        String[] prefs = new String[]{
                Constants.PREF_LANDSCAPE_ONLY,
                Constants.PREF_FULLSCREEN_ONLY,
                Constants.PREF_PADDING_SIDE,
                Constants.PREF_FLOATING_VIEWS,
                Constants.PREF_FEEDBACK,
                Constants.PREF_VIEW_IN_STORE,
                Constants.PREF_VIEW_OTHER_APPS,
                Constants.PREF_SHOW_LIBRARIES,
                Constants.PREF_DOWNLOAD_ITEMIDLIST,
                Constants.PREF_QUEST_SOURCE,
                Constants.PREF_VERSION,
                Constants.PREF_START_EXTERNAL,
                Constants.PREF_HW_ACCELERATION };
        return prefs;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        TextView toolbar = getActivity().findViewById(R.id.toolbar_title);
        toolbar.setTextColor(getResources().getColor(R.color.text));
        toolbar.setText(getResources().getString(R.string.settings));
        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        String[] prefs = getPreferenceKeys();
        for (String pref : prefs) {
            findPreference(pref).setOnPreferenceClickListener(this);
        }
        String[] seekBarPref = new String[]{ Constants.PREF_OPACITY, Constants.PREF_SIZE, Constants.PREF_PADDING };
        for (String pref : seekBarPref) {
            ((SeekBarPreference) findPreference(pref)).setListener(this);
        }
        findPreference(Constants.PREF_VERSION).setTitle(getResources().getString(R.string.version_string, BuildConfig.VERSION_NAME));
    }

    @Override
    public void onResume() {
        super.onResume();
        ((CheckBoxPreference) findPreference(Constants.PREF_RIGHT_SIDE)).setChecked(preferences.getBoolean(Constants.PREF_RIGHT_SIDE, true));
    }

    @Override
    public void onDetach() {
        super.onDetach();
        AppController.getInstance().cancelPendingRequests(ITEMIDLIST_REQUEST_TAG);
    }

    public void updateItemIdList() {
        showToast(getResources().getString(R.string.checking_for_updated_ge_items), Toast.LENGTH_LONG);
        Utils.getString(Constants.ITEMIDLIST_URL, ITEMIDLIST_REQUEST_TAG, new Utils.VolleyCallback() {
            @Override
            public void onSuccess(String result) {
                if (getActivity() == null || getActivity().isFinishing() || !isAdded()) return;
                new UpdateItemIdListTask(getActivity(), result, new ItemIdListResultListener() {
                    @Override
                    public void onItemsUpdated() {
                        showToast(getResources().getString(R.string.updated_list_of_items), Toast.LENGTH_LONG);
                    }

                    @Override
                    public void onItemsNotUpdated() {
                        showToast(getResources().getString(R.string.items_up_to_date), Toast.LENGTH_LONG);
                    }

                    @Override
                    public void onError() {
                        showToast(getResources().getString(R.string.error_please_try_again), Toast.LENGTH_LONG);
                    }
                }).execute();
            }

            @Override
            public void onError(VolleyError error) {
                if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                    showToast(getResources().getString(R.string.failed_to_obtain_data, "updated items", getResources().getString(R.string.network_error)), Toast.LENGTH_LONG);
                    return;
                }
                showToast(getResources().getString(R.string.failed_to_obtain_data, "updated items", error.getMessage()), Toast.LENGTH_LONG);
            }

            @Override
            public void always() {
                SharedPreferences.Editor editor = preferences.edit();
                editor.putLong(LAST_UPDATE_TIME_KEY, System.currentTimeMillis());
                editor.apply();
            }
        });
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        String key = preference.getKey();
        switch (key) {
            case Constants.PREF_FLOATING_VIEWS:
                openFragment(new FloatingViewSelectorFragment());
                break;
            case Constants.PREF_DOWNLOAD_ITEMIDLIST:
                long refreshPeriod = System.currentTimeMillis() - preferences.getLong(LAST_UPDATE_TIME_KEY, 0);
                if (refreshPeriod < Constants.REFRESH_COOLDOWN_LONG_MS) {
                    double timeLeft = (Constants.REFRESH_COOLDOWN_LONG_MS - refreshPeriod) / 1000;
                    showToast(getResources().getString(R.string.wait_before_refresh, (int) Math.ceil(timeLeft + 0.5)), Toast.LENGTH_SHORT);
                }
                else {
                    updateItemIdList();
                }
                break;
            case Constants.PREF_FULLSCREEN_ONLY:
            case Constants.PREF_LANDSCAPE_ONLY:
            case Constants.PREF_HW_ACCELERATION:
                showToast(getString(R.string.rotate_to_take_effect), Toast.LENGTH_SHORT);
                break;
            case Constants.PREF_FEEDBACK:
                final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);

                /* Fill it with Data */
                emailIntent.setType("message/rfc822");
                emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{ "info@dennyy.com" });
                emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getResources().getString(R.string.app_name) + " " + BuildConfig.VERSION_NAME);

                /* Send it off to the Activity-Chooser */
                getActivity().startActivity(Intent.createChooser(emailIntent, getResources().getString(R.string.choose_mail_app)));
                break;
            case Constants.PREF_VIEW_IN_STORE:
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + BuildConfig.APPLICATION_ID)));
                }
                catch (ActivityNotFoundException anfe) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID)));
                }
                break;
            case Constants.PREF_VIEW_OTHER_APPS:
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=pub:Dennyy")));
                }
                catch (ActivityNotFoundException anfe) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/developer?id=Dennyy")));
                }
                break;
            case Constants.PREF_SHOW_LIBRARIES:
                openFragment(new LibrariesFragment());
                break;
            case Constants.PREF_PADDING_SIDE:
                showToast(getResources().getString(R.string.restart_to_take_effect), Toast.LENGTH_LONG);
                break;
            case Constants.PREF_VERSION:
                try {
                    Utils.showChangelogs(getActivity());
                }
                catch (Exception e) {
                    Logger.log("showing changelogs from preferencefragment", e);
                    showToast(getResources().getString(R.string.error_please_try_again), Toast.LENGTH_SHORT);
                }
                break;
            case Constants.PREF_START_EXTERNAL:
                openFragment(new StartExternalInfoFragment());
                break;
        }
        return false;
    }

    private void openFragment(BaseFragment fragment) {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onSeekBarValueSet(SeekBarPreference preference, String key, int value) {
        showToast(getResources().getString(R.string.restart_to_take_effect), Toast.LENGTH_LONG);
    }

    @Override
    public void onSeekBarCancel(SeekBarPreference preference, String key) {

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

}
