package com.dennyy.oldschoolcompanion.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.List;

public class GenericAdapter<T> extends BaseAdapter {
    protected final Context context;
    protected final LayoutInflater inflater;
    protected final ArrayList<T> collection;
    protected final ArrayList<T> originalCollection;

    public GenericAdapter(Context context, List<T> collection) {
        this.context = context;
        this.collection = new ArrayList<>(collection);
        this.originalCollection = new ArrayList<>(collection);
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return collection.size();
    }

    @Override
    public T getItem(int i) {
        return collection.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(final int i, View convertView, ViewGroup viewGroup) {
        throw new RuntimeException("Override this method and use your own implementation");
    }

    public void updateList(List<T> newCollection) {
        this.collection.clear();
        this.collection.trimToSize();
        this.collection.addAll(newCollection);
        this.notifyDataSetChanged();
    }

    public void resetList() {
        this.collection.clear();
        this.collection.trimToSize();
        this.collection.addAll(originalCollection);
        this.notifyDataSetChanged();
    }
}