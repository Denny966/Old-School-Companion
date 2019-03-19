package com.dennyy.oldschoolcompanion.viewhandlers;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;
import android.view.View;
import android.widget.*;
import com.android.volley.VolleyError;
import com.bumptech.glide.Glide;
import com.dennyy.oldschoolcompanion.AppController;
import com.dennyy.oldschoolcompanion.R;
import com.dennyy.oldschoolcompanion.adapters.BestiaryAdapter;
import com.dennyy.oldschoolcompanion.adapters.BestiaryHistoryAdapter;
import com.dennyy.oldschoolcompanion.adapters.NpcDropsAdapter;
import com.dennyy.oldschoolcompanion.adapters.NpcVersionsAdapter;
import com.dennyy.oldschoolcompanion.asynctasks.BestiaryAsyncTasks;
import com.dennyy.oldschoolcompanion.asynctasks.NpcAsyncTasks;
import com.dennyy.oldschoolcompanion.asynctasks.ReadFromAssetsTask;
import com.dennyy.oldschoolcompanion.customviews.ClearableAutoCompleteTextView;
import com.dennyy.oldschoolcompanion.customviews.DelayedAutoCompleteTextView;
import com.dennyy.oldschoolcompanion.customviews.NpcPropertyLayout;
import com.dennyy.oldschoolcompanion.helpers.Logger;
import com.dennyy.oldschoolcompanion.helpers.Utils;
import com.dennyy.oldschoolcompanion.interfaces.BestiaryListeners;
import com.dennyy.oldschoolcompanion.interfaces.ContentLoadedListener;
import com.dennyy.oldschoolcompanion.interfaces.NpcListeners;
import com.dennyy.oldschoolcompanion.interfaces.NpcWikiRequestListener;
import com.dennyy.oldschoolcompanion.models.Bestiary.Npc;
import com.dennyy.oldschoolcompanion.models.Bestiary.NpcDrop;
import me.zhanghai.android.materialprogressbar.MaterialProgressBar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

public class BestiaryViewHandler extends BaseViewHandler implements View.OnClickListener, AdapterView.OnItemSelectedListener, ContentLoadedListener, NpcWikiRequestListener, NpcListeners.NpcLoadedListener, BestiaryListeners.GetHistoryListener, BestiaryListeners.BestiaryAdapterListener {

    private static final String NPC_REQUEST_TAG = "npcrequesttag";

    private DelayedAutoCompleteTextView autoCompleteTextView;
    private BestiaryAdapter adapter;
    private ListView dropsListView;
    private NpcDropsAdapter dropsAdapter;
    private Spinner versionSpinner;
    private NpcVersionsAdapter versionsAdapter;
    private MaterialProgressBar progressBar;
    private Npc npc;
    private BestiaryHistoryAdapter bestiaryHistoryAdapter;
    private LinearLayout bestiaryHistoryContainer;
    private ListView bestiaryListView;


    public BestiaryViewHandler(final Context context, View view, boolean isFloatingView) {
        super(context, view, isFloatingView);
        dropsListView = view.findViewById(R.id.drops_listview);
        versionSpinner = view.findViewById(R.id.npc_version_spinner);
        bestiaryHistoryContainer = view.findViewById(R.id.bestiary_history_container);
        bestiaryListView = bestiaryHistoryContainer.findViewById(R.id.bestiary_history_listview);
        autoCompleteTextView = ((ClearableAutoCompleteTextView) view.findViewById(R.id.npc_search_input)).getAutoCompleteTextView();
        progressBar = view.findViewById(R.id.loading_spinner);

        versionSpinner.setSelection(0, false);
        versionSpinner.setOnItemSelectedListener(this);
        view.findViewById(R.id.show_drops_button).setOnClickListener(this);
        view.findViewById(R.id.npc_force_refresh_button).setOnClickListener(this);
        bestiaryHistoryContainer.findViewById(R.id.bestiary_history_clear).setOnClickListener(this);

        new ReadFromAssetsTask(context, "monster_list.txt", this).execute();
        new BestiaryAsyncTasks.GetHistory(context, this).execute();
        autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long rowId) {
                Utils.hideKeyboard(context, autoCompleteTextView);
                autoCompleteTextView.forceDismissDropdown();
                String search = adapter.getItem(position);
                loadNpc(search);
                toggleProgressBar(true);
            }
        });

        autoCompleteTextView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                return false;
            }
        });

        if (isFloatingView) {
            View navBar = view.findViewById(R.id.navbar);
            navBar.setVisibility(View.VISIBLE);
            navBar.findViewById(R.id.navbar_back).setOnClickListener(this);
        }
    }

    @Override
    public void onContentLoaded(String content) {
        ArrayList<String> data = new ArrayList<>(Arrays.asList(content.split(",")));
        if (adapter == null) {
            adapter = new BestiaryAdapter(context, data, this);
            autoCompleteTextView.setAdapter(adapter);
        }
        else {
            adapter.updateList(data);
        }
    }


    @Override
    public void onBestiaryHistoryLoaded(ArrayList<String> monsters) {
        if (bestiaryHistoryAdapter == null) {
            bestiaryHistoryAdapter = new BestiaryHistoryAdapter(context, monsters, this);
            bestiaryListView.setAdapter(bestiaryHistoryAdapter);
        }
        else {
            bestiaryHistoryAdapter.updateList(monsters);
        }
    }

    @Override
    public void onClickMonsterName(String name) {
        autoCompleteTextView.setText(name);
        autoCompleteTextView.forceDismissDropdown();
        loadNpc(name);
    }

    public void loadNpc(String search) {
        new NpcAsyncTasks.GetNpcData(context, search, BestiaryViewHandler.this).execute();
    }

    @Override
    public void onNpcLoaded(String npcName, String data) {
        updateNpc(npcName, data);
        toggleProgressBar(false);
        toggleCacheMessage(true);
    }

    @Override
    public void onNpcLoadFailed(String npcName) {
        lookupNpc(npcName, false);
    }

    private void lookupNpc(final String npcName, final boolean forceReload) {
        String url = String.format("https://oldschool.runescape.wiki/api.php?format=json&action=parse&page=%1$s&prop=wikitext&redirects=1", Utils.getEncodedString(npcName));
        wasRequesting = true;
        Utils.getString(url, NPC_REQUEST_TAG, new Utils.VolleyCallback() {
            @Override
            public void onSuccess(String result) {
                updateNpc(npcName, result);
                toggleCacheMessage(false);
                if (forceReload) {
                    showToast(getString(R.string.npc_data_updated), Toast.LENGTH_SHORT);
                }
            }

            @Override
            public void onError(VolleyError error) {
                String statusCode = String.valueOf(Utils.getStatusCode(error));
                showToast(getString(R.string.failed_to_obtain_data, "rswiki npc data", statusCode), Toast.LENGTH_LONG);
            }

            @Override
            public void always() {
                wasRequesting = false;
                toggleProgressBar(false);
            }
        });
    }

    private void updateNpc(String npcName, String npcData) {
        npc = Npc.fromJson(context, npcName, npcData);
        if (npc.successfulBuild) {
            new NpcAsyncTasks.InsertNpcData(context, npcName, npcData, null).execute();
            updateVersionsListView(npc.versions);
            updateDropsListView(npc.drops);
            versionSpinner.setSelection(0, false);
            updateNpc(0);
            showMonsterDetails();
            new BestiaryAsyncTasks.InsertHistory(context, npcName, this).execute();
        }
        else {
            new NpcAsyncTasks.DeleteMonsterData(context, npcName).execute();
            Logger.log(npcData, new IllegalArgumentException(String.format("failed to build npc: %s", npcName)));
            showToast(getString(R.string.npc_build_failed), Toast.LENGTH_LONG);
        }
    }

    private void updateNpc(int versionIndex) {
        if (npc == null) {
            showToast(getString(R.string.unexpected_error_try_reopen), Toast.LENGTH_SHORT);
            return;
        }
        Glide.with(context).load("https://oldschool.runescape.wiki/wiki/Special:Redirect/file/" + getIndexOrLast(npc.img, versionIndex)).into((ImageView) view.findViewById(R.id.npc_img));
        updateTextView(R.id.npc_name, npc.name);
        updateTextView(R.id.npc_levels, getIndexOrLast(npc.combat, versionIndex));
        updateTextView(R.id.npc_hp, getIndexOrLast(npc.hitpoints, versionIndex));
        updateTextView(R.id.npc_members, npc.members);
        updateTextView(R.id.npc_examine, getIndexOrLast(npc.examine, versionIndex));
        updateTextView(R.id.npc_aggressive, npc.aggressive);
        updateTextView(R.id.npc_poisonous, npc.poisonous);
        updateTextView(R.id.npc_attack_style, getIndexOrLast(npc.attackStyles, versionIndex));
        updateTextView(R.id.npc_maxhit, getIndexOrLast(npc.maxHit, versionIndex));
        updateTextView(R.id.npc_weakness, getIndexOrLast(npc.weakness, versionIndex));
        updateTextView(R.id.npc_att, getIndexOrLast(npc.att, versionIndex));
        updateTextView(R.id.npc_str, getIndexOrLast(npc.str, versionIndex));
        updateTextView(R.id.npc_def, getIndexOrLast(npc.def, versionIndex));
        updateTextView(R.id.npc_range, getIndexOrLast(npc.range, versionIndex));
        updateTextView(R.id.npc_mage, getIndexOrLast(npc.mage, versionIndex));
        updateTextView(R.id.npc_astab, getIndexOrLast(npc.astab, versionIndex));
        updateTextView(R.id.npc_aslash, getIndexOrLast(npc.aslash, versionIndex));
        updateTextView(R.id.npc_acrush, getIndexOrLast(npc.acrush, versionIndex));
        updateTextView(R.id.npc_arange, getIndexOrLast(npc.arange, versionIndex));
        updateTextView(R.id.npc_amage, getIndexOrLast(npc.amagic, versionIndex));
        updateTextView(R.id.npc_dstab, getIndexOrLast(npc.dstab, versionIndex));
        updateTextView(R.id.npc_dslash, getIndexOrLast(npc.dslash, versionIndex));
        updateTextView(R.id.npc_dcrush, getIndexOrLast(npc.dcrush, versionIndex));
        updateTextView(R.id.npc_drange, getIndexOrLast(npc.drange, versionIndex));
        updateTextView(R.id.npc_dmage, getIndexOrLast(npc.dmagic, versionIndex));
        updateTextView(R.id.npc_att_bonus, getIndexOrLast(npc.attBonus, versionIndex));
        updateTextView(R.id.npc_str_bonus, getIndexOrLast(npc.strBonus, versionIndex));
        updateTextView(R.id.npc_range_str, getIndexOrLast(npc.rangeBonus, versionIndex));
        updateTextView(R.id.npc_mage_str, getIndexOrLast(npc.mageBonus, versionIndex));
    }

    private void updateTextView(int id, String text) {
        View textView = view.findViewById(id);
        if (textView instanceof NpcPropertyLayout) {
            ((NpcPropertyLayout) textView).setValue(text);
        }
        else if (textView instanceof TextView) {
            ((TextView) textView).setText(Utils.isNullOrEmpty(text) ? "?" : text);
        }
    }

    private <T> T getIndexOrLast(ArrayList<T> list, int index) {
        if (list == null || list.size() < 1) return null;
        if (list.size() <= index) {
            return list.get(Math.max(0, list.size() - 1));
        }
        return list.get(index);
    }

    private void updateVersionsListView(ArrayList<String> versions) {
        if (versionsAdapter == null) {
            versionsAdapter = new NpcVersionsAdapter(context, versions);
            versionSpinner.setAdapter(versionsAdapter);
        }
        else {
            versionsAdapter.updateList(versions);
        }

        versionSpinner.setVisibility(versions.size() > 1 ? View.VISIBLE : View.GONE);
    }

    private void updateDropsListView(ArrayList<NpcDrop> drops) {
        Collections.sort(drops, new Comparator<NpcDrop>() {
            @Override
            public int compare(NpcDrop o1, NpcDrop o2) {
                return o1.rarity.compareTo(o2.rarity);
            }
        });
        if (dropsAdapter == null) {
            dropsAdapter = new NpcDropsAdapter(context, drops);
            dropsListView.setAdapter(dropsAdapter);
        }
        else {
            dropsAdapter.updateList(drops);
        }
    }

    @Override
    public boolean wasRequesting() {
        return wasRequesting;
    }

    @Override
    public void cancelRunningTasks() {
        AppController.getInstance().cancelPendingRequests(NPC_REQUEST_TAG);
    }

    public void showMonsterDetails() {
        view.findViewById(R.id.monster_detail_scrollview).setVisibility(View.VISIBLE);
        view.findViewById(R.id.monster_lookup_container).setVisibility(View.VISIBLE);
        view.findViewById(R.id.monster_info_layout).setVisibility(View.VISIBLE);
        view.findViewById(R.id.monster_stats_layout).setVisibility(View.VISIBLE);
        view.findViewById(R.id.monster_cache_layout).setVisibility(View.VISIBLE);
        view.findViewById(R.id.listview_container).setVisibility(View.GONE);
        bestiaryHistoryContainer.setVisibility(View.GONE);
    }

    public void showMonsterDrops() {
        view.findViewById(R.id.monster_detail_scrollview).setVisibility(View.GONE);
        view.findViewById(R.id.listview_container).setVisibility(View.VISIBLE);
        bestiaryHistoryContainer.setVisibility(View.GONE);
    }

    public void showHistory() {
        view.findViewById(R.id.monster_detail_scrollview).setVisibility(View.VISIBLE);
        view.findViewById(R.id.monster_lookup_container).setVisibility(View.VISIBLE);
        view.findViewById(R.id.monster_info_layout).setVisibility(View.GONE);
        view.findViewById(R.id.monster_stats_layout).setVisibility(View.GONE);
        view.findViewById(R.id.monster_cache_layout).setVisibility(View.GONE);

        view.findViewById(R.id.listview_container).setVisibility(View.GONE);

        bestiaryHistoryContainer.setVisibility(View.VISIBLE);
        versionSpinner.setVisibility(View.GONE);
    }

    public boolean isNpcDetailsVisible() {
        return view.findViewById(R.id.monster_info_layout).getVisibility() == View.VISIBLE;
    }

    public boolean isNpcDropViewVisible() {
        return view.findViewById(R.id.listview_container).getVisibility() == View.VISIBLE;
    }

    public boolean isNpcHistoryVisible() {
        return bestiaryHistoryContainer.getVisibility() == View.VISIBLE;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.show_drops_button) {
            showMonsterDrops();
        }
        else if (id == R.id.navbar_back) {
            handleBackClick();
        }
        else if (id == R.id.npc_force_refresh_button) {
            if (npc == null) return;
            toggleProgressBar(true);
            lookupNpc(npc.name, true);
        }
        else if (id == R.id.bestiary_history_clear) {
            new BestiaryAsyncTasks.ClearHistory(context).execute();
            if (bestiaryHistoryAdapter != null) {
                bestiaryHistoryAdapter.updateList(new ArrayList<String>());
            }
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        updateNpc(position);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onWikiRequestStart() {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                toggleProgressBar(true);
            }
        });
    }

    @Override
    public void onWikiRequestError(Exception e) {
        showToast(getString(R.string.failed_to_obtain_data, "rswiki npc data", e.getClass().getSimpleName()), Toast.LENGTH_LONG);
    }

    @Override
    public void onWikiRequestEnd(final boolean hasResults) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                toggleProgressBar(false);
                if (!hasResults) {
                    showToast(getString(R.string.npc_no_results), Toast.LENGTH_SHORT);
                }
            }
        });
    }

    private void toggleProgressBar(boolean visible) {
        progressBar.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    private void toggleCacheMessage(boolean visible) {
        view.findViewById(R.id.npc_cache_info).setVisibility(visible ? View.VISIBLE : View.GONE);
        view.findViewById(R.id.npc_force_refresh_button).setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    public String getNpcName() {
        return npc != null ? npc.name : null;
    }

    public boolean handleBackClick() {
        if (isNpcDropViewVisible()) {
            showMonsterDetails();
            return true;
        }
        else if (isNpcDetailsVisible()) {
            showHistory();
            return true;
        }
        else if (isNpcHistoryVisible()) {
            return false;
        }
        return false;
    }
}