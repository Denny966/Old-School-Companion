package com.dennyy.oldschoolcompanion.adapters;

import android.content.*;
import android.view.*;
import android.widget.*;
import com.android.volley.*;
import com.android.volley.toolbox.*;
import com.dennyy.oldschoolcompanion.*;
import com.dennyy.oldschoolcompanion.R;
import com.dennyy.oldschoolcompanion.helpers.*;
import com.dennyy.oldschoolcompanion.interfaces.*;
import me.xdrop.fuzzywuzzy.*;
import org.json.*;

import java.util.*;
import java.util.concurrent.*;

public class BestiaryAdapter extends GenericAdapter<String> implements Filterable {

    public static final String WIKI_SEARCH_TAG = "wikisearchtag";
    private static final int REQUEST_TIMEOUT = 30;
    public boolean wasRequesting;

    private NpcWikiRequestListener listener;

    public BestiaryAdapter(Context context, ArrayList<String> npcs, NpcWikiRequestListener listener) {
        super(context, npcs);
        this.listener = listener;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.adapter_row_dropdown, null);
            viewHolder = new ViewHolder();
            viewHolder.text = convertView.findViewById(R.id.adapter_row_text);
            convertView.setTag(viewHolder);
        }
        else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        String wikiSearch = getItem(position);
        viewHolder.text.setText(wikiSearch);

        return convertView;
    }

    private static class ViewHolder {
        public TextView text;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                if (constraint == null || constraint.length() == 0) {
                    return results;
                }
                List<String> nlist = new ArrayList<>();
                List<String> fuzzyList = new ArrayList<>();
                for (String npc : originalCollection) {
                    if (npc.toLowerCase().contains(constraint.toString().toLowerCase())) {
                        nlist.add(npc);
                    }
                    else if (FuzzySearch.partialRatio(npc.toLowerCase(), constraint.toString().toLowerCase()) >= Constants.FUZZY_RATIO) {
                        fuzzyList.add(npc);
                    }
                }
                nlist.addAll(fuzzyList);
                if (nlist.isEmpty()) {
                    listener.onWikiRequestStart();
                    wasRequesting = true;
                    String url = String.format("https://oldschool.runescape.wiki/api.php?action=query&format=json&list=prefixsearch&pssearch=%1$s&psnamespace=0", Utils.getEncodedString(constraint.toString()));
                    RequestFuture<String> future = RequestFuture.newFuture();
                    StringRequest request = new StringRequest(Request.Method.GET, url, future, future);
                    AppController.getInstance().addToRequestQueue(request, WIKI_SEARCH_TAG, true);
                    try {
                        String result = future.get(REQUEST_TIMEOUT, TimeUnit.SECONDS);
                        nlist = parseData(result);
                    }
                    catch (Exception e) {
                        listener.onWikiRequestError(e);
                        Logger.log(e);
                    }
                    finally {
                        listener.onWikiRequestEnd(!nlist.isEmpty());
                    }
                }
                results.values = nlist;
                results.count = nlist.size();

                return results;
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                wasRequesting = false;
                if (results != null && results.count > 0) {
                    updateList((ArrayList<String>) results.values);
                }
                else {
                    notifyDataSetInvalidated();
                }
            }
        };
    }

    private ArrayList<String> parseData(String data) throws JSONException {
        ArrayList<String> result = new ArrayList<>();
        JSONObject root = new JSONObject(data);
        JSONObject query = root.getJSONObject("query");
        JSONArray search = query.getJSONArray("prefixsearch");
        ArrayList<JSONObject> results = Utils.jsonArrayToList(JSONObject.class, search);

        for (JSONObject obj : results) {
            String name = Utils.capitalize(obj.getString("title"));
            result.add(name);
        }
        return result;
    }
}
