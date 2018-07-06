package com.dennyy.osrscompanion;

import android.app.FragmentManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.dennyy.osrscompanion.fragments.BaseFragment;
import com.dennyy.osrscompanion.fragments.HomeFragment;
import com.dennyy.osrscompanion.interfaces.IBackButtonHandler;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;

public class MainActivity extends AppCompatActivity implements IBackButtonHandler.BackButtonHandlerInterface {

    private BaseFragment mainFragment;
    private ArrayList<WeakReference<IBackButtonHandler.OnBackClickListener>> backClickListenersList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
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
            mainFragment = new HomeFragment();
            getFragmentManager().beginTransaction().replace(R.id.fragment_container, mainFragment).commit();
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
                boolean isFragmIntercept = onBackClickListener.onBackClick();
                if (!isIntercept)
                    isIntercept = isFragmIntercept;
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
