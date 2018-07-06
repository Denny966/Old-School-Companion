package com.dennyy.osrscompanion.adapters;


import android.app.FragmentManager;
import android.content.Context;
import android.support.v13.app.FragmentPagerAdapter;

import com.dennyy.osrscompanion.R;
import com.dennyy.osrscompanion.fragments.HiscoresCompareFragment;
import com.dennyy.osrscompanion.fragments.HiscoresLookupFragment;


public class HiscoresViewPagerAdapter extends FragmentPagerAdapter {
    private static int NUM_ITEMS = 2;

    private Context context;

    public HiscoresViewPagerAdapter(Context context, FragmentManager fm) {
        super(fm);

        this.context = context;
    }

    // Returns total number of pages
    @Override
    public int getCount() {
        return NUM_ITEMS;
    }


    // Returns the fragment to display for that page
    @Override
    public android.app.Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new HiscoresLookupFragment();
            case 1:
                return new HiscoresCompareFragment();
            default:
                return null;
        }
    }


    // Returns the page title for the top indicator
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return context.getResources().getString(R.string.lookup);
            case 1:
                return context.getResources().getString(R.string.compare);
            default:
                return context.getResources().getString(R.string.hiscores);
        }
    }
}
