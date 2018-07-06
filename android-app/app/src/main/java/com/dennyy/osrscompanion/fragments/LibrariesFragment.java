package com.dennyy.osrscompanion.fragments;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.dennyy.osrscompanion.R;
import com.dennyy.osrscompanion.customviews.LibraryCardview;

public class LibrariesFragment extends BaseFragment {

    private View view;


    public LibrariesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        view = inflater.inflate(R.layout.libraries_layout, container, false);
        return view;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        toolbarTitle.setText(getResources().getString(R.string.libraries));

        LinearLayout rootLinearLayout = (LinearLayout) view.findViewById(R.id.libraries_listview);
        int count = rootLinearLayout.getChildCount();
        for (int i = 0; i < count; i++) {
            View v = rootLinearLayout.getChildAt(i);
            if (v instanceof LibraryCardview) {
                final LibraryCardview libraryCardview = (LibraryCardview) v;
                libraryCardview.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(libraryCardview.getUrl()));
                        startActivity(browserIntent);
                    }
                });
            }
        }
    }
}

