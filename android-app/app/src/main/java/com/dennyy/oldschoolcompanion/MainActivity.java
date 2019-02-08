package com.dennyy.oldschoolcompanion;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.dennyy.oldschoolcompanion.fragments.BaseFragment;
import com.dennyy.oldschoolcompanion.fragments.HomeFragment;
import com.dennyy.oldschoolcompanion.helpers.AdBlocker;
import com.dennyy.oldschoolcompanion.helpers.GeHelper;
import com.dennyy.oldschoolcompanion.interfaces.IBackButtonHandler;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;

public class MainActivity extends AppCompatActivity implements IBackButtonHandler.BackButtonHandlerInterface {

    private ArrayList<WeakReference<IBackButtonHandler.OnBackClickListener>> backClickListenersList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AdBlocker.init(this);
        GeHelper.init(this);
        setContentView(R.layout.main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        if (savedInstanceState == null) {
            BaseFragment mainFragment = new HomeFragment();
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.add(R.id.fragment_container, mainFragment);
            transaction.commit();
        }
        getFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                getSupportActionBar().invalidateOptionsMenu();
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (!fragmentsBackKeyIntercept()) {
            super.onBackPressed();
        }
    }

    private boolean fragmentsBackKeyIntercept() {
        boolean isIntercept = false;
        for (WeakReference<IBackButtonHandler.OnBackClickListener> weakRef : backClickListenersList) {
            IBackButtonHandler.OnBackClickListener onBackClickListener = weakRef.get();
            if (onBackClickListener != null) {
                boolean isFragmentIntercept = onBackClickListener.onBackClick();
                if (!isIntercept)
                    isIntercept = isFragmentIntercept;
            }
        }
        return isIntercept;
    }

    @Override
    public void addBackClickListener(IBackButtonHandler.OnBackClickListener onBackClickListener) {
        backClickListenersList.add(new WeakReference<>(onBackClickListener));
    }

    @Override
    public void removeBackClickListener(IBackButtonHandler.OnBackClickListener onBackClickListener) {
        for (Iterator<WeakReference<IBackButtonHandler.OnBackClickListener>> iterator = backClickListenersList.iterator();
             iterator.hasNext(); ) {
            WeakReference<IBackButtonHandler.OnBackClickListener> weakRef = iterator.next();
            if (weakRef.get() == onBackClickListener) {
                iterator.remove();
            }
        }
    }
}
