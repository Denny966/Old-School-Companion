package com.dennyy.osrscompanion.fragments;


import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dennyy.osrscompanion.R;
import com.dennyy.osrscompanion.adapters.HiscoresViewPagerAdapter;

public class HiscoresFragmentViewPager extends BaseFragment {


    public HiscoresFragmentViewPager() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.hiscores_viewpager_layout, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ViewPager viewPager = (ViewPager) getActivity().findViewById(R.id.viewpager);
        final HiscoresViewPagerAdapter adapterViewPager = new HiscoresViewPagerAdapter(getActivity(), getChildFragmentManager());
        viewPager.setAdapter(adapterViewPager);

        // Add tabbar to viewpager
        TabLayout tabLayout = (TabLayout) getActivity().findViewById(R.id.sliding_tabs);
        tabLayout.setupWithViewPager(viewPager);
    }
}
