package com.dennyy.osrscompanion.layouthandlers;

import android.content.Context;
import android.os.AsyncTask;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.dennyy.osrscompanion.R;
import com.dennyy.osrscompanion.adapters.FairyRingListAdapter;
import com.dennyy.osrscompanion.adapters.FairyRingSearchAdapter;
import com.dennyy.osrscompanion.customviews.ClearableAutoCompleteTextView;
import com.dennyy.osrscompanion.customviews.DelayedAutoCompleteTextView;
import com.dennyy.osrscompanion.helpers.Utils;
import com.dennyy.osrscompanion.models.FairyRings.FairyRing;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class FairyRingViewHandler extends BaseViewHandler implements TextWatcher, View.OnClickListener {
    public int selectedIndex;

    private DelayedAutoCompleteTextView autoCompleteTextView;
    public ListView listView;
    private FairyRingSearchAdapter searchAdapter;
    private FairyRingListAdapter listViewAdapter;
    private ArrayList<FairyRing> fairyRings;

    public FairyRingViewHandler(Context context, final View view) {
        super(context, view);
        new LoadItems(context, new FairyRingsLoadedLoadedCallback() {
            @Override
            public void onLoaded(ArrayList<FairyRing> items) {
                fairyRings = new ArrayList<>(items);
                updateView(view);
            }

            @Override
            public void onLoadError() {
                showToast(resources.getString(R.string.exception_occurred, "exception", "loading items from file"), Toast.LENGTH_LONG);
            }
        }).execute();
    }

    private void updateView(View view) {
        listView = view.findViewById(R.id.fairy_rings_listview);
        autoCompleteTextView = ((ClearableAutoCompleteTextView) view.findViewById(R.id.fr_search_input)).getAutoCompleteTextView();

        if (searchAdapter == null) {
            searchAdapter = new FairyRingSearchAdapter(context, fairyRings);
        }
        if (listViewAdapter == null) {
            listViewAdapter = new FairyRingListAdapter(context, fairyRings);
        }
        listView.setAdapter(listViewAdapter);
        autoCompleteTextView.setAdapter(searchAdapter);
        autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Utils.hideKeyboard(context, autoCompleteTextView);
                String selection = adapterView.getItemAtPosition(position).toString();
                selectedIndex = -1;
                for (int i = 0; i < fairyRings.size(); i++) {
                    if (fairyRings.get(i).code.toLowerCase().equals(selection.toLowerCase())) {
                        selectedIndex = i;
                        break;
                    }
                }
                listView.setSelection(selectedIndex);
            }
        });

        autoCompleteTextView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                return false;
            }
        });

        autoCompleteTextView.addTextChangedListener(this);
        listView.setSelection(selectedIndex);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        if (s.toString().trim().length() == 0) {

            searchAdapter.resetItems();
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {

        }
    }

    private static class LoadItems extends AsyncTask<String, Void, ArrayList<FairyRing>> {
        private WeakReference<Context> context;
        private FairyRingsLoadedLoadedCallback fairyRingsLoadedLoadedCallback;

        private LoadItems(Context context, FairyRingsLoadedLoadedCallback fairyRingsLoadedLoadedCallback) {
            this.context = new WeakReference<>(context);
            this.fairyRingsLoadedLoadedCallback = fairyRingsLoadedLoadedCallback;
        }

        @Override
        protected ArrayList<FairyRing> doInBackground(String... params) {
            ArrayList<FairyRing> fairyRings = new ArrayList<>();
            try {
                String fairyRingsString = Utils.readFromAssets(context.get(), "fairyrings.json");
                JSONArray fairyRingJsonArray = new JSONArray(fairyRingsString);
                for (int i = 0; i < fairyRingJsonArray.length(); i++) {
                    JSONObject jsonObject = fairyRingJsonArray.getJSONObject(i);
                    FairyRing fairyRing = new FairyRing();
                    fairyRing.code = jsonObject.getString("code");
                    fairyRing.location = jsonObject.getString("location");
                    fairyRing.pointsOfInterest = jsonObject.getString("points of interest");
                    fairyRings.add(fairyRing);
                }
            }
            catch (JSONException ignored) {

            }
            return fairyRings;
        }

        @Override
        protected void onPostExecute(ArrayList<FairyRing> items) {
            if (items.size() > 0) {
                fairyRingsLoadedLoadedCallback.onLoaded(items);
            }
            else {
                fairyRingsLoadedLoadedCallback.onLoadError();
            }
        }
    }

    public interface FairyRingsLoadedLoadedCallback {
        void onLoaded(ArrayList<FairyRing> fairyRings);

        void onLoadError();
    }

    @Override
    public void cancelVolleyRequests() {
    }

    @Override
    public boolean wasRequesting() {
        return false;
    }

}
