package com.dennyy.osrscompanion.layouthandlers;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.dennyy.osrscompanion.R;
import com.dennyy.osrscompanion.adapters.ExperienceAdapter;
import com.dennyy.osrscompanion.helpers.RsUtils;
import com.dennyy.osrscompanion.helpers.Utils;
import com.dennyy.osrscompanion.models.General.Experience;

import java.util.ArrayList;

public class ExpCalculatorViewHandler extends BaseViewHandler {

    public ExpCalculatorViewHandler(Context context, View view) {
        super(context, view);
        initializeListView();
        ((EditText) view.findViewById(R.id.exp_or_lvl_input)).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            Handler handler = new Handler(Looper.getMainLooper() /*UI thread*/);
            Runnable workRunnable;

            @Override
            public void afterTextChanged(final Editable editable) {
                handler.removeCallbacks(workRunnable);
                workRunnable = new Runnable() {
                    @Override
                    public void run() {
                        if (!editable.toString().isEmpty()) {
                            calculateExpAndLevel();
                        }
                    }
                };
                handler.postDelayed(workRunnable, 500 /*delay*/);
            }
        });
        final EditText fromLvlInput = view.findViewById(R.id.from_lvl_input);
        final EditText toLvlInput = view.findViewById(R.id.to_lvl_input);
        fromLvlInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            Handler handler = new Handler(Looper.getMainLooper() /*UI thread*/);
            Runnable workRunnable;

            @Override
            public void afterTextChanged(final Editable editable) {
                handler.removeCallbacks(workRunnable);
                workRunnable = new Runnable() {
                    @Override
                    public void run() {
                        if (!editable.toString().isEmpty() && !toLvlInput.getText().toString().isEmpty()) {
                            calculateDifference();
                        }
                    }
                };
                handler.postDelayed(workRunnable, 500 /*delay*/);
            }
        });
        toLvlInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            Handler handler = new Handler(Looper.getMainLooper() /*UI thread*/);
            Runnable workRunnable;

            @Override
            public void afterTextChanged(final Editable editable) {
                handler.removeCallbacks(workRunnable);
                workRunnable = new Runnable() {
                    @Override
                    public void run() {
                        if (!editable.toString().isEmpty() && !fromLvlInput.getText().toString().isEmpty()) {
                            calculateDifference();
                        }
                    }
                };
                handler.postDelayed(workRunnable, 500 /*delay*/);
            }
        });

        ListView lv = view.findViewById(R.id.exp_listview);

    }

    private void calculateDifference() {
        int fromEditTextValue = getValueFromEditText(R.id.from_lvl_input, 1, 126);
        int toEditTextValue = getValueFromEditText(R.id.to_lvl_input, 1, 126);

        if (fromEditTextValue > toEditTextValue) {
            showToast(resources.getString(R.string.lvl_from_higher_than_to), Toast.LENGTH_SHORT);
            return;
        }

        int diff = RsUtils.exp(toEditTextValue) - RsUtils.exp(fromEditTextValue);

        ((TextView) view.findViewById(R.id.lvl_diff_textview)).setText(String.valueOf(Utils.formatNumber(diff)));
    }

    private void calculateExpAndLevel() {
        int editTextValue = getValueFromEditText(R.id.exp_or_lvl_input, 0, 200_000_000);
        int lvl = RsUtils.lvl(editTextValue, false);
        int exp = RsUtils.exp(Math.min(editTextValue, 126));

        ((TextView) view.findViewById(R.id.level_textview)).setText(context.getString(R.string.lvl_formatted, editTextValue));
        ((TextView) view.findViewById(R.id.calc_exp_textview)).setText(String.format("%s exp", Utils.formatNumber(exp)));
        ((TextView) view.findViewById(R.id.experience_textview)).setText(context.getString(R.string.exp_formatted, editTextValue));
        ((TextView) view.findViewById(R.id.calc_lvl_textview)).setText(String.format("Lvl %s", Utils.formatNumber(lvl)));
    }

    private int getValueFromEditText(int viewId, int min, int max) {
        View v = view.findViewById(viewId);
        int defaultLevel = 1;
        if (!(v instanceof EditText)) {
            return defaultLevel;
        }
        String text = ((EditText) v).getText().toString();
        try {
            int value = Integer.parseInt(text);
            if (value >= min && value <= max) {
                return value;
            }
            return defaultLevel;
        }
        catch (NumberFormatException e) {
            ((EditText) v).setText(String.valueOf(defaultLevel));
            return defaultLevel;
        }
    }

    private void initializeListView() {
        ListView expListView = view.findViewById(R.id.exp_listview);
        ArrayList<Experience> experiences = new ArrayList<>();
        for (int i = 1; i < 127; i++) {
            Experience experience = new Experience();
            experience.level = i;
            experience.experience = RsUtils.exp(i);
            experience.difference = RsUtils.exp(i) - RsUtils.exp(i - 1);
            experiences.add(experience);
        }
        ExperienceAdapter adapter = new ExperienceAdapter(context, experiences);
        expListView.setAdapter(adapter);
    }

    @Override
    public boolean wasRequesting() {
        return false;
    }

    @Override
    public void cancelVolleyRequests() {

    }
}
