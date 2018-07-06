package com.dennyy.osrscompanion.customviews;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.preference.ListPreference;
import android.util.AttributeSet;

import com.dennyy.osrscompanion.helpers.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class CheckboxDialogPreference extends ListPreference {

    private Context context;
    private String separator;
    public static final String DEFAULT_SEPARATOR = "~";
    private boolean[] entryChecked;
    private List<DialogClosedListener> listeners = new ArrayList<>();

    public CheckboxDialogPreference(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.context = context;
        entryChecked = new boolean[getEntries().length];
        separator = DEFAULT_SEPARATOR;
    }

    public CheckboxDialogPreference(Context context) {
        this(context, null);
    }

    public void addListener(DialogClosedListener listenerToAdd) {
        listeners.add(listenerToAdd);
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        CharSequence[] entries = getEntries();
        CharSequence[] entryValues = getEntryValues();
        if (entries == null || entryValues == null
                || entries.length != entryValues.length) {
            throw new IllegalStateException(
                    "MultiSelectListPreference requires an entries array and an entryValues "
                            + "array which are both the same length");
        }

        restoreCheckedEntries();
        DialogInterface.OnMultiChoiceClickListener listener = new DialogInterface.OnMultiChoiceClickListener() {
            public void onClick(DialogInterface dialog, int which, boolean val) {
                entryChecked[which] = val;
            }
        };
        builder.setMultiChoiceItems(entries, entryChecked, listener);
    }


    /**
     * Gets the entries values that are selected
     *
     * @return the selected entries values
     */
    public CharSequence[] getCheckedValues() {
        return Utils.seperate(getValue(), DEFAULT_SEPARATOR);
    }

    private void restoreCheckedEntries() {
        CharSequence[] entryValues = getEntryValues();

        // Explode the string read in sharedpreferences
        CharSequence[] vals = Utils.seperate(getValue(), DEFAULT_SEPARATOR);

        if (vals != null) {
            List<CharSequence> valuesList = Arrays.asList(vals);
            for (int i = 0; i < entryValues.length; i++) {
                CharSequence entry = entryValues[i];
                entryChecked[i] = valuesList.contains(entry);
            }
        }
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        List<CharSequence> values = new ArrayList<>();

        CharSequence[] entryValues = getEntryValues();
        if (positiveResult && entryValues != null) {
            for (int i = 0; i < entryValues.length; i++) {
                if (entryChecked[i]) {
                    String val = (String) entryValues[i];
                    values.add(val);
                }
            }

            String value = Utils.join(values, separator);

            for (DialogClosedListener listener : listeners) {
                listener.onClose(this, value);
            }
            //setSummary(prepareSummary(values));
            //setValueAndEvent(value);
        }
    }

    public void setValueAndEvent(String value) {
        if (callChangeListener(Utils.seperate(value, DEFAULT_SEPARATOR))) {
            setValue(value);
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray typedArray, int index) {
        return typedArray.getTextArray(index);
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object rawDefaultValue) {
        String value;
        CharSequence[] defaultValue;
        if (rawDefaultValue == null) {
            defaultValue = new CharSequence[0];
        }
        else {
            defaultValue = (CharSequence[]) rawDefaultValue;
        }
        List<CharSequence> joined = Arrays.asList(defaultValue);
        String joinedDefaultValue = Utils.join(joined, separator);
        if (restoreValue) {
            value = getPersistedString(joinedDefaultValue);
        }
        else {
            value = joinedDefaultValue;
        }
        setValueAndEvent(value);
    }

    public interface DialogClosedListener {
        void onClose(CheckboxDialogPreference self, String selection);
    }
}
